package com.github.kdvolder.tttree.test;

import org.junit.Test;

import com.github.kdvolder.tttree.TTTSet;

public class TTTSetTest {

	protected <E extends Comparable<E>> MutableSet<E> createEmptySet() {
		return MutableSet.from(new TTTSet<E>());
	}

	@Test
	public void emptySet() {
		s = createEmptySet();
	}

}
