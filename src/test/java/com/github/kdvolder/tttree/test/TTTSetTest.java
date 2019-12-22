package com.github.kdvolder.tttree.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.github.kdvolder.tttree.TTTSet;
import com.google.common.collect.ImmutableSet;

public class TTTSetTest extends RandomTestUtils {


	MutableSet<Integer> set = createEmptySet();
	Set<Integer> shadowSet = new TreeSet<>();

	protected <E extends Comparable<E>> MutableSet<E> createEmptySet() {
		return MutableSet.from(new TTTSet<E>());
	}

	@Test
	public void union() {
		TTTSet<String> empty = TTTSet.of();
		TTTSet<String> stuff = TTTSet.of("abc", "def");

		assertEquals(empty.union(empty), empty);
		assertEquals(empty.union(empty), TTTSet.<String>of());

		assertEquals(stuff.union(stuff), stuff);
		assertEquals(empty.union(stuff), stuff);
		assertEquals(stuff.union(empty), stuff);

		assertEquals(stuff.union(TTTSet.of("more", "stuff", "here")), TTTSet.of("abc", "def", "more", "stuff", "here"));
		assertEquals(TTTSet.of("more", "stuff", "here").union(stuff), TTTSet.of("abc", "def", "more", "stuff", "here"));
	}

	@Test
	public void intersection() {
		TTTSet<String> empty = TTTSet.of();

		TTTSet<String> seta = TTTSet.of("a", "b", "c", "d");
		TTTSet<String> setb = TTTSet.of("b", "d", "f");

		assertEquals(empty.intersection(empty), empty);
		assertEquals(empty.intersection(empty), TTTSet.<String>of());

		assertEquals(seta.intersection(seta), seta);
		assertEquals(setb.intersection(setb), setb);

		assertEquals(seta.intersection(setb), TTTSet.of("b", "d"));
		assertEquals(setb.intersection(seta), TTTSet.of("b", "d"));
	}

	@Test
	public void emptySet() {
		MutableSet<String> set = createEmptySet();
		assertEquals(0, set.size());
		assertTrue(set.isEmpty());
		assertEquals(ImmutableSet.of(), set.asImmutableSet());
		assertFalse(set.iterator().hasNext());
	}

	@Test
	public void singletonSet() {
		MutableSet<String> set = createEmptySet();
		set.add("Something");
		assertEquals(1, set.size());
		assertFalse(set.isEmpty());
		assertEquals(ImmutableSet.of("Something"), set.asImmutableSet());
		assertEquals(ImmutableSet.of("Something"), ImmutableSet.copyOf(set.asImmutableSet()));
	}

	@Test
	public void noUnneededCopyOnInsert() {
		MutableSet<String> set = createEmptySet();
		set.add("Hello");

		Set<String> before = set.asImmutableSet();
		set.add("Hello");
		assertTrue(before==set.asImmutableSet());
	}

	@Test
	public void randomOperations() {
		int DATA_RANGE = 90;
		int DATA_SIZE = 100; //Size > range ensures some duplicates
		Integer[] data = randomInts(DATA_SIZE, DATA_RANGE);

		for (Integer d : data) {
			add(d);
		}

		for (Integer d : data) {
			remove(d);
		}

	}

	@Test(timeout=100)
	public void performanceTest() {
		int DATA_RANGE = 9999;
		int DATA_SIZE = 10000; //Size > range ensures some duplicates
		Integer[] data = randomInts(DATA_SIZE, DATA_RANGE);

		MutableSet<Integer> set = createEmptySet();
		for (Integer d : data) {
			set.add(d);
		}
		for (Integer d : data) {
			set.remove(d);
		}
	}

	private void add(Integer d) {
		set.add(d);
		shadowSet.add(d);
		assertEqualSets(shadowSet, set);
	}

	private void remove(Integer d) {
		set.remove(d);
		shadowSet.remove(d);
		assertEqualSets(shadowSet, set);
	}


	private void assertEqualSets(Set<Integer> expectedSet, MutableSet<Integer> actualSet) {
		assertEquals(shadowSet, set.asImmutableSet());
		assertEquals(set.asImmutableSet(), shadowSet);
		assertEquals(expectedSet.size(), actualSet.size());
		assertEquals(expectedSet.isEmpty(), actualSet.isEmpty());
		for (Integer d : actualSet) {
			assertTrue(expectedSet.contains(d));
		}
		for (Integer d : expectedSet) {
			assertTrue(actualSet.contains(d));
		}
	}


}
