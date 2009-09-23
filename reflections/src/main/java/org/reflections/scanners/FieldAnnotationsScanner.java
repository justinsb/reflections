package org.reflections.scanners;

//import jsr166y.forkjoin.ForkJoinPool;
//import jsr166y.forkjoin.Ops;
//import jsr166y.forkjoin.ParallelArray;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.reflections.NullObject;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 *
 */
@SuppressWarnings({"unchecked"})
public class FieldAnnotationsScanner extends AbstractScanner {

	public static final String indexName = "FieldAnnotations";

	public void scan(final Object cls) throws ReflectionsException {
		final String className = getMetadataAdapter().getClassName(cls);
		List<Object> fields = getMetadataAdapter().getFields(cls);
		
		for (final Object field : fields) {
			List<String> fieldAnnotations = getMetadataAdapter().getFieldAnnotationNames(field);
			
			parallelTransform(fieldAnnotations, new Function<String, NullObject>() {
				public NullObject apply(String fieldAnnotation) {
					if (accept(fieldAnnotation)) {
						String fieldName = getMetadataAdapter().getFieldName(field);
						populate(fieldAnnotation, String.format("%s.%s", className, fieldName));
					}
					return NullObject.NULL;
				}
			});
			
//			ParallelArray.createFromCopy(Iterables.toArray(fieldAnnotations, String.class), forkJoinPool).apply(new Ops.Procedure<String>() {
//				public void apply(String fieldAnnotation) {
//					if (accept(fieldAnnotation)) {
//						String fieldName = getMetadataAdapter().getFieldName(field);
//						populate(fieldAnnotation, String.format("%s.%s", className, fieldName));
//					}
//				}
//			});
		}
	}

	public String getIndexName() {
		return "FieldAnnotations";
	}
}
