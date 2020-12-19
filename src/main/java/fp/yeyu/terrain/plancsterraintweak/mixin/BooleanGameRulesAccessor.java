package fp.yeyu.terrain.plancsterraintweak.mixin;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanRule.class)
public interface BooleanGameRulesAccessor {

	@Invoker("create")
	static GameRules.Type<GameRules.BooleanRule> of(boolean initialValue) {
		return null;
	}
}
