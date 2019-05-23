import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Runner {
	public static final String LEVEL = "LEVEL";
	public static final String DEBUG = "DEBUG";
	public static final String GUI = "GUI";
	
	private static final String CURRENT_DIRECTORY;
	private static final String DEBUG_STRING;
	static {
		File currentDirFile = new File(".");
		CURRENT_DIRECTORY = currentDirFile.getAbsolutePath();
		DEBUG_STRING = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,quiet=y,address=8000 ";
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Map<String, String> options = loadOptions("lib/runner.conf");
		Pattern pattern = Pattern.compile("\\<([a-zA-Z0-9]*)\\>");
		for (int i = 0; i < args.length; i++) {
			Matcher matcher = pattern.matcher(args[i]);
			if (matcher.find()) {
				options.put(matcher.group(1), args[++i]);
			}
		}
		args = new String[] {"java",
				"-jar", arg("lib/server.jar"),
				"-c", "java -Duser.dir=" + arg("bin ") + (Boolean.parseBoolean(options.get(DEBUG)) ? DEBUG_STRING : "") + "Client",
				"-l", arg("complevels/" + options.get(LEVEL)),
				Boolean.parseBoolean(options.get(GUI)) ? "-g" : "-p",
		};
		ProcessBuilder pb = new ProcessBuilder(args).inheritIO();
		Process p = pb.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				super.run();
				p.descendants().forEach(x -> x.destroy());
				p.destroy();
			}
		});
		p.waitFor();
	}

	private static String arg(String arg) {
		return CURRENT_DIRECTORY.replace(".", arg);
	}
	
	private static Map<String, String> loadOptions(String path) throws FileNotFoundException {
		Map<String, String> options = new HashMap<>();
		Scanner scanner = new Scanner(new File(arg(path)));
		Pattern pattern = Pattern.compile("\\<([a-zA-Z0-9]*)\\>\\s*([a-zA-Z0-9\\.]*)");
		while (scanner.hasNext()) {
			String option = scanner.nextLine();
			Matcher matcher = pattern.matcher(option);
			if (matcher.find()) {
				String key = matcher.group(1);
				String value = matcher.group(2);
				options.put(key, value);
			}
		}
		scanner.close();
		return options;
	}

}
