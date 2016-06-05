package com.github.kdvolder.tttree;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A wrapper around a {@link TTTree} that implements {@link Set}. The elements in the set are the keys of
 * the TTTree. The {@link TTTSet} behaves the way one would expect from an immutable set. It also provides
 * some additional methods to make modified copies of the Set.
 */
public class TTTSet<E extends Comparable<E>> extends AbstractSet<E> {

	private static final class Null {
		Null() {}
	}

	private static final Null NULL = new Null();

	//TODO: we can create a more memory efficient implementation
	// The current implementation essentialy uses a map where all the values stored are 'null' or ignored.
	// This is the easiest way to implement TTTSet (i.e. least amount of coding effort)
	private final TTTree<E, Object> map;

	/**
	 * Create an empty TTTSet
	 */
	public TTTSet() {
		this(TTTree.empty());
	}

	@SuppressWarnings("unchecked")
	public TTTSet(TTTree<E, ?> map) {
		this.map = (TTTree<E, Object>) map;
	}

	/**
	 * The real size is only computed if it is required as it implies counting the elements in the tree.
	 */
	private int size = -1;

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public int size() {
		if (size<0) {
			size = map.size();
		}
		return size;
	}

	public TTTSet<E> insert(E e) {
		TTTree<E, Object> copy = map.put(e, NULL);
		if (copy==map) {
			return this;
		}
		return new TTTSet<>(copy);
	}

	public TTTSet<E> delete(E e) {
		TTTree<E, Object> copy = map.remove(e);
		if (copy==map) {
			return this;
		}
		return new TTTSet<>(copy);
	}

	public void dump() {
		map.dump();
	}

}
