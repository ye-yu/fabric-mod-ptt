package fp.yeyu.terrain.plancsterraintweak.mixin;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PerlinNoiseSampler.class)
public class PerlinNoiseSamplerMixin {

	@Inject(method = "sample(IIIDDDDDD)D", at = @At("RETURN"), cancellable = true)
	private void onSample(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX, double fadeLocalY, double fadeLocalZ, CallbackInfoReturnable<Double> cir) {
		cir.setReturnValue(cir.getReturnValue() * 0.5);
	}
}
