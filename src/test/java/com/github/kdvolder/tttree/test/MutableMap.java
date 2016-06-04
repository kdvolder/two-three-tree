package com.github.kdvolder.tttree.test;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.github.kdvolder.tttree.TTTree;
import com.google.common.collect.ImmutableSortedMap;

/**
 * Abstract class representing a minimal interface for a 'MutableMap'.
 * Used by the test to subclass and create a wrapper around some
 * immutable map implementation, making it mutable.
 * <p>
 * What exactly is the point of this? To make it possible to
 * do some comparisons between different immutable map
 * implementations (one of them our {@link TTTree})
 * <p>
 * I.e. by adapting each implementation to provide a similar
 * interface, we can write a single test and run it with
 * both implementations, then compare some performance
 * results.
 */
public abstract class MutableMap<K extends Comparable<K>, V> {

	public abstract void put(K k, V v);
	public abstract V get(K k);
	public abstract void remove(K k);
	public abstract Set<K> keySet();
	public abstract Iterator<Entry<K, V>> iterator();
	public abstract void dump();
	public abstract boolean isEmpty();

	public static <K extends Comparable<K>, V> MutableMap<K, V> from(ImmutableSortedMap<K, V> _map) {
		return new MutableMap<K, V>() {
			ImmutableSortedMap<K, V> map = _map;

			@Override
			public void put(K k, V v) {
				map = ImmutableSortedMap.<K,V>naturalOrder()
					.putAll(map)
					.put(k, v)
					.build();
			}

			@Override
			public V get(K k) {
				return map.get(k);
			}

			@Override
			public void remove(K k) {
				map = ImmutableSortedMap.<K,V>naturalOrder()
					.putAll(map.headMap(k))
					.putAll(map.tailMap(k, false))
					.build();
			}

			@Override
			public Set<K> keySet() {
				return map.keySet();
			}

			@Override
			public Iterator<Entry<K, V>> iterator() {
				return map.entrySet().iterator();
			}

			@Override
			public void dump() {
				//not implemented don't care.
			}

			@Override
			public boolean isEmpty() {
				return map.isEmpty();
			}
		};
	}


	public static <K extends Comparable<K>, V> MutableMap<K, V> from(TTTree<K, V> _map) {
		return new MutableMap<K, V>() {
			TTTree<K, V> map = _map;
			@Override
			public void put(K k, V v) {
				map = map.put(k, v);
			}

			@Override
			public V get(K k) {
				return map.get(k);
			}

			@Override
			public void remove(K k) {
				map = map.remove(k);
			}

			@Override
			public Set<K> keySet() {
				return map.keySet();
			}

			@Override
			public Iterator<Entry<K, V>> iterator() {
				return map.iterator();
			}

			@Override
			public void dump() {
				map.dump();
			}

			@Override
			public boolean isEmpty() {
				return map.isEmpty();
			}
		};
	}

}
