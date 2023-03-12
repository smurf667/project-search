package de.engehausen.maven.projectsearch;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Loads predefined queries from {@code #PROPERTIES}.
 */
public class DefaultPresets implements PresetProvider {

	/** name of resource with the presets */
	public static final String PROPERTIES = "/presets.properties";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> get() {
		final Properties presets = new Properties();
		try {
			presets.load(DefaultPresets.class.getResourceAsStream(PROPERTIES));
		} catch (IOException e) {
			return Map.of();
		}
		return presets
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
				entry -> entry.getKey().toString(),
				entry -> entry.getValue().toString(),
				(x, y) -> y, 
				() -> new HashMap<String, String>())
			);
	}
}
