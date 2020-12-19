package fp.yeyu.terrain.plancsterraintweak;

import com.mojang.brigadier.context.CommandContext;
import fp.yeyu.terrain.plancsterraintweak.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static fp.yeyu.terrain.plancsterraintweak.Command.registerCommand;

public final class PlancsTerrainTweak implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("PTT");

	private static int onPttCommand(final CommandContext<ServerCommandSource> context, final boolean isDedicated) {
		context.getSource().sendFeedback(new LiteralText("§6§lPlanc_'s Terrain Tweak§r\n"), false);
		final MinecraftServer server = context.getSource().getMinecraftServer();
		final boolean doBiomore = Config.doBiomore(server);
		final boolean doLayeredCave = Config.doLayeredCave(server);
		final boolean doRiver = Config.doRiver(server);
		final boolean doNetherHighway = Config.doNetherHighway(server);
		final boolean doTheEndPath = Config.doTheEndPath(server);

		context.getSource().sendFeedback(new LiteralText("   BiomOre: " + formatBoolean(doBiomore) + "§r"), false);
		context.getSource().sendFeedback(new LiteralText("   Layered Cave: " + formatBoolean(doLayeredCave) + "§r"), false);
		context.getSource().sendFeedback(new LiteralText("   River: " + formatBoolean(doRiver) + "§r"), false);
		context.getSource().sendFeedback(new LiteralText("   Nether Highway: " + formatBoolean(doNetherHighway) + "§r"), false);
		context.getSource().sendFeedback(new LiteralText("   The End Path: " + formatBoolean(doTheEndPath) + "§r"), false);
		context.getSource().sendFeedback(new LiteralText("   Terrain: " + formatBoolean(false) + "§r"), false);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) return 1;

		context.getSource().sendFeedback(new LiteralText("\n" +
				"§6§lINFO:§r You are running on the client side. You will have access to §n/pttexport§r to export your current PTT" +
				" settings for your server.§r"), false);
		return 1;
	}

	private static String formatBoolean(boolean b) {
		return (b ? "§2" : "§c") + "§l" + (b ? "TRUE" : "FALSE") + "§r";
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Registering PTT commands...");
		registerCommand("ptt", PlancsTerrainTweak::onPttCommand);

		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) return;

		try {
			Config.loadConfig();
		} catch (IOException e) {
			LOGGER.fatal(e);
			throw new RuntimeException("Cannot load PTT configuration file.");
		}
	}
}
