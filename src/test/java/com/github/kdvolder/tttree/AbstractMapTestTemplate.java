package com.github.kdvolder.tttree;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractMapTestTemplate {

	protected boolean NOISY = false;
	protected void println(String string) {
		if (NOISY) System.out.println(string);
	}

	/**
	 * To generate 'random' test data. We fix the seed however so that each test runs the
	 * same test data, so its not really 'random'.
	 */
	protected Random random = new Random(12555);

	@Test
	public void emptySetHasNoKeys() {
		MutableMap<String, String> tree = createEmptyMap();
		assertEquals(ImmutableSet.of(), tree.keySet());
	}

	protected abstract MutableMap<String, String> createEmptyMap();
}
