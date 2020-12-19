package fp.yeyu.terrain.plancsterraintweak.distribution;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.IntStream;

public enum Quadratic {
	THREE(3), FIVE(5), SEVEN(7), ELEVEN(11);

	public final int radius;
	private final HashMap<Integer, Double> refDist2D = new HashMap<>();
	private final HashMap<Integer, Double> refDist3D = new HashMap<>();

	Quadratic(int radius) {
		this.radius = radius;
		int r = 2 * radius + 1;

		final double max = radius * radius;
		final Iterator<Double> distIterator = IntStream
				.range(1, radius + 1)
				.mapToDouble(i -> i * i)
				.map(i -> i / max)
				.iterator();

		final ArrayList<Double> dist = Lists.newArrayList(distIterator);
		Collections.reverse(dist);

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < r; j++) {
				final int iDiff = i - radius;
				final int jDiff = j - radius;
				final int distance = iDiff * iDiff + jDiff * jDiff;
				final int distIndex = (int) (Math.sqrt(distance));
				if (distIndex >= dist.size()) continue;
				this.refDist2D.put(distance, dist.get(distIndex));
			}
		}

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < r; j++) {
				for (int k = 0; k < r; k++) {
					final int iDiff = i - radius;
					final int jDiff = j - radius;
					final int kDiff = k - radius;
					final int distance = iDiff * iDiff + jDiff * jDiff + kDiff * kDiff;
					final int distIndex = (int) (Math.sqrt(distance));
					if (distIndex >= dist.size()) continue;
					this.refDist3D.put(distance, dist.get(distIndex));
				}
			}
		}
	}

	public static Quadratic of(int radius) {
		switch (radius) {
			case 3:
				return THREE;
			case 5:
				return FIVE;
			case 7:
				return SEVEN;
			case 11:
				return ELEVEN;
			default:
				throw new IllegalArgumentException("Radius of " + radius + " is not available");
		}
	}

	public boolean sample2DDistance(int distance, Random random) {
		final Double dist = refDist2D.getOrDefault(distance, null);
		if (dist == null) return false;
		return random.nextFloat() < dist;
	}

	public boolean sample3DDistance(int distance, Random random) {
		final Double dist = refDist3D.getOrDefault(distance, null);
		if (dist == null) return false;
		return random.nextFloat() < dist;
	}
}
