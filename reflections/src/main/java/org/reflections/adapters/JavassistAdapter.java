package org.reflections.adapters;

import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import org.reflections.filters.Filter;
import org.reflections.util.DescriptorHelper;
import org.reflections.util.FluentIterable;
import org.reflections.util.Transformer;
import org.reflections.util.VirtualFile;
import static org.reflections.util.VirtualFile.urls2VirtualFiles;
import org.reflections.ReflectionsException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.common.collect.AbstractIterator;

/**
 *
 */
public class JavassistAdapter implements MetadataAdapter<ClassFile, FieldInfo, MethodInfo> {
	private static final Logger log = LoggerFactory.getLogger(JavassistAdapter.class);
	private static final Pattern METHOD_PARAMS_PATTERN = Pattern.compile(".*\\((.*)\\).*");

    public List<FieldInfo> getFields(final ClassFile cls) {
        //noinspection unchecked
        return cls.getFields();
    }

    public List<MethodInfo> getMethods(final ClassFile cls) {
        //noinspection unchecked
        return cls.getMethods();
    }

    public String getMethodName(final MethodInfo method) {
        return method.getName();
    }

    public List<String> getParameterNames(final MethodInfo method) {
        return DescriptorHelper.methodDescriptorToParameterNameList(method.getDescriptor());
    }

    public List<String> getClassAnnotationNames(final ClassFile aClass) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) aClass.getAttribute(AnnotationsAttribute.visibleTag);
        return getAnnotationNames(annotationsAttribute);
    }

    public List<String> getFieldAnnotationNames(final FieldInfo field) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) field.getAttribute(AnnotationsAttribute.visibleTag);

        return getAnnotationNames(annotationsAttribute);
    }

    public List<String> getMethodAnnotationNames(final MethodInfo method) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) method.getAttribute(AnnotationsAttribute.visibleTag);

        return getAnnotationNames(annotationsAttribute);
    }

    public List<String> getParameterAnnotationNames(final MethodInfo method, final int parameterIndex) {
        ParameterAnnotationsAttribute parameterAnnotationsAttribute = (ParameterAnnotationsAttribute) method.getAttribute(ParameterAnnotationsAttribute.visibleTag);

        if (parameterAnnotationsAttribute != null) {
            Annotation[][] annotations = parameterAnnotationsAttribute.getAnnotations();
            Annotation[] annotation = annotations[parameterIndex];
            return getAnnotationNames(annotation);
        } else {
            return new ArrayList<String>();
        }
    }

    public String getReturnTypeName(final MethodInfo method) {
        return DescriptorHelper.methodDescriptorToReturnTypeName(method.getDescriptor());
    }

    public String getFieldName(final FieldInfo field) {
        return field.getName();
    }

    public String getMethodKey(final MethodInfo method) throws ReflectionsException {
        String descriptor = method.getDescriptor();
		Matcher matcher = METHOD_PARAMS_PATTERN.matcher(descriptor);
		if (!matcher.matches()) {
			throw new ReflectionsException("The regex pattern is probably wrong.");
		}
		String paramDescriptor = matcher.group(1);

		return String.format("%s %s", getMethodName(method), paramDescriptor);
    }

    //

    public String getClassName(final ClassFile cls) {
        return cls.getName();
    }

    public String getSuperclassName(final ClassFile cls) {
        return cls.getSuperclass();
    }

    public List<String> getInterfacesNames(final ClassFile cls) {
        return Arrays.asList(cls.getInterfaces());
    }

	    public Iterator<ClassFile> iterateClasses(final Collection<URL> urls, final Filter<String> filter) {
        return new AbstractIterator<ClassFile>() {
            private Iterator<URL> urlsIterator = urls.iterator();
            private Iterator<VirtualFile> virtualFileIterator = new FluentIterable.EmptyIterator<VirtualFile>();

            @SuppressWarnings({"ThrowFromFinallyBlock"})
            protected ClassFile computeNext() {
                while (true) {
                    if (!virtualFileIterator.hasNext()) {
                        if (!urlsIterator.hasNext()) {
                            return endOfData();
                        } else {
                            URL url = urlsIterator.next();
                            try {
								virtualFileIterator = VirtualFile.iterable(url).iterator();
							} catch (ReflectionsException e) {
								throw new IllegalArgumentException("Error processing url: " + url, e);
							}
                        }
                    } else {
                        VirtualFile virtualFile = virtualFileIterator.next();
                        InputStream inputStream = null;
                        BufferedInputStream bis = null;
                        try {
                        	inputStream = virtualFile.getInputStream();
                            bis = new BufferedInputStream(inputStream);
                            DataInputStream dis = new DataInputStream(bis);
                            ClassFile classFile = new ClassFile(dis);
                            String className = getClassName(classFile);
                            if (filter.accept(className)) {
                                return classFile;
                            }
                        }
                        catch (IOException e) {
                            // need to filter directories and none class files
                        	log.debug("Ignoring error reading class file", e);
                        }
                        finally {
                            if (bis != null) {
								try {
									bis.close();
								} catch (IOException e) {
									log.warn("Ignoring error closing InputStream", e);
								}
							}
                        }
                    }
                }
            }
        };
    }

    public Iterable<ClassFile> iterateClasses(final Collection<URL> urls) {
        return FluentIterable
                .iterate(urls)
                .fork(urls2VirtualFiles)
                .filter(classesOnly)
                .transform(virtualFile2ClassFile);
    }

    //
    private List<String> getAnnotationNames(final AnnotationsAttribute annotationsAttribute) {
        if (annotationsAttribute == null) {return new ArrayList<String>(0);}

        final Annotation[] annotations = annotationsAttribute.getAnnotations();
        return getAnnotationNames(annotations);
    }

    private List<String> getAnnotationNames(final Annotation[] annotations) {
        List<String> result = new ArrayList<String>();

        for (Annotation annotation : annotations) {
            result.add(annotation.getTypeName());
        }

        return result;
    }

    //
    //filter only VirtualFiles that are .class files
	private static final Filter<VirtualFile> classesOnly = new Filter<VirtualFile>() {
        public boolean accept(final VirtualFile virtualFile) {
            return virtualFile.getName().endsWith(".class");
        }
    };

    //transform VirtualFile to ClassFile
	private static final Transformer<VirtualFile, ClassFile> virtualFile2ClassFile = new Transformer<VirtualFile, ClassFile>() {
        public ClassFile transform(final VirtualFile virtualFile) {
            BufferedInputStream bis = null;
			try {
				return new ClassFile(new DataInputStream(bis = new BufferedInputStream(virtualFile.getInputStream())));
            }
            catch (IOException e) {
                log.warn(String.format("Ignoring IOException while scanning %s", virtualFile.getName()));
                return null;
            }
            finally {
                if (bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						log.error("Ignoring error closing stream", e);
					}
				}
            }
        }
    };

}
