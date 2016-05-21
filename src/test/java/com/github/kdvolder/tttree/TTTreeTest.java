package com.github.kdvolder.tttree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class TTTreeTest {
	
	/**
	 * To generate 'random' test data. We fix the seed however so that each test runs the 
	 * same test data, so its not really 'random'. 
	 */
	private Random random = new Random(12555);

	private TTTree<Integer, String> tree = TTTree.empty();
	private Map<Integer, String> shadowMap = new HashMap<>();
	
	@Test
	public void emptySetHasNoKeys() {
		TTTree<String, String> tree = TTTree.empty();
		assertEquals(ImmutableSet.of(), tree.keySet());
	}

	@Test
	public void treeWithOneEntry() {
		TTTree<String, String> tree = TTTree.empty();
		tree = tree.put("Hello", "World");
		assertEquals(ImmutableSet.of("Hello"), tree.keySet());
		assertEquals("World", tree.get("Hello"));
		assertNull(tree.get("not-present-key"));
	}
	
	@Test
	public void testRandomData() {
		//Test the TTTree by filling it with random data
		//and comparing its behavior with a 'shadowMap' which is a plain old java.util.HashMap
		
		final int ITERATIONS = 1000;
		final int DATA_RANGE = 900; 
		// choose DATA_RANGE smaller than ITERATIONS, this ensures we are also testing some 'collision'
		// where old map value is overwritten with a new one.
		for (int i = 0; i < ITERATIONS; i++) {
			int key = random.nextInt(DATA_RANGE);
			String val = ""+random.nextInt(DATA_RANGE);
//			System.out.println("put("+key+", \""+val+"\");");
			put(key, val);
		}
		assertTreeData(shadowMap, tree);
	}

	private void put(int key, String val) {
//		System.out.println("=====================");
		tree = tree.put(key, val);
		shadowMap.put(key, val);
//		tree.dump();
		assertTreeData(shadowMap, tree);
	}

	private void assertTreeData(Map<Integer, String> shadowMap, TTTree<Integer, String> tree) {
		assertEquals(shadowMap.keySet(), tree.keySet());
		for (Integer k : shadowMap.keySet()) {
			assertEquals(shadowMap.get(k), tree.get(k));
		}
	}

}
