import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestSuite {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		File file = new File("levels");
		File[] levels = file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".lvl");
			}
		});
		File bin = new File("bin");
		for (File level : levels) {
			args = new String[] {"java", "-cp", bin.getPath(), "Runner", 
					"<DEBUG>", "false",
					"<GUI>", "false",
					"<LEVEL>", level.getName()
				};
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process p = pb.start();
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					p.destroy();
				}
			}));
			long time = 5;
			TimeUnit unit = TimeUnit.SECONDS;
			if (p.waitFor(time, unit)) {
				System.out.println("[finished]");
				System.out.println();
			}
			else {
				System.err.println("[timeout] After " + time + " " + unit.name().toLowerCase());
				System.err.println();
			}
		}
	}

}
