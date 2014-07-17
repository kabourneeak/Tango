package console;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The main startup class. Handles bringing up the application into a running
 * state based on the configuration file supplied on the command line
 * 
 * @author Gregory Bint
 * 
 */
public class StatMain {
	private static final Logger log = Logger.getLogger(StatMain.class);

	private static String _configFile;

	public static void main(String[] args) {
		parseCommandLine(args);

		startLog4j();

		GlobalConfig.initialize(_configFile);

		StatRunner sr = new StatRunner();

		sr.start();

		log.info("Finished.");
	}

	private static void startLog4j() {
		PropertyConfigurator.configure(_configFile);
	}

	private static void parseCommandLine(String[] args) {
		if (args.length < 2 || 0 != (args.length % 2)) {
			printUsage();
			System.exit(1);
		}

		for (int i = 0; i < args.length / 2; ++i) {
			String arg = args[2 * i];
			String val = args[(2 * i) + 1];

			if ("-c".equals(arg)) {
				parseConfigFile(val);
			} else {
				printUsage();
				System.exit(2);
			}
		}
	}

	private static void parseConfigFile(String val) {
		_configFile = val;
	}

	private static void printUsage() {
		System.out.println("Usage: java -jar treestats.jar -c <config_file>");
	}

}
