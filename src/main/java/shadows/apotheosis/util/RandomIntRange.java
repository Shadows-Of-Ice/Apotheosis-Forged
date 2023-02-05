package shadows.apotheosis.util;

import net.minecraft.util.RandomSource;

/**
 * A class that helps to generate random values within a range.
 */
public class RandomIntRange {

	public static final RandomIntRange ZERO = new RandomIntRange(0, 1) {
		@Override
		public int generateInt(RandomSource rand) {
			return 0;
		}
	};

	private final int min;
	private final int max;

	/**
	 * Creates a range.
	 * @param min Min value, inclusive.
	 * @param max Max value, exclusive.
	 */
	public RandomIntRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return this.min;
	}

	public int getMax() {
		return this.max;
	}

	public int generateInt(RandomSource rand) {
		return this.min + rand.nextInt(Math.max(1, this.max - this.min));
	}
}