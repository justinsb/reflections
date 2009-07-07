package org.reflections.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.reflections.ReflectionsException;

/**
 *
 */
public abstract class Utils {

    public static <T> Set<Class<? extends T>> forNames(final Collection<String> classes) throws ReflectionsException {
        Set<Class<? extends T>> result = new HashSet<Class<? extends T>>(classes.size());
        for (String className : classes) {
			result.add((Class<? extends T>) ReflectionUtil.resolveClass(className));
		}
        return result;
    }

    public static void safeWriteFile(String fileContent, File destFile) throws IOException {
        FileWriter writer = null;
        try {
            if (destFile.getParentFile() != null) {destFile.getParentFile().mkdirs();}

            writer = new FileWriter(destFile);
            writer.write(fileContent);
        } finally {
            try {
                //noinspection ConstantConditions
                writer.close();
            } catch (Exception e) {/*fuck off*/}
        }
    }

    public static ClassLoader getEffectiveClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
