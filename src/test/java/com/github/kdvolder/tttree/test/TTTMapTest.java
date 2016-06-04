package com.github.kdvolder.tttree.test;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.github.kdvolder.tttree.TTTMap;

public class TTTMapTest extends AbstractMapTestTemplate {

	@Override
	protected <K extends Comparable<K>, V> MutableMap<K, V> createEmptyMap() {
		return new MutableMap<K, V>() {

			private TTTMap<K, V> map = new TTTMap<>();

			@Override
			public void put(K k, V v) {
				map = map.insert(k, v);
			}

			@Override
			public V get(K k) {
				return map.get(k);
			}

			@Override
			public void remove(K k) {
				map = map.delete(k);
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
				map.dump();
			}

			@Override
			public boolean isEmpty() {
				return map.isEmpty();
			}
		};
	}

}
