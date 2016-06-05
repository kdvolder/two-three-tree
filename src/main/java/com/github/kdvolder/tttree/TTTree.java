package com.github.kdvolder.tttree;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import com.github.kdvolder.util.Assert;
import com.google.common.collect.Iterators;

/**
 * A 'map-like' data structure implemented by means of a Two-Three Tree.
 * <p>
 * Map-like means, it implements an interface similar to java.util.Map, but it
 * doesn't formally implement the actual java.util.Map interface.
 * <p>
 * If you don't mind the 'non-standard' interface you can use TTTree directly from
 * your code. However, if you prefer something that formally implements java.util.Map
 * you can wrap it in a {@link TTTMap} adapter.
 */
public abstract class TTTree<K extends Comparable<K>, V> implements Iterable<Map.Entry<K, V>> {

	////////////////////////////////////
	// public api
	////////////////////////////////////

	@SuppressWarnings("unchecked")
	public static <K extends Comparable<K>, V> TTTree<K, V> empty() {
		return EMPTY_TREE;
	}

	public abstract TTTree<K, V> put(K k, V v);
	public final V get(K k) {
		Leaf<K, V> e = getEntry(k);
		if (e!=null) {
			return e.getValue();
		}
		return null;
	}

	public abstract TTTree<K, V> remove(K k);

	public boolean isEmpty() {
		return false; // good default because most nodes aren't empty.
	}

	@Override
	public Iterator<Entry<K, V>> iterator() {
		if (isEmpty()) {
			Collections.emptyIterator();
		}
		return new TTTreeIterator(this);
	}

	public Set<K> keySet() {
		return new AbstractSet<K>() {
			private Integer size;

			@SuppressWarnings("unchecked")
			@Override
			public boolean contains(Object o) {
				if (o instanceof Comparable) {
					return TTTree.this.containsKey((K)o);
				}
				return false;
			}

			@Override
			public Iterator<K> iterator() {
				Iterator<Entry<K, V>> entries = TTTree.this.iterator();
				return Iterators.transform(entries, Map.Entry::getKey);
			}

			@Override
			public int size() {
				if (size==null) {
					this.size = TTTree.this.size();
				}
				return size;
			}
		};
	}

	public final boolean containsKey(K key) {
		return getEntry(key)!=null;
	}

	abstract Leaf<K, V> getEntry(K key);

	/**
	 * Counts the elements in the {@link TTTree}.
	 * <p>
	 * WARNING: not cached... so O(n) operation.
	 */
	abstract int size();

	/**
	 * For debugging. Dump tree structure in indented format onto sysout
	 */
	public void dump() {
		dump(0);
	}

	/////////////////////////////////////
	// implementation
	/////////////////////////////////////

	//Some possible optimization to consider in future:

	// 1) Can we avoid storing/computing 'depth' in each node?
	// It is nice to have the depth for now to:
	//   - execute some asserts to make sure the code doesn't break the 'equal depths' invariant
	//   - easily determine if a tree has grown.
	// However it should not be necessary to track the depth. All that would be needed is for a
	// internal 'put/remove' operation to somehow return a boolean alongside the resulting tree to
	// indicate whether the new tree's depth has changed.

	// 2) Remove 'redundant' internal keys
	//   See the method TTTreeTest.checkForRedundantInternalKeys

	@Override
	public abstract String toString();
	protected static final TTTree<?,?>[] NO_CHILDREN = {};
	abstract void dump(int indent);
	abstract TTTree<K, V>[] getChildren();
	abstract int depth();

