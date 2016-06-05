package com.github.kdvolder.tttree;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A {@link TTTMap} wraps a {@link TTTree} adapting it to provide
 * a standard immutable {@link Map} implementation.
 * <p>
 * TODO: maybe this wrapper can be avoided if TTTree directly implements / subclasses
 * from AbstractMap. A potential drawback is that doesn't give us a good place to
 * cache the 'size' of the map (caching it in every tree node might be wasting space).
 *
 * @author Kris De Volder
 */
public class TTTMap<K extends Comparable<K>, V> extends AbstractMap<K, V> {

	private TTTree<K, V> map;
	private int size = -1; //computed the first time it is used.

	public TTTMap() {
		this(TTTree.empty());
	}

	public TTTMap(TTTree<K, V> map) {
		this.map = map;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new AbstractSet<Entry<K,V>>() {

			@Override
			public Iterator<Entry<K, V>> iterator() {
				return map.iterator();
			}

			@Override
			public boolean isEmpty() {
				return TTTMap.this.isEmpty();
			}

			@Override
			public int size() {
				return TTTMap.this.size();
			}
		};
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public int size() {
		if (size<0) {
			size = map.size();
		}
		return size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(Object k) {
		if (k instanceof Comparable<?>) {
			//This instanceof is not sufficient to make it safe, but
			//is the best we can do given that all the other type info
			//is 'erased' at runtime.
			return map.get((K)k);
		}
		return null;
	}

	/**
	 * Make a copy of this map, adding or changing a single key-value association
	 * in the copy.
	 * <p>
	 * If key-value association already exists in this map then no copy is made and
	 * the returned 'copy' is '==' to the receiver. Callers may use this knowledge
	 * to test whether the insert made any change to the map. For example:
	 * <pre>
	 *    copy = map.insert(key, newValue);
	 *    if (copy!=map) {
	 *       mapListeners.notifyChanged(...);
	 *    }
	 * </pre>
	 */
	public TTTMap<K, V> insert(K k, V v) {
		TTTree<K, V> copy = map.put(k, v);
		if (copy==map) {
			return this;
		}
		return new TTTMap<>(copy);
	}

	/**
	 * Make a copy of this map, deleting any existing association with a given key.
	 * <p>
	 * If the key is not present in this map, then the returned map is guaranteed to
	 * be identical (in the sense of '=='). Callers can safely use this knowledge to
	 * determine if the deleted key was presented in the original map.
	 */
	public TTTMap<K, V> delete(K k) {
		TTTree<K, V> copy = map.remove(k);
		if (copy==map) {
			return this;
		}
		return new TTTMap<>(copy);
	}

	public void dump() {
		map.dump();
	}
}
