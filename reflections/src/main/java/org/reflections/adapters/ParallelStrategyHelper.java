package org.reflections.adapters;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.reflections.ReflectionsException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ParallelStrategyHelper {
	private ParallelStrategyHelper() {
	}

	/**
	 * Runs a multi-threaded transform task, wrapping the exceptions appropriately.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param source
	 * @param function
	 * @return
	 */
	public static <K, V> List<V> parallelTransform(ParallelStrategy parallelStrategy, Iterable<K> source, final Function<K, V> function) throws ReflectionsException {
		if (source == null)
			return Collections.emptyList();

		try {
			return parallelStrategy.transform(source.iterator(), function);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ReflectionsException("Multi-threaded execution interrupted", e);
		} catch (ExecutionException e) {
			throw new ReflectionsException("Error in parallel function execution", e);
		}
	}

	public static <K, V> void apply(ParallelStrategy parallelStrategy, K[] source, Function<K, V> function) throws ReflectionsException {
		if (source == null || source.length == 0)
			return;

		// A little inefficient (we collect the results and throw them away), but not critically so...
		parallelTransform(parallelStrategy, Lists.newArrayList(source), function);
	}

}
