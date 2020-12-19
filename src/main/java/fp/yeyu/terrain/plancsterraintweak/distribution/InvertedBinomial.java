package fp.yeyu.terrain.plancsterraintweak.distribution;

import java.util.HashMap;
import java.util.Random;
import java.util.stream.IntStream;

public enum InvertedBinomial {
	FIVE(5),
	SEVEN(7);

	public final int radius;
	public final int r;
	private static final double P = 0.5f;
	public final HashMap<Integer, Double> refDist2D = new HashMap<>();
	public final HashMap<Integer, Double> refDist3D = new HashMap<>();

	InvertedBinomial(int radius) {
		this.radius = radius;
		this.r = 2 * radius + 1;
		final double[] dist = new double[(this.r + 1) / 2];

		final double[] binomialDist = IntStream.range(0, r + 1)
				.mapToDouble(x -> (2 * factorial(r) * Math.pow(P, x) * Math.pow(1 - P, r - x)) /
						(factorial(x) * factorial(r - x))).toArray();

		final double m = max(binomialDist);

		for (int i = 0; i < (r + 1) / 2; i++) {
			dist[i] = 1 - binomialDist[i] / m;
		}

		for (int i = 0; i < r; i++) {
			for (int j = 0; j < r; j++) {
				final int iDiff = i - radius;
				final int jDiff = j - radius;
				final int distance = iDiff * iDiff + jDiff * jDiff;
				final int distIndex = (int) (Math.sqrt(distance));
				if (distIndex >= dist.length) continue;
				this.refDist2D.put(distance, dist[distIndex]);
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
					if (distIndex >= dist.length) continue;
					this.refDist3D.put(distance, dist[distIndex]);
				}
			}
		}

	}

	public static long factorial(final long number) {
		if (number < 2) return 1;

		long result = 1;

		for (int factor = 2; factor <= number; factor++) {
			result *= factor;
		}

		return result;
	}

	private static double max(double... number) {
		if (number.length == 1) return number[0];
		double max = Math.max(number[0], number[1]);
		for (int i = 2; i < number.length; i++) {
			max = Math.max(max, number[i]);
		}
		return max;
	}

	public boolean sampleXZ(int x, int z, Random random) {
		final int xDiff = x - radius;
		final int zDiff = z - radius;
		final int distance = xDiff * xDiff + zDiff * zDiff;
		return sample2DDistance(distance, random);
	}

	public boolean sampleXYZ(int x, int y, int z, Random random) {
		final int xDiff = x - radius;
		final int yDiff = y - radius;
		final int zDiff = z - radius;
		final int distance = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
		return sample3DDistance(distance, random);
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

	public static InvertedBinomial of(int radius) {
		switch (radius) {
			case 5: return FIVE;
			case 7: return SEVEN;
			default: throw new IllegalArgumentException("Radius of " + radius + " is not available");
		}
	}
}
