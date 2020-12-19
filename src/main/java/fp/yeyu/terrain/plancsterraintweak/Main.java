package fp.yeyu.terrain.plancsterraintweak;

import fp.yeyu.terrain.plancsterraintweak.config.Config;

import java.io.File;
import java.io.IOException;

public final class Main {

	public static void main(String[] args) throws IOException {
		System.out.println("Initializing configuration file...\n");
		System.out.println("Note: If you are running this mod on");
		System.out.println("a client side, you do not have to run");
		System.out.println("this. See game rules or run /ptt");
		System.out.println("instead.");

		Config.getConfig().save();
	}
}
