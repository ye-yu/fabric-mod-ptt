package fp.yeyu.terrain.plancsterraintweak.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fp.yeyu.terrain.plancsterraintweak.Command;
import fp.yeyu.terrain.plancsterraintweak.TerrainGenerationType;
import fp.yeyu.terrain.plancsterraintweak.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public final class PlancsTerrainTweakClient implements ClientModInitializer {
	private static final Logger LOGGER = LogManager.getLogger("PTT-Client");
	private static final CustomGameRuleCategory PTT_RULE = new CustomGameRuleCategory(new Identifier("ptt", "ptt_rule"), new TranslatableText("ptt.gamerule.title"));
	private static final GameRules.Type<GameRules.BooleanRule> BIOMORE = GameRuleFactory.createBooleanRule(true);
	private static final GameRules.Type<GameRules.BooleanRule> LAYERED_CAVE = GameRuleFactory.createBooleanRule(true);
	private static final GameRules.Type<GameRules.BooleanRule> RIVER = GameRuleFactory.createBooleanRule(true);
	private static final GameRules.Type<GameRules.BooleanRule> NETHER_HIGHWAY = GameRuleFactory.createBooleanRule(true);
	private static final GameRules.Type<GameRules.BooleanRule> THE_END_PATH = GameRuleFactory.createBooleanRule(true);
	private static final GameRules.Type<EnumRule<TerrainGenerationType>> TERRAIN_GENERATION_TYPE = GameRuleFactory.createEnumRule(TerrainGenerationType.PERLIN);

	@Nullable
	private static volatile GameRules.Key<GameRules.BooleanRule> pttBiomore = null;

	@Nullable
	private static volatile GameRules.Key<GameRules.BooleanRule> pttLayeredCave = null;

	@Nullable
	private static volatile GameRules.Key<GameRules.BooleanRule> pttRiver = null;

	@Nullable
	private static volatile GameRules.Key<GameRules.BooleanRule> pttNetherHighway = null;

	@Nullable
	private static volatile GameRules.Key<GameRules.BooleanRule> pttTheEndPath = null;

	@Nullable
	private static volatile GameRules.Key<EnumRule<TerrainGenerationType>> pttTerrainType = null;

	@NotNull
	public static GameRules.Key<GameRules.BooleanRule> getPttBiomore() {
		return Objects.requireNonNull(pttBiomore);
	}

	@NotNull
	public static GameRules.Key<GameRules.BooleanRule> getPttLayeredCave() {
		return Objects.requireNonNull(pttLayeredCave);
	}

	@NotNull
	public static GameRules.Key<GameRules.BooleanRule> getPttRiver() {
		return Objects.requireNonNull(pttRiver);
	}

	@NotNull
	public static GameRules.Key<EnumRule<TerrainGenerationType>> getPttTerrainType() {
		return Objects.requireNonNull(pttTerrainType);
	}

	@NotNull
	public static GameRules.Key<GameRules.BooleanRule> getPttNetherHighway() {
		return Objects.requireNonNull(pttNetherHighway);
	}

	@NotNull
	public static GameRules.Key<GameRules.BooleanRule> getPttTheEndPath() {
		return Objects.requireNonNull(pttTheEndPath);
	}

	@Override
	public void onInitializeClient() {
		LOGGER.info("Setting up game rules...");
		pttBiomore = GameRuleRegistry.register("pttBiomore", PTT_RULE, BIOMORE);
		pttLayeredCave = GameRuleRegistry.register("pttLayeredCave", PTT_RULE, LAYERED_CAVE);
		pttRiver = GameRuleRegistry.register("pttRiver", PTT_RULE, RIVER);
		pttNetherHighway = GameRuleRegistry.register("pttNetherHighway", PTT_RULE, NETHER_HIGHWAY);
		pttTheEndPath = GameRuleRegistry.register("pttTheEndPath", PTT_RULE, THE_END_PATH);
		pttTerrainType = GameRuleRegistry.register("pttTerrainType", PTT_RULE, TERRAIN_GENERATION_TYPE);

		CommandRegistrationCallback.EVENT.register(((commandDispatcher, isDedicated) -> commandDispatcher.register(CommandManager.literal("pttexport").executes(this::onPttExport))));
	}

	public static final SimpleCommandExceptionType COMMAND_EXCEPTION_TYPE = new SimpleCommandExceptionType(new TranslatableText("ptt.export.error"));

	public int onPttExport(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final MinecraftServer server = context.getSource().getMinecraftServer();
		final Config config = Config.getConfig();
		config.initialize(server);
		try {
			config.save();
		} catch (IOException e) {
			LOGGER.error(e);
			throw COMMAND_EXCEPTION_TYPE.create();
		}

		context.getSource().sendFeedback(new TranslatableText("ptt.export.successful"), false);
		return 1;
	}
}
