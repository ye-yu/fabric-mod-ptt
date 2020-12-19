package fp.yeyu.terrain.plancsterraintweak;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.function.BiFunction;

public final class Command {

	public static final SimpleCommandExceptionType COMMAND_EXCEPTION_TYPE = new SimpleCommandExceptionType(new TranslatableText("ptt.command.error"));

	public static void registerCommand(String command, BiFunction<CommandContext<ServerCommandSource>, Boolean, Integer> callback) {
		CommandRegistrationCallback.EVENT.register(((commandDispatcher, isDedicated) -> commandDispatcher.register(CommandManager.literal(command).executes(context -> callback.apply(context, isDedicated)))));
	}
}
