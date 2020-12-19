package fp.yeyu.terrain.plancsterraintweak;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WorldView {

	public static final Logger LOGGER = LogManager.getLogger();

	public static boolean getTopNonAirPosition(StructureWorldAccess world, BlockPos.Mutable topPos, int fromY, boolean doAbove) {
		for (int i = fromY; i >= 0; i--) {
			topPos.setY(i);
			if (world.getBlockState(topPos).isAir()) continue;
			if (doAbove) {
				topPos.setY(i + 1);
			}
			return true;
		}
		LOGGER.warn("Reached to the bottom of the world while searching for a non-air block.");
		topPos.setY(0);
		return false;
	}
}
