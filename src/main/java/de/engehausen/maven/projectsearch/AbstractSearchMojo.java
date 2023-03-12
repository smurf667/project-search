package de.engehausen.maven.projectsearch;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Base for all mojos.
 */
public abstract class AbstractSearchMojo extends AbstractMojo {

	/**
	 * The folder to use for storing the index.
	 */
	@Parameter(name = Constants.PARAM_INDEX_FOLDER, property = Constants.PARAM_INDEX_FOLDER, defaultValue = Constants.DEFAULT_INDEX, required = false)
	protected String indexFolder;

	/**
	 * The root folder where indexing starts. If not specified the current folder is used.
	 */
	@Parameter(name = Constants.PARAM_ROOT, property = Constants.PARAM_ROOT, defaultValue = Constants.DEFAULT_ROOT, required = false)
	protected String root;

	/**
	 * Regular expression for MIME types to ignore when indexing.
	 */
	@Parameter(name = Constants.PARAM_IGNORE_MIME_TYPES, property = Constants.PARAM_IGNORE_MIME_TYPES, defaultValue = Constants.DEFAULT_IGNORE_MIME_TYPES, required = false)
	protected String ignoreMimeTypes;

	/**
	 * Flag to force rebuilding the index.
	 */
	@Parameter(name = Constants.PARAM_CLEAN, property = Constants.PARAM_CLEAN, defaultValue = "false", required = false)
	protected boolean clean;

	protected Path rootFolder;

	protected Path getIndex(final String root, final String name) {
		rootFolder = Paths.get(root != null ? root : Constants.DEFAULT_ROOT);
		return rootFolder.resolve(name);
	}
}