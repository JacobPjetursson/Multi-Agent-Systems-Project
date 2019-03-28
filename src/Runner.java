import java.io.File;
import java.io.IOException;

public class Runner {
	
	public static final String LEVEL = "MANoConflict.lvl";
	public static final boolean DEBUG = false;
	public static final boolean GUI = true;
	
	private static final String CURRENT_DIRECTORY;
	private static final String DEBUG_STRING;
	static {
		File currentDirFile = new File(".");
		CURRENT_DIRECTORY = currentDirFile.getAbsolutePath();
		DEBUG_STRING = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,quiet=y,address=8000 ";
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		args = new String[] {"java", 
				"-jar", arg("lib/server.jar"),
				"-c", "java -Duser.dir=" + arg("bin ") + (DEBUG ? DEBUG_STRING : "") + "Client",
				"-l", arg("levels/" + LEVEL),
				GUI ? "-g" : "-p",
		};
		ProcessBuilder pb = new ProcessBuilder(args).inheritIO();
		Process p = pb.start();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				p.destroy();
			}
		}));
		p.waitFor();
	}
	
	private static String arg(String arg) {
		return CURRENT_DIRECTORY.replace(".", arg);
	}

}
