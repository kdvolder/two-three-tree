package com.github.kdvolder.tttree.test;

import java.util.Random;

public class RandomTestUtils {

	/**
	 * To generate 'random' test data. We fix the seed however so that each test runs the
	 * same test data, so its not really 'random'.
	 */
	protected Random random = new Random(12555);

	protected Integer[] randomInts(int howMany, int bound) {
		Integer[] data = new Integer[howMany];
		for (int i = 0; i < data.length; i++) {
			data[i] = random.nextInt(bound);
		}
		return data;
	}
}
