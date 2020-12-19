package fp.yeyu.terrain.plancsterraintweak.mixin.client;

import fp.yeyu.terrain.plancsterraintweak.TerrainFeature;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenReturnToMenuMixin {

	private static final Logger LOGGER = LogManager.getLogger("GameMenuScreenReturnToMenuMixin");

	@SuppressWarnings("UnresolvedMixinReference")
	@Inject(method = "method_19836", at = @At("TAIL"))
	private void onReturnToMenu(ButtonWidget widget, CallbackInfo ci) {
		TerrainFeature.resetNoiseGenerator();
		LOGGER.info("Static noise generators have been reset.");
	}
}
