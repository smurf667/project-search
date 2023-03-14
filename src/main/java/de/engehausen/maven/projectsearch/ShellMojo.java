package de.engehausen.maven.projectsearch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Mojo to perform interactive search.
 */
@Mojo(name = Constants.SHELL_NAME, requiresProject = false)
public class ShellMojo extends AbstractSearchMojo {

	private static final int MAX = 16;
	private static final String COMMAND_HELP = "?help";
	private static final String COMMAND_QUIT = "?quit";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			final Path indexPath = getIndex(root, indexFolder);
			if (clean || !Files.exists(indexPath)) {
				final Indexer indexer = new Indexer(
					rootFolder,
					indexPath,
					Indexer.createPathMatchers(indexFolder, Constants.DEFAULT_IGNORE_FOLDERS),
					ignoreMimeTypes,
					getLog()
				);
				indexer.index(clean);
			}
			final Directory directory = FSDirectory.open(indexPath);
			final Analyzer analyzer = new CaseSensitiveAnalyzer();
			final IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
			final Path rootFolderAbsolute = rootFolder.toAbsolutePath();
			try (final Scanner scanner = new Scanner(System.in)) {
				help();
				while (scanner.hasNext()) {
					final String query = scanner.nextLine();
					if (COMMAND_QUIT.equals(query)) {
						break;
					} else if (COMMAND_HELP.equals(query)) {
						help();
						continue;
					}
					try {
						final TopDocs topDocs = searcher.search(new QueryParser(Constants.FIELD_CONTENTS, analyzer).parse(query), MAX);
						final List<String> hits = Arrays.stream(topDocs.scoreDocs)
							.map(scoreDoc -> {
								try {
									return searcher.storedFields().document(scoreDoc.doc, Constants.RESULT_FIELDS);
								} catch (IOException e) {
									return null;
								}
							})
							.filter(Objects::nonNull)
							.map(document -> rootFolderAbsolute.resolve(document.get(Constants.FIELD_PATH)).resolve(document.get(Constants.FIELD_FILENAME)).toString())
							.sorted()
							.collect(Collectors.toList());
						if (hits.isEmpty()) {
							getLog().info("No results.");
						} else {
							getLog().info("Found:");
							hits.forEach(result -> getLog().info(String.format("  %s", result)));
							if (hits.size() == MAX) {
								getLog().info(String.format("There may be more results (limited to %d)", MAX));
							}
						}
						getLog().info("");
					} catch (ParseException e) {
						getLog().error(e.getMessage());
					}
				}
				directory.close();
			}
		} catch (IOException e) {
			throw new MojoExecutionException("I/O error", e);
		}
	}

	protected void help() {
		getLog().info("Interactive search. Use Lucene Query Syntax: https://lucene.apache.org/core/2_9_4/queryparsersyntax.html");
		getLog().info(String.format("Default search is for content, further namespaces: %s", Constants.RESULT_FIELDS));
		getLog().info("Example for searching filenames: filename:test*");
		getLog().info("");
		getLog().info(String.format("Enter query, or type '%s' or '%s'", COMMAND_QUIT, COMMAND_HELP));
	}
}