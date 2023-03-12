package de.engehausen.maven.projectsearch;

import java.util.Map;
import java.util.function.Supplier;

/** Service interface which can provide predefined queries. */
public interface PresetProvider extends Supplier<Map<String,String>> {}
