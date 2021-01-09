package fp.yeyu.terrain.plancsterraintweak;

import com.google.common.collect.Lists;
import fp.yeyu.terrain.plancsterraintweak.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public final class TerrainFeature {
	private static final double THRESHOLD = 0.03;
	private static final double FROM = 0.10;
	private static final double THRESHOLD_L = FROM - THRESHOLD;
	private static final double THRESHOLD_U = FROM + THRESHOLD;
	private static final double FREQ = 0.005;
	@Nullable
	public static volatile OctaveSimplexNoiseSampler riverSampler = null;
	@Nullable
	public static volatile OctaveSimplexNoiseSampler highwaySampler1 = null;
	@Nullable
	public static volatile OctaveSimplexNoiseSampler highwaySampler2 = null;
	@Nullable
	public static volatile OctaveSimplexNoiseSampler theEndSampler = null;

	@SuppressWarnings("deprecation")
	public static void buildRiver(ChunkRegion region, Chunk chunk) {
		if (!Config.doRiver(region.toServerWorld().getServer())) return;

		if (riverSampler == null) {
			riverSampler = new OctaveSimplexNoiseSampler(new ChunkRandom(region.getSeed()), Lists.newArrayList(-1));
		}

		final int startX = chunk.getPos().getStartX();
		final int startZ = chunk.getPos().getStartZ();

		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int localX = 0; localX < 16; ++localX) {
			for (int localZ = 0; localZ < 16; ++localZ) {

				pos.setX(localX);
				pos.setZ(localZ);

				if (Config.doLayeredCave(region.toServerWorld().getServer()))
					addCaveLayers(chunk, pos, region.getRandom());


				final int x = localX + startX;
				final int z = localZ + startZ;
				if (sampleRiver(x, z)) {
					final int localY = Math.min(chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, localX, localZ), region.getSeaLevel() + 5);
					pos.setY(localY);
					final BlockState blockState = chunk.getBlockState(pos);
					if (blockState.getBlock() == Blocks.WATER) continue;

					chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
					chunk.getFluidTickScheduler().schedule(pos, Blocks.WATER.getDefaultState().getFluidState().getFluid(), 5);

					pos.setY(localY - 1);
					chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
					chunk.getFluidTickScheduler().schedule(pos, Blocks.WATER.getDefaultState().getFluidState().getFluid(), 5);

					pos.setY(localY - 2);
					chunk.setBlockState(pos, Blocks.GRAVEL.getDefaultState(), false);

					pos.setY(localY + 1);
					chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);

					pos.setY(localY + 2);
					chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void buildHighway(ChunkRegion region, Chunk chunk) {
		if (!Config.doNetherHighway(region.toServerWorld().getServer())) return;

		if (highwaySampler1 == null) {
			highwaySampler1 = new OctaveSimplexNoiseSampler(new ChunkRandom(region.getSeed() - 8), Lists.newArrayList(-1));
		}

		if (highwaySampler2 == null) {
			highwaySampler2 = new OctaveSimplexNoiseSampler(new ChunkRandom(region.getSeed() + 8), Lists.newArrayList(-1));
		}

		final int startX = chunk.getPos().getStartX();
		final int startZ = chunk.getPos().getStartZ();

		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int localX = 0; localX < 16; ++localX) {
			for (int localZ = 0; localZ < 16; ++localZ) {

				final int x = localX + startX;
				final int z = localZ + startZ;
				if (sampleHighway(x, z)) {
					final int localY = 64;

					pos.setX(localX);
					pos.setY(localY);
					pos.setZ(localZ);

					chunk.setBlockState(pos, Blocks.CAVE_AIR.getDefaultState(), false);

					pos.setY(localY - 1);
					chunk.setBlockState(pos, Blocks.CAVE_AIR.getDefaultState(), false);

					pos.setY(localY - 2);
					chunk.setBlockState(pos, Blocks.POLISHED_BLACKSTONE.getDefaultState(), false);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void buildTheEndPath(ChunkRegion region, Chunk chunk) {
		if (!Config.doTheEndPath(region.toServerWorld().getServer())) return;

		if (theEndSampler == null) {
			theEndSampler = new OctaveSimplexNoiseSampler(new ChunkRandom(region.getSeed() - 8), Lists.newArrayList(-1));
		}

		final int startX = chunk.getPos().getStartX();
		final int startZ = chunk.getPos().getStartZ();

		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int localX = 0; localX < 16; ++localX) {
			for (int localZ = 0; localZ < 16; ++localZ) {

				final int x = localX + startX;
				final int z = localZ + startZ;
				if (sampleTheEndPath(x, z)) {
					final int localY = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, localX, localZ);
					if (localY < 30) continue;

					pos.setX(localX);
					pos.setY(localY);
					pos.setZ(localZ);

					final boolean shroomLight = region.getRandom().nextFloat() < 0.002;
					chunk.setBlockState(pos, shroomLight ? Blocks.SHROOMLIGHT.getDefaultState() : Blocks.DEAD_BRAIN_CORAL_BLOCK.getDefaultState(), false);
				}
			}
		}
	}

	private static void addCaveLayers(Chunk chunk, BlockPos.Mutable pos, Random random) {
		LayeredUndergroundPlacer.GRANITE_PLACER.place(chunk, pos, random);
		LayeredUndergroundPlacer.DIORITE_PLACER.place(chunk, pos, random);
	}

	private static boolean sampleRiver(int x, int z) {
		final double sample = Objects.requireNonNull(riverSampler).sample(x * FREQ, z * FREQ, true);
		return !(sample > THRESHOLD_U) && !(sample < THRESHOLD_L);
	}

	private static boolean sampleHighway(int x, int z) {
		final double sample1 = Objects.requireNonNull(highwaySampler1).sample(x * FREQ, z * FREQ, true);
		if (!(sample1 > THRESHOLD_U) && !(sample1 < THRESHOLD_L)) return true;
		final double sample2 = Objects.requireNonNull(highwaySampler1).sample(x * FREQ, z * FREQ, true);
		return !(sample2 > THRESHOLD_U) && !(sample2 < THRESHOLD_L);
	}

	private static boolean sampleTheEndPath(int x, int z) {
		final double sample = Objects.requireNonNull(theEndSampler).sample(x * FREQ, z * FREQ, true);
		return !(sample > THRESHOLD_U) && !(sample < THRESHOLD_L);
	}

	public static boolean isRiver(int x, int z) {
		return riverSampler != null && sampleRiver(x, z);
	}

	public static void resetNoiseGenerator() {
		riverSampler = null;
		highwaySampler1 = null;
		highwaySampler2 = null;
		theEndSampler = null;
	}
}
