package com.github.kdvolder.tttree.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public abstract class AbstractMapTestTemplate {

	protected boolean NOISY = false;
	protected void println(String string) {
		if (NOISY) System.out.println(string);
	}

	protected MutableMap<Integer, String> testMap = createEmptyMap();
	protected Map<Integer, String> shadowMap = new HashMap<>();

	protected Map<String, Long> measurements = new HashMap<>();

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

	protected abstract <K extends Comparable<K>, V> MutableMap<K, V> createEmptyMap();

	protected void assertTreeData(Map<Integer, String> shadowMap, MutableMap<Integer, String> tree) {
		assertEquals(shadowMap.keySet(), tree.keySet());
		for (Integer k : shadowMap.keySet()) {
			assertEquals(shadowMap.get(k), tree.get(k));
		}
	}

	protected void shuffle(Integer[] data) {
		for (int i = 0; i < data.length; i++) {
			int j = random.nextInt(data.length);
			Integer bucket = data[i];
			data[i] = data[j];
			data[j] = bucket;
		}
	}

	protected void remove(Integer key) {
		println("remove("+key+");");
		testMap.remove(key);
		shadowMap.remove(key);
		println("=====================");
		if (NOISY) testMap.dump();
		assertTreeData(shadowMap, testMap);
	}

	protected void put(int key, String val) {
		println("put("+key+", \""+val+"\");");
		testMap.put(key, val);
		shadowMap.put(key, val);
		println("=====================");
		if (NOISY) testMap.dump();
		assertTreeData(shadowMap, testMap);
	}

	@Test
	public void emptySetIterator() {
		MutableMap<String, String> map = createEmptyMap();
		Iterator<Entry<String, String>> iter = map.iterator();
		assertFalse(iter.hasNext());
	}

	@Test
	public void singletonIterator() {
		MutableMap<String,String> tree = createEmptyMap();
		tree.put("Hello", "World");
		Iterator<Entry<String, String>> iter = tree.iterator();

		assertTrue(iter.hasNext());

		Entry<String, String> element = iter.next();
		assertEquals("Hello", element.getKey());
		assertEquals("World", element.getValue());

		assertFalse(iter.hasNext());
	}

	@Test
	public void mapWithOneEntry() {
		MutableMap<String, String> map = createEmptyMap();
		map.put("Hello", "World");
		assertEquals(ImmutableSet.of("Hello"), map.keySet());
		assertEquals("World", map.get("Hello"));
		assertNull(map.get("not-present-key"));
	}

	@Test
	public void singletonKeyset() {
		MutableMap<String,String> map = createEmptyMap();
		map.put("Hello", "World");

		Set<String> keys = map.keySet();
		assertTrue(keys.contains("Hello"));
		assertEquals(1, keys.size());

		assertEquals(ImmutableSet.of("Hello"), keys);
	}

	@Test
	public void testRandomDataInsertions() {
		//Test the TTTree by filling it with random data
		//and comparing its behavior with a 'shadowMap' which is a plain old java.util.HashMap

		final int ITERATIONS = 1000;
		final int DATA_RANGE = 900;
		// choose DATA_RANGE smaller than ITERATIONS, this ensures we are also testing some 'collision'
		// where old map value is overwritten with a new one.
		for (int i = 0; i < ITERATIONS; i++) {
			int key = random.nextInt(DATA_RANGE);
			String val = ""+random.nextInt(DATA_RANGE);
			put(key, val);
		}
		assertTreeData(shadowMap, testMap);
	}

	@Test
	public void testRandomDataDeletions() {
		//Test the TTTree by filling it with random data
		//and comparing its behavior with a 'shadowMap' which is a plain old java.util.HashMap

		for (int i = 0; i < 10; i++) {
			final int ITERATIONS = 100;
			final int DATA_RANGE = 90;

			Integer[] keys = new Integer[ITERATIONS];
			for (int j = 0; j < ITERATIONS; j++) {
				Integer key = keys[j] = random.nextInt(DATA_RANGE);
				String val = ""+random.nextInt(DATA_RANGE);
				put(key, val);
			}
			shuffle(keys);
			for (int j = 0; j < ITERATIONS; j++) {
				Integer key = keys[j];
				remove(key);
				//TODO: maintain this invariant to save memory
				//checkForRedundantInternalKeys(tree);
			}
			assertTrue(testMap.isEmpty());
		}
	}

	@Test
	@Ignore
	public void performanceTest() {
		int WORKLOAD_SIZE = 100_000;
		int MAP_SIZES[] = { 1, 10, 100, 1_000 /*, 10_000*/ };
		for (int MAP_SIZE : MAP_SIZES) {
			int ITERATIONS = WORKLOAD_SIZE / MAP_SIZE;
			assertEquals(ITERATIONS*MAP_SIZE,WORKLOAD_SIZE);
			doNoisy(() -> {
				println("===== Map SIZE = "+MAP_SIZE);
				MutableMap<Integer, Integer> map = createEmptyMap();
				doPerformanceTest(map, ITERATIONS, MAP_SIZE);
			});
		}
	}

	private void doPerformanceTest(MutableMap<Integer, Integer> map, int ITERATIONS, int MAP_SIZE) {
		Integer[] keys = randomInts(MAP_SIZE);
		for (int iteration = 0; iteration < ITERATIONS; iteration++) {
			measure(iteration, "inserting", () -> {
				for (Integer key : keys) {
					map.put(key, key);
				}
			});
			measure(iteration, "accessing", () -> {
				for (Integer key : keys) {
					map.get(key);
				}
			});
			measure(iteration, "removing ", () -> {
				for (Integer key : keys) {
					map.remove(key);
				}
			});
		}
		summarizeMeasurements();
	}

	@Test
	@Ignore
	public void performanceAccessingTest() {
		int WORKLOAD_SIZE = 100_000_000;
		int MAP_SIZES[] = { 1, 10, 100, 1_000, 10_000, 100_000 };
		for (int MAP_SIZE : MAP_SIZES) {
			Integer[] keys = randomInts(MAP_SIZE);
			int ITERATIONS = WORKLOAD_SIZE / MAP_SIZE;
			assertEquals(ITERATIONS*MAP_SIZE,WORKLOAD_SIZE);
			doNoisy(() -> {
				println("===== TTTree SIZE = "+MAP_SIZE);
				MutableMap<Integer, Integer> map = createEmptyMap();
				for (Integer key : keys) {
					map.put(key, key);
				}
				doAccessTest(keys, map, ITERATIONS, MAP_SIZE);
			});
		}
	}

	private void doAccessTest(Integer[] keys, MutableMap<Integer, Integer> map, int ITERATIONS, int MAP_SIZE) {
		measure(0, "accessing", () -> {
			for (int iteration = 0; iteration < ITERATIONS; iteration++) {
				for (Integer key : keys) {
					map.get(key);
				}
			}
		});
		summarizeMeasurements();
	}

	private void doNoisy(Runnable body) {
		boolean wasNoisy = NOISY;
		NOISY = true;
		try {
			body.run();
		} finally {
			NOISY = wasNoisy;
		}
	}

	private void summarizeMeasurements() {
//		println("========= performance test summary ======");
		long totalTime = 0;
		for (String type : measurements.keySet()) {
			Long time = 0L;
			println(type +  ": " + seconds(time = measurements.get(type)));
			totalTime += time;
		}
		if (measurements.keySet().size()>1) {
			println("----------------------------------------");
			println("total : "+seconds(totalTime));
		}
	}

	private void measure(int iter, String testType, Runnable body) {
//		println(">>> "+iter+":"+testType+" ...");
		long start = System.currentTimeMillis();
		body.run();
		long duration = System.currentTimeMillis()-start;
//		println(">>> "+iter+":"+testType+" took "+seconds(duration));
		if (iter==0) {
			measurements.put(testType, duration);
		} else {
			measurements.put(testType, measurements.get(testType)+duration);
		}
	}

	private String seconds(long duration) {
		return String.format("%.3f", duration/1000.0)+" s";
	}

	private Integer[] randomInts(int howMany) {
		Integer[] data = new Integer[howMany];
		for (int i = 0; i < data.length; i++) {
			data[i] = random.nextInt();
		}
		return data;
	}

//	public static void checkForRedundantInternalKeys(TTTree<Integer,String> tree) {
//		//Look for internal keys that are not at the same time also used in a leaf.
//		//These keys are redundant in the sense that they could be replaced by a existing leaf's key
//		//this might save memory (the key can then be garbage collected)
//		class MyVisitor extends TTTreeVisitor<Integer, String> {
//			Set<Integer> internalKeys = new HashSet<>();
//			Set<Integer> externalKeys = new HashSet<>();
//			@Override
//			public void visit_internal_key(Integer k) {
//				internalKeys.add(k);
//			}
//			@Override
//			public void visit_leaf(Integer k, String v) {
//				externalKeys.add(k);
//			}
//			public void checkConstraint() {
//				if (externalKeys.containsAll(internalKeys)) {
//					return;
//				}
//				//There are some internalkeys that don't correspond to a key used in a leaf!
//				internalKeys.removeAll(externalKeys);
//				throw new IllegalStateException("Redundant internal keys found: "+internalKeys);
//			}
//		};
//
//		MyVisitor visitor = new MyVisitor();
//		tree.accept(visitor);
//		visitor.checkConstraint();
//	}

}
