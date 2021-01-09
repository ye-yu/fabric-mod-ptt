package fp.yeyu.terrain.plancsterraintweak.mixin;

import fp.yeyu.terrain.plancsterraintweak.TerrainFeature;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

	@Inject(method = "buildSurface", at = @At("RETURN"))
	private void onBuildSurface(ChunkRegion region, Chunk chunk, CallbackInfo ci) {
		final Biome biome = region.getBiome(chunk.getPos().getStartPos());
		final long seed = region.getSeed();
		if (biome.getCategory() == Biome.Category.NETHER) TerrainFeature.buildHighway(seed, chunk);
		else if (biome.getCategory() == Biome.Category.THEEND) TerrainFeature.buildTheEndPath(seed, chunk);
		else TerrainFeature.buildRiver(seed, chunk, region::getBiome);
	}
}
