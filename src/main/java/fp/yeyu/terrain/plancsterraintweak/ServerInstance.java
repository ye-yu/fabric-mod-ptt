package fp.yeyu.terrain.plancsterraintweak;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class ServerInstance {

	private static final AtomicReference<MinecraftServer> instance = new AtomicReference<>(null);

	public static void setInstance(@NotNull MinecraftServer server) {
		instance.set(server);
	}

	@NotNull
	public static MinecraftServer getInstance() {
		final MinecraftServer minecraftServer = instance.get();
		if (minecraftServer == null) throw new NullPointerException();
		return minecraftServer;
	}
}
