package fp.yeyu.terrain.plancsterraintweak.config;

import fp.yeyu.terrain.plancsterraintweak.PlancsTerrainTweak;
import fp.yeyu.terrain.plancsterraintweak.client.PlancsTerrainTweakClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class Config {

	private static final Path savePath;
	private static final String FILENAME = "ptt-config.properties";
	@Nullable
	private static volatile Config config = null;

	static {
		Path savePathTemp = null;

		try {
			final Class<?> fabric = Config.class.getClassLoader().loadClass("net.fabricmc.loader.api.FabricLoader");
			final Object fabricInstance = fabric.getDeclaredMethod("getInstance").invoke(null);
			final Path configDir = (Path) fabricInstance.getClass().getDeclaredMethod("getConfigDir").invoke(fabricInstance);
			savePathTemp = configDir.resolve(FILENAME);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			savePathTemp = Paths.get("..", "config");
			final File file = savePathTemp.toFile();
			if (!file.exists())
				//noinspection ResultOfMethodCallIgnored
				file.mkdir();

			savePathTemp = savePathTemp.resolve(FILENAME);
		} finally {
			savePath = savePathTemp;
		}

	}

	private boolean biomore = true;
	private boolean layeredCave = true;
	private boolean river = true;
	private boolean netherHighway = true;
	private boolean theEndPath = true;

	public static boolean doBiomore(MinecraftServer server) {
		final EnvType environmentType = FabricLoader.getInstance().getEnvironmentType();
		if (environmentType == EnvType.CLIENT) {
			final GameRules.Key<GameRules.BooleanRule> pttBiomore = PlancsTerrainTweakClient.getPttBiomore();
			return server.getGameRules().getBoolean(pttBiomore);
		} else {
			return getConfig().biomore;
		}
	}

	public static boolean doLayeredCave(MinecraftServer server) {
		final EnvType environmentType = FabricLoader.getInstance().getEnvironmentType();
		if (environmentType == EnvType.CLIENT) {
			final GameRules.Key<GameRules.BooleanRule> pttBiomore = PlancsTerrainTweakClient.getPttLayeredCave();
			return server.getGameRules().getBoolean(pttBiomore);
		} else {
			return getConfig().layeredCave;
		}
	}

	public static boolean doRiver(MinecraftServer server) {
		final EnvType environmentType = FabricLoader.getInstance().getEnvironmentType();
		if (environmentType == EnvType.CLIENT) {
			final GameRules.Key<GameRules.BooleanRule> pttBiomore = PlancsTerrainTweakClient.getPttRiver();
			return server.getGameRules().getBoolean(pttBiomore);
		} else {
			return getConfig().river;
		}
	}

	public static boolean doNetherHighway(MinecraftServer server) {
		final EnvType environmentType = FabricLoader.getInstance().getEnvironmentType();
		if (environmentType == EnvType.CLIENT) {
			final GameRules.Key<GameRules.BooleanRule> pttBiomore = PlancsTerrainTweakClient.getPttNetherHighway();
			return server.getGameRules().getBoolean(pttBiomore);
		} else {
			return getConfig().netherHighway;
		}
	}

	public static boolean doTheEndPath(MinecraftServer server) {
		final EnvType environmentType = FabricLoader.getInstance().getEnvironmentType();
		if (environmentType == EnvType.CLIENT) {
			final GameRules.Key<GameRules.BooleanRule> pttBiomore = PlancsTerrainTweakClient.getPttTheEndPath();
			return server.getGameRules().getBoolean(pttBiomore);
		} else {
			return getConfig().theEndPath;
		}
	}

	public static void loadConfig() throws IOException {
		final Properties properties = new Properties();
		if (savePath.toFile().exists()) {
			properties.load(new FileInputStream(savePath.toFile()));
		} else {
			PlancsTerrainTweak.LOGGER.error("Cannot find the configuration file. Using default settings instead.");
		}
		final Config config = getConfig();
		config.biomore = getBooleanProperty(properties, "biomore", config.biomore);
		config.layeredCave = getBooleanProperty(properties, "layeredCave", config.layeredCave);
		config.river = getBooleanProperty(properties, "river", config.river);
		config.netherHighway = getBooleanProperty(properties, "netherHighway", config.theEndPath);
		config.theEndPath = getBooleanProperty(properties, "theEndPath", config.theEndPath);
	}

	private static boolean getBooleanProperty(Properties properties, String key, boolean defaults) {
		return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaults)));
	}

	@NotNull
	public static Config getConfig() {
		final Config config = Config.config;
		if (config == null) {
			Config.config = new Config();
			return getConfig();
		}
		return config;
	}

	public void initialize(MinecraftServer server) {
		biomore = doBiomore(server);
		layeredCave = doLayeredCave(server);
		river = doRiver(server);
		netherHighway = doNetherHighway(server);
		theEndPath = doTheEndPath(server);
	}

	public void save() throws IOException {
		save(savePath.toFile());
	}

	private void save(File path) throws IOException {
		if (path.exists()) {
			System.err.println("Error: File already exists.");
			return;
		}
		final Properties properties = new Properties();
		properties.setProperty("biomore", String.valueOf(biomore));
		properties.setProperty("layeredCave", String.valueOf(layeredCave));
		properties.setProperty("river", String.valueOf(river));
		properties.setProperty("netherHighway", String.valueOf(netherHighway));
		properties.setProperty("theEndPath", String.valueOf(theEndPath));
		properties.store(new FileWriter(path), "Planc_'s Terrain Tweak Configuration File");
	}
}
