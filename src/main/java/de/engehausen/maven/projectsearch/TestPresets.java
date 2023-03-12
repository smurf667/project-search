package de.engehausen.maven.projectsearch;

import java.util.Map;

/** Provider of test presets; only active if {@link #TEST_PRESETS} environment variable is set. */
public class TestPresets implements PresetProvider {

	public static final String TEST_PRESETS = "testPresets";

	@Override
	public Map<String, String> get() {
		return System.getenv(TEST_PRESETS) != null ? Map.of("test", "(@brown AND end\\:text) OR dolor") : Map.of();
	}
}
