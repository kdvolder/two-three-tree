package com.github.kdvolder.tttree.test;

import com.github.kdvolder.tttree.TTTree;

public class TTTreeTest extends AbstractMapTestTemplate {

	@Override
	protected <K extends Comparable<K>, V> MutableMap<K, V> createEmptyMap() {
		return MutableMap.<K,V>from(TTTree.empty());
	}
}
