package fp.yeyu.terrain.plancsterraintweak.mixin;

import fp.yeyu.terrain.plancsterraintweak.TerrainFeature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class WorldMixin implements WorldView {

	@Override
	public Biome getBiome(BlockPos pos) {
		final Biome biome = getBiomeAccess().getBiome(pos);

		if (TerrainFeature.isRiver(pos.getX(), pos.getZ())) {
			final Biome.Category category = biome.getCategory();
			return BuiltinRegistries.BIOME.get(category == Biome.Category.ICY ? 11 : 7);
		}

		return biome;
	}
}
