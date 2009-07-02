package org.reflections.scanners;

//import jsr166y.forkjoin.ParallelArray;
//import jsr166y.forkjoin.Ops;
//import jsr166y.forkjoin.ForkJoinPool;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.reflections.NullObject;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.adapters.SimpleForkJoiner;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 *
 */
@SuppressWarnings( { "unchecked" })
public class ClassAnnotationsScanner extends AbstractScanner {
	public static final String indexName = "ClassAnnotations";

	// private final ForkJoinPool forkJoinPool = new ForkJoinPool(16);

	public void scan(final Object cls) {
		final String className = getMetadataAdapter().getClassName(cls);
		List<String> annotationTypes = getMetadataAdapter().getClassAnnotationNames(cls);

		parallelTransform(annotationTypes, new Function<String, NullObject>() {
			public NullObject apply(String annotationType) {
				if (accept(annotationType)) {
					populate(annotationType, className);
				}
				return NullObject.NULL;
			}
		}, String.class);

		// ParallelArray.createFromCopy(Iterables.toArray(annotationTypes, String.class), forkJoinPool).apply(new Ops.Procedure<String>() {
		// public void apply(String annotationType) {
		// if (accept(annotationType)) {
		// populate(annotationType, className);
		// }
		// }
		// });
	}

	public String getIndexName() {
		return indexName;
	}
}
