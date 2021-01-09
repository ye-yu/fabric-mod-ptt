package fp.yeyu.terrain.plancsterraintweak;

import com.google.common.collect.Lists;
import fp.yeyu.terrain.plancsterraintweak.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.util.math.noise.OctaveSimplexNoiseSampler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public final class TerrainFeature {
	private static final double THRESHOLD_L = 0;
	private static final double THRESHOLD_U = 0.04;
	private static final double FREQ = 0.1;
	private static final Block[] SUBSTITUTE_TYPE = new Block[]{
			Blocks.GRASS_BLOCK,
			Blocks.GRASS_BLOCK,
			Blocks.SAND,
			Blocks.WATER,
			Blocks.WATER,
			Blocks.WATER,
			Blocks.WATER,
			Blocks.WATER
	};
	@Nullable
	public static volatile OctavePerlinNoiseSampler riverSampler = null;
	@Nullable
	public static volatile OctaveSimplexNoiseSampler highwaySampler1 = null;
	@Nullable
	public static volatile OctaveSimplexNoiseSampler highwaySampler2 = null;
	@Nullable
	public static volatile OctaveSimplexNoiseSampler theEndSampler = null;
	@Nullable
	public static volatile ChunkRandom chunkRandom = null;

	public static void buildRiver(final long seed, final Chunk chunk, Function<BlockPos, Biome> biomeAccessor) {
		final MinecraftServer server = ServerInstance.getInstance();
		if (!Config.doRiver(server)) return;

		if (chunkRandom == null) {
			chunkRandom = new ChunkRandom(seed);
		}

		if (riverSampler == null) {
			riverSampler = new OctavePerlinNoiseSampler(getChunkRandom(), Lists.newArrayList(-5, -3));
		}

		final int startX = chunk.getPos().getStartX();
		final int startZ = chunk.getPos().getStartZ();

		BlockPos.Mutable pos = new BlockPos.Mutable();
		final BlockPos.Mutable absolutePos = chunk.getPos().getStartPos().mutableCopy();
		final int absX = absolutePos.getX();
		final int absZ = absolutePos.getZ();

		for (int localX = 0; localX < 16; ++localX) {
			for (int localZ = 0; localZ < 16; ++localZ) {
				pos.setX(localX);
				pos.setZ(localZ);


				if (Config.doLayeredCave(server))
					addCaveLayers(chunk, pos, getChunkRandom());


				final int x = localX + startX;
				final int z = localZ + startZ;
				final int level = sampleRiverLevel(x, z);
				if (level > -1) {
					final int localY = Math.min(chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, localX, localZ), 70);
					for (int i = 0; i < Math.min(3, level); i++) {
						pos.setY(localY + i);
						chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
					}

					final int yToDig = Math.min(3, localY - 62);
					final int waterDepth = level - 3;

					for (int i = 0; i < yToDig; i++) {
						pos.setY(localY - i);
						final BlockState block = chunk.getBlockState(pos);
						absolutePos.set(localX + absX, 0, localZ + absZ);
						final Biome.Category category = biomeAccessor.apply(absolutePos).getCategory();
						if (category == Biome.Category.OCEAN || category == Biome.Category.RIVER) continue;
						chunk.setBlockState(pos, Blocks.AIR.getDefaultState(), false);
					}

					if (waterDepth > 0) {
						for (int i = 0; i < waterDepth; i++) {
							pos.setY(localY - yToDig - i);
							chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
						}
					} else {
						pos.setY(localY - yToDig);

						absolutePos.set(localX + absX, 0, localZ + absZ);
						final Biome.Category category = biomeAccessor.apply(absolutePos).getCategory();
						if (category != Biome.Category.OCEAN && category != Biome.Category.RIVER) {
							chunk.setBlockState(pos, SUBSTITUTE_TYPE[level].getDefaultState(), false);
						} else {
							chunk.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
						}
					}
				}
			}
		}
	}

	public static void buildHighway(final long seed, final Chunk chunk) {
		if (!Config.doNetherHighway(ServerInstance.getInstance())) return;

		if (highwaySampler1 == null) {
			highwaySampler1 = new OctaveSimplexNoiseSampler(new ChunkRandom(seed - 8), Lists.newArrayList(-1));
		}

		if (highwaySampler2 == null) {
			highwaySampler2 = new OctaveSimplexNoiseSampler(new ChunkRandom(seed + 8), Lists.newArrayList(-1));
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

	public static void buildTheEndPath(final long seed, final Chunk chunk) {
		if (!Config.doTheEndPath(ServerInstance.getInstance())) return;
		final Random random = ServerInstance.getInstance().getWorlds().iterator().next().random;

		if (theEndSampler == null) {
			theEndSampler = new OctaveSimplexNoiseSampler(new ChunkRandom(seed - 8), Lists.newArrayList(-1));
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

					final boolean shroomLight = random.nextFloat() < 0.002;
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
		final double sample = Objects.requireNonNull(riverSampler).sample(x * FREQ, z * FREQ, 0, 0);
		return !(sample > THRESHOLD_U) && !(sample < THRESHOLD_L);
	}

	public static int sampleRiverLevel(int x, int z) {
		final double sample = Objects.requireNonNull(riverSampler).sample(x * FREQ, z * FREQ, 0, 0);
		if (!(sample > THRESHOLD_U) && !(sample < THRESHOLD_L)) {
			return 7 - Math.min(7, Math.abs((int) ((sample - 0.02) * 500)));
		}
		return -1;
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

	private static ChunkRandom getChunkRandom() {
		final ChunkRandom chunkRandom = TerrainFeature.chunkRandom;
		if (chunkRandom == null) throw new NullPointerException();
		return chunkRandom;
	}
}
