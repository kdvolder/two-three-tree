package com.github.kdvolder.tttree.test;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.github.kdvolder.tttree.TTTMap;

public class TTTMapTest extends AbstractMapTestTemplate {


	@Test
	public void noUnneededCopiesOnInsert() {
		MutableMap<Integer, String> map = createEmptyMap();
		map.put(123, "123");

		Map<Integer, String> before = map.asImmutableMap();
		map.put(123, "123");
		Map<Integer, String> after = map.asImmutableMap();
		assertTrue(before==after);
	}

	@Test
	public void frozenData() {
		MutableMap<Integer, String> map = createEmptyMap();

		put(150, "150");
		put(426, "426");

		Map<Integer, String> before = map.asImmutableMap();
		put(426, "426");
		Map<Integer, String> after = map.asImmutableMap();

		assertTrue(before==after);
	}

	@Test
	public void noUnneededCopiesOnInsertAndRemove() {
		Integer[] data = randomInts(1000, 900);
		MutableMap<Integer, String> map = createEmptyMap();
		for (Integer k : data) {
			map.put(k, ""+k);

			Map<Integer, String> before = map.asImmutableMap();
			map.put(k, ""+k);
			Map<Integer, String> after = map.asImmutableMap();
			assertTrue(before==after);
		}

		for (Integer k : data) {
			map.remove(k);

			Map<Integer, String> before = map.asImmutableMap();
			map.remove(k);
			Map<Integer, String> after = map.asImmutableMap();

			assertTrue(before==after);
		}
	}

	@Override
	protected <K extends Comparable<K>, V> MutableMap<K, V> createEmptyMap() {
		return MutableMap.from(new TTTMap<K,V>());
	}

}
