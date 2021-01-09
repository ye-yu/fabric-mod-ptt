package fp.yeyu.terrain.plancsterraintweak.mixin;

import fp.yeyu.terrain.plancsterraintweak.ServerInstance;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "startServer", at = @At("RETURN"))
	private static <S extends MinecraftServer> void onStartServer(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir) {
		final S returnValue = cir.getReturnValue();
		ServerInstance.setInstance(returnValue);
	}
}
