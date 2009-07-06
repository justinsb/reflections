package org.reflections.adapters;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.reflections.ReflectionsException;

import com.google.common.base.Function;

public class ForkJoinerWrapper {
	/**
	 * Runs a multi-threaded transform task, wrapping the exceptions appropriately.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param source
	 * @param function
	 * @return
	 */
	public static <K, V> List<V> parallelTransform(ForkJoiner forkJoiner, Iterable<K> source, final Function<K, V> function, Class<K> keyClass) throws ReflectionsException  {
		try {
			return forkJoiner.transform(source, function, keyClass);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ReflectionsException("Multi-threaded execution interrupted", e);
		} catch (ExecutionException e) {
			Thread.currentThread().interrupt();
			throw new ReflectionsException("Error in parallel function execution", e);
		}
		
		// We could also implement this using jsr166, using something like the code below...
		// The ability to have this switchable (e.g. if jsr166 is present!) would be pretty cool
		
//		ParallelArray<String> parallelArray = ParallelArray.createFromCopy(Iterables.toArray(source, K.class), forkJoinPool);
//		return ImmutableSet.of(parallelArray.withMapping(new Ops.Mapper<K, V>() {
//			public V map(K key) {
//				return function.apply(key);
//			}
//		}).all().getArray());
	}
}
