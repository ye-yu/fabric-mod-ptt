package fp.yeyu.terrain.plancsterraintweak;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.stream.IntStream;

public final class LayeredUndergroundPlacer {

	public static final LayeredUndergroundPlacer GRANITE_PLACER = new LayeredUndergroundPlacer(0, 6, Blocks.GRANITE, Distribution.GRANITE, null);
	public static final LayeredUndergroundPlacer DIORITE_PLACER = new LayeredUndergroundPlacer(36, 40, Blocks.DIORITE, Distribution.DIORITE_UPWARDS, Distribution.DIORITE_DOWNWARDS);

	public final int lowerHeight;
	public final int upperHeight;
	public final Block block;

	public final Distribution primaryDistribution;
	@Nullable
	public final Distribution secondaryDistribution;

	public LayeredUndergroundPlacer(int lowerHeight, int upperHeight, Block block, Distribution primaryDistribution, @Nullable Distribution secondaryDistribution) {
		this.lowerHeight = lowerHeight;
		this.upperHeight = upperHeight;
		this.block = block;
		this.primaryDistribution = primaryDistribution;
		this.secondaryDistribution = secondaryDistribution;
	}

	public void place(Chunk chunk, BlockPos.Mutable pos, Random random) {
		IntStream.range(lowerHeight, upperHeight + 1).forEach(i -> {
			pos.setY(i);
			final BlockState blockState = chunk.getBlockState(pos);
			if (blockState.getBlock() == Blocks.STONE) {
				chunk.setBlockState(pos, block.getDefaultState(), false);
			}
		});

		primaryDistribution.place(chunk, pos, block, random);

		if (secondaryDistribution != null) secondaryDistribution.place(chunk, pos, block, random);
	}


	public static abstract class Distribution {
		public static final Distribution GRANITE = new Distribution(6, 3) {
			private final double[] rollFactors = new double[]{
					0.8, 0.55, 0.2
			};

			@Override
			public boolean sample(int factor, Random random) {
				final int centerDelta = getCenterDelta(factor);
				if (centerDelta >= delta) return false;
				return random.nextFloat() < rollFactors[centerDelta];
			}
		};
		public static final Distribution DIORITE_UPWARDS = new Distribution(40, 3) {
			private final double[] rollFactors = new double[]{
					0.8, 0.55, 0.2
			};

			@Override
			public boolean sample(int factor, Random random) {
				final int centerDelta = getCenterDelta(factor);
				if (centerDelta >= delta) return false;
				return random.nextFloat() < rollFactors[centerDelta];
			}
		};
		public static final Distribution DIORITE_DOWNWARDS = new Distribution(36, -3) {
			private final double[] rollFactors = new double[]{
					0.8, 0.55, 0.2
			};

			@Override
			public boolean sample(int factor, Random random) {
				final int centerDelta = Math.abs(getCenterDelta(factor));
				if (centerDelta >= rollFactors.length) return false;
				return random.nextFloat() < rollFactors[centerDelta];
			}
		};
		public final int center;
		public final int delta;

		protected Distribution(int center, int delta) {
			this.center = center;
			this.delta = delta;
		}

		public abstract boolean sample(int factor, Random random);

		public final int getCenterDelta(int from) {
			return from - center;
		}

		public final void place(Chunk chunk, BlockPos.Mutable pos, Block block, Random random) {
			final BlockState state = block.getDefaultState();
			final int from = Math.min(center, center + delta);
			final int to = Math.max(center, center + delta);
			IntStream.range(from, to).forEach(i -> {
				pos.setY(i);
				final BlockState blockState = chunk.getBlockState(pos);
				if (blockState.getBlock() == Blocks.STONE && sample(i, random)) {
					chunk.setBlockState(pos, state, false);
				}
			});
		}
	}
}