	void print(int indent, Object msg) {
		for (int i = 0; i < indent; i++) {
			System.out.print("  ");
		}
		System.out.println(msg);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static final TTTree EMPTY_TREE = new TTTree() {
		@Override public TTTree put(Comparable k, Object v) { return leaf(k, v); }
		@Override Leaf getEntry(Comparable key) { return null; }
		@Override public String toString() { return "EMPTY"; }
		@Override public boolean isEmpty() { return true; }
		@Override TTTree[] getChildren() { return NO_CHILDREN; }
		@Override int depth() { return 0; }
		@Override int size() { return 0; }
		@Override void dump(int indent) {print(indent, this);}
		@Override public TTTree remove(Comparable k) {return this; }
		@Override public void accept(TTTreeVisitor visitor) { visitor.visit_empty(); }
	};

	/**
	 * Create a LEAF node which contains a single key -> value pair.
	 */
	private static <K extends Comparable<K>, V> TTTree<K, V> leaf(K k, V v) {
		return new Leaf<>(k, v);
	}

	private static class Leaf<K extends Comparable<K>, V> extends TTTree<K, V> implements Map.Entry<K, V> {

		private final K k;
		private final V v;

		Leaf(K k, V v) {
			super();
			this.k = k;
			this.v = v;
		}

		@Override
		Leaf<K, V> getEntry(K key) {
			if (key.equals(k)) {
				return this;
			}
			return null;
		}

		@Override
		public TTTree<K, V> put(K ik, V iv) {
			int compare = ik.compareTo(k);
			if (compare==0) {
				// ik == k
				if (Objects.equals(v, iv)) {
					return this;
				}
				return leaf(ik, iv);
			} else {
				TTTree<K, V> newLeaf = leaf(ik, iv);
				if (compare<0) {
					// ik < k
					return new Node2<>(newLeaf, ik, this);
				} else {
					// ik > k
					return new Node2<>(this, k, newLeaf);
				}
			}
		}

		@Override
		public TTTree<K, V> remove(K fk) {
			if (fk.equals(k)) {
				return empty();
			}
			return this;
		}

		@Override
		protected int depth() {
			return 1;
		}

		@Override
		void dump(int indent) {
			print(indent, k + " = " +v);
		}

		@Override
		public K getKey() {
			return k;
		}

		@Override
		public V getValue() {
			return v;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException("setValue");
		}

		@SuppressWarnings("unchecked")
		@Override
		TTTree<K, V>[] getChildren() {
			return (TTTree<K, V>[]) NO_CHILDREN;
		}

		@Override
		int size() {
			return 1;
		}

		@Override
		public String toString() {
			return "["+k+" = "+v+"]";
		}

		@Override
		public void accept(TTTreeVisitor<K, V> visitor) {
			visitor.visit_leaf(k, v);
		}
	};

	private static class Node2<K extends Comparable<K>, V> extends TTTree<K, V> {
		private final TTTree<K, V> l;
		private final K k;
		private final TTTree<K, V> r;
		private int depth;

		Node2(TTTree<K, V> l, K k, TTTree<K, V> r) {
			Assert.isLegalState(l.depth()==r.depth());
			this.depth = l.depth()+1;
			this.l = l;
			this.k = k;
			this.r = r;
		}
		@Override
		public TTTree<K, V> put(K ik, V iv) {
			int c = ik.compareTo(k);
			if (c<=0) {
				//ik <= k
				TTTree<K, V> new_l = l.put(ik, iv);
				if (new_l.depth()>l.depth()) {
					//Since the tree has just grown its root must be Node2
					K lk = ((Node2<K,V>)new_l).k;
					TTTree<K, V> ll = ((Node2<K,V>)new_l).l;
					TTTree<K, V> lr = ((Node2<K,V>)new_l).r;
					return new Node3<>(ll, lk, lr, k, r);
				} else {
					//Tree remained same size
					return new Node2<>(new_l,k,r);
				}
			} else {
				//ik > k
				TTTree<K, V> new_r = r.put(ik, iv);
				if (new_r.depth()>r.depth()) {
					//Since the tree has just grown its root must be Node2
					K rk = ((Node2<K,V>)new_r).k;
					TTTree<K, V> rl = ((Node2<K,V>)new_r).l;
					TTTree<K, V> rr = ((Node2<K,V>)new_r).r;
					return new Node3<>(l, k, rl, rk, rr);
				} else {
					//Tree remained same size
					return new Node2<>(l,k,new_r);
				}
			}
		}

		@Override
		public TTTree<K, V> remove(K fk) {
			int c = fk.compareTo(k);
			if (c<=0) {
				// fk <= k
				TTTree<K, V> l = this.l.remove(fk);
				if (this.l==l) {
					return this; //Avoid needless copying if tree is unchanged
				} else if (this.l.depth()==l.depth()) {
					return new Node2<>(l, k, r);
				} else {
					//l.depth shrunk
					if (r instanceof Node2) {
						Node2<K,V> r = (Node2<K, V>) this.r;
						return new Node3<>(l, k, r.l, r.k, r.r);
					} else if (r instanceof Node3) {
						Node3<K,V> r = (Node3<K, V>) this.r;
						return new Node2<>(
								new Node2<>(l, k, r.l),
								r.k1,
								new Node2<>(r.m, r.k2, r.r)
						);
					} else {
						Assert.isLegalState(r instanceof Leaf);
						Assert.isLegalState(l.isEmpty());
						return r;
					}
				}
			} else {
				// fk > k
				TTTree<K, V> r = this.r.remove(fk);
				if (this.r==r) {
					return this; //Avoid needless copying if tree is unchanged
				} else if (this.r.depth()==r.depth()) {
					return new Node2<>(l, k, r);
				} else {
					//r.depth shrunk
					if (l instanceof Node2) {
						Node2<K,V> l = (Node2<K, V>) this.l;
						return new Node3<>(l.l, l.k, l.r, k, r);
					} else if (l instanceof Node3) {
						Node3<K,V> l = (Node3<K, V>) this.l;
						return new Node2<>(
								new Node2<>(l.l, l.k1, l.m),
								l.k2,
								new Node2<>(l.r, k, r)
						);
					} else {
						Assert.isLegalState(l instanceof Leaf);
						Assert.isLegalState(r.isEmpty());
						return l;
					}
				}
			}
		}

		@Override
		public Leaf<K,V> getEntry(K fk) {
			int c = fk.compareTo(k);
			if (c<=0) {
				// fk <= k
				return l.getEntry(fk);
			} else {
				// fk > k
				return r.getEntry(fk);
			}
		}

		@Override
		protected int depth() {
			return depth;
		}

		@Override
		void dump(int indent) {
			l.dump(indent+1);
			print(indent, k);
			r.dump(indent+1);
		}
		@SuppressWarnings("unchecked")
		@Override
		TTTree<K, V>[] getChildren() {
			return new TTTree[] {l, r};
		}
		@Override
		int size() {
			return l.size() + r.size();
		}

		@Override
		public String toString() {
			return "Node2["+depth+"]("+k+")";
		}
		@Override
		public void accept(TTTreeVisitor<K, V> visitor) {
			visitor.visit_2node(l, k, r);
		}

	}

	private static class Node3<K extends Comparable<K>, V> extends TTTree<K, V> {

		private int depth;

		final TTTree<K, V> l;
		final K k1;
		final TTTree<K, V> m;
		final K k2;
		final TTTree<K, V> r;

		public Node3(TTTree<K, V> l, K k1, TTTree<K, V> m, K k2, TTTree<K, V> r) {
			Assert.isLegalState(l.depth()==m.depth());
			Assert.isLegalState(l.depth()==r.depth());
			this.depth = l.depth()+1;
			this.l = l;
			this.k1 = k1;
			this.m = m;
			this.k2 = k2;
			this.r = r;
		}

		@Override
		public TTTree<K, V> put(K k, V v) {
			int c = k.compareTo(k1);
			if (c<=0) {
				//k <= k1
				final TTTree<K, V> l = this.l.put(k, v);
				if (l.depth()>this.l.depth()) {
					//The tree has just grown
					//split ourself into a new Node2.
					return new Node2<>(
							l,
							k1,
							new Node2<>(m, k2, r)
					);
				} else {
					//Tree remained same size
					return new Node3<>(l, k1, m, k2, r);
				}
			} else {
				// k1 < k
				c = k.compareTo(k2);
				if (c<=0) {
					//k1 < k <= k2
					final TTTree<K, V> m = this.m.put(k, v);
					if (m.depth()>this.m.depth()) {
						//Since the tree has just grown its root *must* be Node2
						K mk = ((Node2<K,V>)m).k;
						TTTree<K, V> ml = ((Node2<K,V>)m).l;
						TTTree<K, V> mr = ((Node2<K,V>)m).r;
						return new Node2<>(
								new Node2<>(l, k1, ml),
								mk,
								new Node2<>(mr, k2, r)
						);
					} else {
						//Tree remained same size
						return new Node3<>(l, k1, m, k2, r);
					}
				} else {
					//k2 < k
					final TTTree<K, V> r = this.r.put(k, v);
					if (r.depth()>this.r.depth()) {
						//The tree has just grown
						//split ourself into a new Node2.
						return new Node2<>(
								new Node2<>(l, k1, m),
								k2,
								r
						);
					} else {
						//Tree remained same size
						return new Node3<>(l, k1, m, k2, r);
					}
				}
			}
		}

		@Override
		public TTTree<K, V> remove(K fk) {
			int c = fk.compareTo(k1);
			if (c<=0) {
				//fk <= k1
				TTTree<K, V> l = this.l.remove(fk);
				if (l==this.l) {
					return this;
				} else if (l.depth()==this.l.depth()) {
					return new Node3<>(l, k1 ,m, k2, r);
				} else {
					//shrunk l
					if (l.isEmpty()) {
						return new Node2<>(m, k2, r);
					} else if (m instanceof Node2) {
						Node2<K, V> m = (Node2<K, V>) this.m;
						return new Node2<>(
							new Node3<>(l, k1, m.l, m.k, m.r),
							k2,
							r
						);
					} else { //m instanceof Node3
						Node3<K, V> m = (Node3<K, V>) this.m;
						return new Node3<>(
							new Node2<>(l, k1, m.l),
							m.k1,
							new Node2<>(m.m, m.k2, m.r),
							k2,
							r
						);
					}
				}
			} else {
				//k1 < fk
				c = fk.compareTo(k2);
				if (c<=0) {
					//k1 < fk <= k2
					TTTree<K, V> m = this.m.remove(fk);
					if (m==this.m) {
						return this;
					} else if (m.depth()==this.m.depth()) {
						return new Node3<>(l, k1, m, k2, r);
					} else {
						//shrunk
						if (m.isEmpty()) {
							return new Node2<>(l, k1, r);
						} else if (l instanceof Node2) {
							Node2<K,V> l = (Node2<K, V>) this.l;
							return new Node2<>(
								new Node3<>(l.l, l.k, l.r, k1, m),
								k2,
								r
							);
						} else { //l instanceof Node3
							Node3<K,V> l = (Node3<K, V>) this.l;
							return new Node3<>(
								new Node2<>(l.l, l.k1, l.m),
								l.k2,
								new Node2<>(l.r, k1, m),
								k2,
								r
							);
						}
					}
				} else {
					//k2 < fk
					TTTree<K, V> r = this.r.remove(fk);
					if (r==this.r) {
						return this;
					} else if (r.depth()==this.r.depth()) {
						return new Node3<>(l, k1, m, k2, r);
					} else {
						//shrunk
						if (r.isEmpty()) {
							return new Node2<>(l, k1, m);
						} else if (m instanceof Node2) {
							Node2<K,V> m = (Node2<K,V>)this.m;
							return new Node2<>(
								l,
								k1,
								new Node3<>(m.l, m.k, m.r, k2, r)
							);
						} else { //m instanceof Node3
							Node3<K,V> m = (Node3<K, V>) this.m;
							return new Node3<>(
								l,
								k1,
								new Node2<>(m.l, m.k1, m.m),
								m.k2,
								new Node2<>(m.r, k2, r)
							);
						}
					}
				}
			}
		}

		@Override
		public Leaf<K, V> getEntry(K k) {
			int c = k.compareTo(k1);
			if (c<=0) {
				//k <= k1
				return l.getEntry(k);
			} else {
				// k1 < k
				c = k.compareTo(k2);
				if (c<=0) {
					//k1 < k <= k2
					return m.getEntry(k);
				} else {
					//k2 < k
					return r.getEntry(k);
				}
			}
		}

		@Override
		protected int depth() {
			return depth;
		}

		@Override
		void dump(int indent) {
			l.dump(indent+1);
			print(indent, k1);
			m.dump(indent+1);
			print(indent, k2);
			r.dump(indent+1);
		}

		@SuppressWarnings("unchecked")
		@Override
		TTTree<K, V>[] getChildren() {
			return new TTTree[] {l, m, r};
		}
		@Override
		int size() {
			return l.size() + m.size() + r.size();
		}

		@Override
		public String toString() {
			return "Node3["+depth+"]("+k1+", "+k2+")";
		}

		@Override
		public void accept(TTTreeVisitor<K, V> visitor) {
			visitor.visit_3node(l, k1, m, k2, r);
		}
	}

	private class TTTreeIterator implements Iterator<Entry<K, V>> {

		Stack<TTTree<K, V>> stack = new Stack<>();

		TTTreeIterator(TTTree<K, V> tree) {
			if (!tree.isEmpty())
			stack.push(tree);
		}

		@Override
		public boolean hasNext() {
			return !stack.isEmpty();
		}

		@Override
		public Entry<K, V> next() {
			while(!stack.isEmpty()) {
				TTTree<K,V> node = stack.pop();
				if (node instanceof Leaf) {
					return (Leaf<K,V>)node;
				} else {
					TTTree<K, V>[] children = node.getChildren();
					for (int i = children.length-1; i >= 0; i--) {
						TTTree<K, V> c = children[i];
						if (!c.isEmpty()) {
							stack.push(children[i]);
						}
					}
				}
			}
			throw new NoSuchElementException();
		}
	}

	/**
	 * Useful to perform various traversals / analysis on the tree.
	 */
	public abstract void accept(TTTreeVisitor<K, V> visitor);
}
