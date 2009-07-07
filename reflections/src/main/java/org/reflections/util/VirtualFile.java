package org.reflections.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Iterators;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.reflections.ReflectionsException;

/**
 * a File and JarFile bridge
 */
public abstract class VirtualFile {
	public abstract InputStream getInputStream() throws IOException;

	public abstract String getName();

	//
	public static VirtualFile create(final File file) {
		return new VirtualFile() {
			public InputStream getInputStream() throws FileNotFoundException {
				return new FileInputStream(file);
			}

			public String getName() {
				return file.getName();
			}
		};
	}

	public static VirtualFile create(final JarFile jarFile, final JarEntry entry) {
		return new VirtualFile() {
			public InputStream getInputStream() throws IOException {
				try {
					return jarFile.getInputStream(entry);
				} catch (IOException e) {
					throw new IOException("Can't open a Jar file input stream " + jarFile.getName(), e);
				}
			}

			public String getName() {
				return entry.getName();
			}
		};
	}

	//
	public static Iterable<VirtualFile> iterable(final Collection<URL> urls) {
		return FluentIterable.iterate(urls).fork(urls2VirtualFiles);
	}

	public static Iterable<VirtualFile> iterable(final URL url) throws ReflectionsException {
		URI uri;
		try {
			uri = url.toURI();
		} catch (URISyntaxException e) {
			throw new ReflectionsException("Can't convert a URL to a URI: " + url, e);
		}

		File file = new File(uri);
		if (!file.exists()) {
			throw new ReflectionsException("File does not exist: " + url);
		}

		if (file.isDirectory()) {
			try {
				return iterable(file);
			} catch (IOException e) {
				throw new ReflectionsException("Could not iterate over directory: " + url, e);
			}
		}

		if (isJar(url)) {
			try {
				return iterable(new JarFile(file));
			} catch (IOException e) {
				throw new ReflectionsException("Could not iterate over Jar: " + url, e);
			}
		}

		throw new ReflectionsException("Could not create iterator of VirtualFiles from url " + url);
	}

	public static Iterable<VirtualFile> iterable(final File dir) throws IOException {
		if (!dir.isDirectory()) {
			throw new IOException(dir + "is not a directory");
		}

		return new Iterable<VirtualFile>() {
			public Iterator<VirtualFile> iterator() {
				try {
					return new DirFilesIterator(dir);
				} catch (IOException e) {
					throw new IllegalStateException("Error processing directory: " + dir, e);
				}
			}
		};
	}

	public static Iterable<VirtualFile> iterable(final JarFile jarFile) {
		return new Iterable<VirtualFile>() {
			public Iterator<VirtualFile> iterator() {
				return new JarFileIterator(jarFile);
			}
		};
	}

	//
	public static boolean isJar(final URL url) {
		return url.getFile().endsWith(".jar");
	}

	public static boolean isDirectory(final URL url) {
		return url.getFile().endsWith("/");
	}

	//

	/**
	 * iterates files recursively over a given root directory
	 */
	public static class DirFilesIterator extends AbstractIterator<VirtualFile> {
		private final Stack<File> fileStack;

		public DirFilesIterator(final File dir) throws IOException {
			if (!dir.isDirectory()) {
				throw new IOException(dir + "is not a directory");
			}
			fileStack = new Stack<File>();
			fileStack.add(dir);
		}

		private void addFilesFromDir(final File dir) {
			fileStack.addAll(Lists.newArrayList(dir.listFiles()));
		}

		@Override
		protected VirtualFile computeNext() {
			while (!fileStack.isEmpty()) {
				File file = fileStack.pop();

				if (file.isDirectory()) {
					addFilesFromDir(file);
				} else {
					return create(file);
				}
			}

			return endOfData();
		}
	}

	/**
	 * iterates jar entries recursively(?) over a given jar file
	 */
	public static class JarFileIterator extends AbstractIterator<VirtualFile> {
		private final Iterator<JarEntry> entries;
		private final JarFile jarFile;

		public JarFileIterator(final JarFile jarFile) {
			this.jarFile = jarFile;
			entries = Iterators.forEnumeration(jarFile.entries());
		}

		protected VirtualFile computeNext() {
			if (entries.hasNext()) {
				return create(jarFile, entries.next());
			} else {
				return endOfData();
			}
		}
	}

	public static final Transformer<URL, Iterator<VirtualFile>> urls2VirtualFiles = new Transformer<URL, Iterator<VirtualFile>>() {
		public Iterator<VirtualFile> transform(final URL url) {
			try {
				return VirtualFile.iterable(url).iterator();
			} catch (ReflectionsException e) {
				throw new IllegalArgumentException("Error transforming URLs", e);
			}
		}
	};

}
