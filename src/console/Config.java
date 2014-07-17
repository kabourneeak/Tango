package console;

/**
 * Represents an object that contains Configuration information
 * 
 * @author Gregory Bint
 * 
 */
public interface Config {

	/**
	 * Insert or replace the named entry with the supplied value.
	 * 
	 * @param name
	 *            the name of the configuration entry.
	 * @param value
	 *            the value to set
	 */
	public void addConfig(String name, String value);

	/**
	 * Indicates whether a configuration entry for the given key exists.
	 * 
	 * @param key
	 *            the key to search for
	 * @return true if there is a configuration entry by that name
	 */
	public boolean hasConfig(String key);

	/**
	 * Gets a configuration setting from the settings file whose key matches the
	 * given key.
	 * 
	 * @param key
	 *            the name of the configuration entry to return
	 * @return the matching configuration entry
	 * @throws ItemNotFoundException
	 *             if the key does not exist
	 */
	public String getConfig(String key) throws ItemNotFoundException;

}
