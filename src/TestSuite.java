import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
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
		
		int[] results = new int[3];
		for (File level : levels) {
			args = new String[] {"java", "-cp", bin.getPath(), "Runner", 
					"<DEBUG>", "false",
					"<GUI>", "false",
					"<LEVEL>", level.getName()
				};
			ProcessBuilder pb = new ProcessBuilder(args);
			//pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			Process p = pb.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					p.destroy();
				}
			}));
			
			long time = 2;
			TimeUnit unit = TimeUnit.SECONDS;
			int status = p.waitFor(time, unit) ? 0 : 1;
			while (input.ready()) {
				String line = input.readLine();
				System.out.println(line);
				if (line.contains("Level solved: No")) {
					status = 2;
				}
			}
			input.close();
			Thread.sleep(1);
			
			results[status]++;
			switch (status) {
			case 0:
				System.out.println("[finish]");
				System.out.println();
				break;
			case 1:
				System.err.println("[timeout] After " + time + " " + unit.name().toLowerCase());
				System.err.println();
				break;
			case 2:
				System.err.println("[fail]");
				System.err.println();
				break;
			}
		}
		System.out.println("*** Result ***");
		System.out.println("Finished: " + results[0]);
		System.out.println("Failed: " + results[2]);
		System.out.println("Timeout: " + results[1]);
		System.out.println("Total: " + (results[0] + results[1] + results[2]));
	}

}
