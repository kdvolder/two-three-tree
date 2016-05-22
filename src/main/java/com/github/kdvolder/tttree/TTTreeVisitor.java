package com.github.kdvolder.tttree;

public class TTTreeVisitor<K extends Comparable<K>, V> {

	/**
	 * Visit empty tree.
	 */
	public void visit_empty() {}

	/**
	 * Visit a leaf
	 */
	public void visit_leaf(K k, V v) {}

	public void visit_2node(TTTree<K, V> l, K k, TTTree<K, V> r) {
		enter_2node(l, k, r);
		l.accept(this);
		r.accept(this);
		exit_2node(l, k, r);
	}

	/**
	 * Visit a 3 node
	 */
	public void visit_3node(TTTree<K, V> l, K k1, TTTree<K, V> m, K k2, TTTree<K, V> r) {
		enter_3node(l, k1, m, k2, r);
		l.accept(this);
		visit_internal_key(k1);
		m.accept(this);
		visit_internal_key(k2);
		r.accept(this);
		exit_3node(l, k1, m, k2, r);
	}

	public void visit_internal_key(K k) {}
	public void exit_3node(TTTree<K, V> l, K k1, TTTree<K, V> m, K k2, TTTree<K, V> r) {}
	public void enter_3node(TTTree<K, V> l, K k1, TTTree<K, V> m, K k2, TTTree<K, V> r) {}
	public void enter_2node(TTTree<K, V> l, K k, TTTree<K, V> r) {}
	public void exit_2node(TTTree<K, V> l, K k, TTTree<K, V> r) {}
}
