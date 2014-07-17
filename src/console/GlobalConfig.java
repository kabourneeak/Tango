package console;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Provides configuration services to the rest of the application. This
 * singleton can be initialized with values from a file and then modified as
 * needed during runtime providing a decoupled method of specifying arguments
 * between components.
 * 
 * @author Gregory Bint
 * 
 */
public class GlobalConfig implements Config {
	/*
	 * Singleton Implementation
	 */
	private static final GlobalConfig _instance = new GlobalConfig();

	/*
	 * Instance Implementation
	 */
	private Properties _properties = null;

	private GlobalConfig() {
	}

	/**
	 * @return the singleton GlobalConfig instance
	 */
	public static GlobalConfig getInstance() {
		return _instance;
	}

	/**
	 * Loads the given file into the Configuration environment. This may be
	 * called more than once, if needed, however all previous values will be
	 * lost
	 * 
	 * @param configFile
	 *            the file to load
	 */
	public static void initialize(String configFile) {
		_instance.doInit(configFile);
	}

	private void doInit(String configFile) {
		try {
			_properties = new Properties();
			_properties.load(new FileInputStream(configFile));

		} catch (Exception e) {
			throw new RuntimeException("Error reading configuration file");
		}
	}

	@Override
	public synchronized void addConfig(String name, String value) {
		_properties.put(name, value);
	}

	@Override
	public boolean hasConfig(String key) {
		return _properties.containsKey(key);
	}

	@Override
	public String getConfig(String key) throws ItemNotFoundException {
		if (!_properties.containsKey(key)) {
			throw new ItemNotFoundException("Missing Config entry " + key);
		}

		return _properties.getProperty(key);
	}
}
