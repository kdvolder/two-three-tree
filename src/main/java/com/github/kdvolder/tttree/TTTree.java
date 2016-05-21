package com.github.kdvolder.tttree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.github.kdvolder.util.Assert;

public abstract class TTTree<K extends Comparable<K>, V> {
	
	////////////////////////////////////
	// public api
	////////////////////////////////////

	@SuppressWarnings("unchecked")
	public static <K extends Comparable<K>, V> TTTree<K, V> empty() {
		return EMPTY_TREE;
	}

	public abstract TTTree<K, V> put(K k, V v);
	public abstract V get(K k);
	
	public Set<K> keySet() {
		//TODO: implement this without actually dumping all the keys into a newly created set.
		// The TTTree itself should be able to be used as data structure to represent the set
		HashSet<K> keys = new HashSet<>();
		collectKeys(keys);
		return keys;
	}

	/**
	 * For debugging. Dump tree structure in indented format onto sysout
	 */
	public void dump() {
		dump(0);
	}

	
	//TODO: public abstract TTTree<K, V> remove(K k); 
	
	/////////////////////////////////////
	// implementation 
	/////////////////////////////////////

	//Some possible optimization to consider in future:
	
	// 1) Can we avoid storing/computing 'depth' in each node?
	// It is nice to have the depth for now to:
	//   - execute some asserts to make sure the code doesn't break the 'equal depths' invariant
	//   - easily determine if a tree has grown.
	// However it should not be necessary to track the depth. All that would be needed is for a
	// internal 'put' operation to somehow return a boolean alongside the resulting tree to
	// indicate whether the new tree's depth has grown.

	abstract void dump(int indent);
	
	protected void print(int indent, Object msg) {
		for (int i = 0; i < indent; i++) {
			System.out.print("  ");
		}
		System.out.println(msg);
	}

	protected abstract void collectKeys(Collection<K> keys);

	protected abstract int depth();

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static final TTTree EMPTY_TREE = new TTTree() {
		@Override public TTTree put(Comparable k, Object v) { return leaf(k, v);}
		@Override protected int depth() { return 0; }
		@Override public Object get(Comparable k) { return null; }
		@Override public String toString() { return "EMPTY"; }
		@Override protected void collectKeys(Collection keys) {}
		@Override void dump(int indent) {print(indent, this);}
	};

	/**
	 * Create a LEAF node which contains a single key -> value pair.
	 */
	private static <K extends Comparable<K>, V> TTTree<K, V> leaf(K k, V v) {
		return new Leaf<K,V>(k, v);
	}
	
	private static class Leaf<K extends Comparable<K>, V> extends TTTree<K, V> {
		
		private final K k;
		private final V v;
		
		Leaf(K k, V v) {
			super();
			this.k = k;
			this.v = v;
		}

		@Override
		public TTTree<K, V> put(K ik, V iv) {
			int compare = ik.compareTo(k);
			TTTree<K, V> newLeaf = leaf(ik, iv);
			if (compare==0) {
				// ik == k
				return newLeaf;
			} else if (compare<0) {
				// ik < k
				return new Node2<>(newLeaf, ik, this);
			} else {
				// ik > k
				return new Node2<>(this, k, newLeaf);
			}
		}

		@Override
		public V get(K fk) {
			if (k.equals(fk)) {
				return v;
			}
			return null;
		}

		@Override
		protected int depth() {
			//Depth should be the same as that of empty tree, because
			//Empty tree is also a kind of leaf node.
			return 0;
		}

		@Override
		protected void collectKeys(Collection<K> keys) {
			keys.add(k);
		}

		@Override
		void dump(int indent) {
			print(indent, k + " = " +v);
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
		public V get(K fk) {
			int c = fk.compareTo(k);
			if (c<=0) {
				// fk <= k
				return l.get(fk);
			} else {
				// fk > k
				return r.get(fk);
			}
		}

		@Override
		protected int depth() {
			return depth;
		}
		@Override
		protected void collectKeys(Collection<K> keys) {
			l.collectKeys(keys);
			r.collectKeys(keys);
		}
		@Override
		void dump(int indent) {
			l.dump(indent+1);
			print(indent, k);
			r.dump(indent+1);
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
		public V get(K k) {
			int c = k.compareTo(k1);
			if (c<=0) {
				//k <= k1
				return l.get(k);
			} else {
				// k1 < k
				c = k.compareTo(k2);
				if (c<=0) {
					//k1 < k <= k2 
					return m.get(k);
				} else {
					//k2 < k
					return r.get(k);
				}
			}
		}

		@Override
		protected int depth() {
			return depth;
		}

		@Override
		protected void collectKeys(Collection<K> keys) {
			r.collectKeys(keys);
			m.collectKeys(keys);
			l.collectKeys(keys);
		}

		@Override
		void dump(int indent) {
			l.dump(indent+1);
			print(indent, k1);
			m.dump(indent+1);
			print(indent, k2);
			r.dump(indent+1);
		}
	}

}
