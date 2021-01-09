package fp.yeyu.terrain.plancsterraintweak.mixin;

import com.google.common.collect.Lists;
import fp.yeyu.terrain.plancsterraintweak.WorldView;
import fp.yeyu.terrain.plancsterraintweak.config.Config;
import fp.yeyu.terrain.plancsterraintweak.distribution.Quadratic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

@Mixin(OreFeature.class)
public class OreFeatureMixin {

	private static final HashMap<Block, Quadratic> NOISE_FN = new HashMap<>();
	private static final HashMap<Block, Integer> SPAWN_HEIGHT = new HashMap<>();
	private static final HashMap<Block, Integer> SPAWN_OFFSET = new HashMap<>();
	private static final Quadratic DEF_NOISE = Quadratic.SEVEN;
	private static final int DEF_SPAWN_HEIGHT = 120;
	private static final ArrayList<Block> ORE_BLOCKS = Lists.newArrayList(
			Blocks.COAL_ORE,
			Blocks.IRON_ORE,
			Blocks.GOLD_ORE,
			Blocks.REDSTONE_ORE,
			Blocks.LAPIS_ORE,
			Blocks.DIAMOND_ORE,
			Blocks.EMERALD_ORE
	);

	static {
		NOISE_FN.put(Blocks.COAL_ORE, Quadratic.ELEVEN);
		NOISE_FN.put(Blocks.IRON_ORE, Quadratic.FIVE);
		NOISE_FN.put(Blocks.GOLD_ORE, Quadratic.FIVE);
		NOISE_FN.put(Blocks.LAPIS_ORE, Quadratic.SEVEN);
		NOISE_FN.put(Blocks.REDSTONE_ORE, Quadratic.SEVEN);
		NOISE_FN.put(Blocks.DIAMOND_ORE, Quadratic.FIVE);
		NOISE_FN.put(Blocks.EMERALD_ORE, Quadratic.THREE);
		NOISE_FN.put(Blocks.ANDESITE, Quadratic.ELEVEN);
		NOISE_FN.put(Blocks.GRANITE, Quadratic.SEVEN);
		NOISE_FN.put(Blocks.DIORITE, Quadratic.SEVEN);

		SPAWN_HEIGHT.put(Blocks.IRON_ORE, 70);
		SPAWN_HEIGHT.put(Blocks.GOLD_ORE, 40);
		SPAWN_HEIGHT.put(Blocks.REDSTONE_ORE, 30);
		SPAWN_HEIGHT.put(Blocks.LAPIS_ORE, 30);
		SPAWN_HEIGHT.put(Blocks.DIAMOND_ORE, 20);

		SPAWN_OFFSET.put(Blocks.COAL_ORE, 50);
		SPAWN_OFFSET.put(Blocks.IRON_ORE, 40);
	}

	private static boolean isOre(Block block) {
		return ORE_BLOCKS.contains(block);
	}

	private static boolean isStone(Block block) {
		return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.DIORITE || block == Blocks.ANDESITE;
	}

	private static float getChance(Block block) {
		return isOre(block) ? 0.008f : 0.08f;
	}

	@Inject(method = "generate", at = @At("HEAD"), cancellable = true)
	private void onGenerate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos, OreFeatureConfig oreFeatureConfig, CallbackInfoReturnable<Boolean> cir) {
		if (!Config.doBiomore(world.toServerWorld().getServer())) return;
		final Biome.Category category = world.getBiome(pos).getCategory();
		if (category == Biome.Category.NETHER || category == Biome.Category.THEEND) return;

		final Block oreBlock = oreFeatureConfig.state.getBlock();
		final double chance = random.nextDouble();
		if (chance > getChance(oreBlock)) {
			cir.setReturnValue(false);
			return;
		}

		final Quadratic noiseFn = oreBlock == Blocks.COAL_ORE && random.nextBoolean() ? Quadratic.THREE : NOISE_FN.computeIfAbsent(oreBlock, block -> DEF_NOISE);
		final int radius = noiseFn.radius;
		final int spawnHeight = SPAWN_HEIGHT.computeIfAbsent(oreBlock, block -> DEF_SPAWN_HEIGHT);
		final int spawnOffset = SPAWN_OFFSET.computeIfAbsent(oreBlock, block -> 0);

		final BlockPos.Mutable topSolidPosition = pos.mutableCopy();
		final boolean topNonAirPosition = WorldView.getTopNonAirPosition(world, topSolidPosition, spawnHeight - spawnOffset, false);

		if (!topNonAirPosition) {
			cir.setReturnValue(false);
			return;
		}

		final int topY = spawnOffset + Math.max(topSolidPosition.getY() - radius, radius);

		final int centerX = topSolidPosition.getX() - radius;
		final int centerY = radius + random.nextInt(topY);
		final int centerZ = topSolidPosition.getZ() - radius;

		final CuboidBlockIterator blockIterator = new CuboidBlockIterator(centerX - radius, centerY - radius, centerZ - radius,
				centerX + radius, centerY + radius, centerZ + radius);

		final BlockPos.Mutable mutable = BlockPos.ORIGIN.mutableCopy();

		final BlockState blockState = oreFeatureConfig.state;

		while (blockIterator.step()) {
			final int currentX = blockIterator.getX() - centerX;
			final int currentY = blockIterator.getY() - centerY;
			final int currentZ = blockIterator.getZ() - centerZ;
			mutable.set(blockIterator.getX(), blockIterator.getY(), blockIterator.getZ());

			final int distance = currentX * currentX + currentY * currentY + currentZ * currentZ;
			final boolean shouldReplace = noiseFn.sample3DDistance(distance, random);
			final boolean isStone = isStone(world.getBlockState(mutable).getBlock());
			final boolean shouldHideFromAir = shouldHideFromAir(world, mutable.toImmutable());
			if (!shouldReplace || !isStone || shouldHideFromAir) continue;
			world.setBlockState(mutable, blockState, 3);
		}
		cir.setReturnValue(true);
	}

	private boolean shouldHideFromAir(StructureWorldAccess world, BlockPos pos) {
		return Stream.of(pos, pos.up(), pos.down(), pos.north(), pos.east(), pos.south(), pos.west())
				.map(world::getBlockState)
				.anyMatch(BlockState::isAir);
	}
}
