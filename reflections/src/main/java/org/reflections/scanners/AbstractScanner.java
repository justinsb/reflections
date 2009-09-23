package org.reflections.scanners;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.reflections.Configuration;
import org.reflections.ReflectionsException;
import org.reflections.filters.Filter;
import org.reflections.filters.Any;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.adapters.ParallelStrategy;
import org.reflections.adapters.ParallelStrategyHelper;

/**
 *
 */
@SuppressWarnings( { "RawUseOfParameterizedType", "unchecked" })
public abstract class AbstractScanner implements Scanner {

	private Configuration configuration;
	private Multimap<String, String> store;
	private Filter<String> filter = Any.ANY;

	public Scanner filterBy(Filter<String> filter) {
		this.filter = filter;
		return this;
	}

	public void setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
	}

	public void setStore(final Multimap<String, String> store) {
		this.store = store;
	}

	protected MetadataAdapter getMetadataAdapter() {
		return configuration.getMetadataAdapter();
	}

	protected ParallelStrategy getParallelStrategy() {
		return configuration.getParallelStrategy();
	}

	protected <K, V> List<V> parallelTransform(Iterable<K> source, final Function<K, V> function) throws ReflectionsException {
		return ParallelStrategyHelper.parallelTransform(getParallelStrategy(), source, function);
	}

	protected void populate(final String key, final String value) {
		multiRelaxAdd(store, key, value);
	}

	protected boolean accept(final String fqn) {
		return fqn != null && filter.accept(fqn);
	}

	protected <T> void multiRelaxAdd(final Multimap<T, T> map, final T key, final T... values) {
		map.putAll(key, ImmutableSet.of(values));
	}
}
