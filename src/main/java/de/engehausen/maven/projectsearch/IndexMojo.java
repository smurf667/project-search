package de.engehausen.maven.projectsearch;

import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Mojo to create the search index.
 * The other mojos use an existing index, or create one (with default settings)
 * if none exists. For better index control, this mojo can be run separately.
 */
@Mojo(name = Constants.INDEX_NAME, requiresProject = false)
public class IndexMojo extends AbstractSearchMojo {

	@Parameter(name = Constants.PARAM_IGNORE_FOLDERS, property = Constants.PARAM_IGNORE_FOLDERS, defaultValue = Constants.DEFAULT_IGNORE_FOLDERS, required = false)
	protected String ignoreFolders;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException {
		final Path indexPath = getIndex(root, indexFolder);
		final Indexer indexer = new Indexer(
			rootFolder,
			indexPath,
			Indexer.createPathMatchers(indexFolder, ignoreFolders),
			ignoreMimeTypes,
			getLog()
		);
		indexer.index(clean);
	}
}