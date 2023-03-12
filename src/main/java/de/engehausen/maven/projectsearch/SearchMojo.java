package de.engehausen.maven.projectsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Mojo to perform a single search.
 */
@Mojo(name = Constants.SEARCH_NAME, requiresProject = false)
public class SearchMojo extends AbstractSearchMojo {

	private static final int MAX = 256;

	private static final Map<Constants.FailCondition, Function<List<String>, Boolean>> FAIL_CONDITIONS = Map.of(
		Constants.FailCondition.hit, hits -> Boolean.valueOf(!hits.isEmpty()),
		Constants.FailCondition.miss, hits -> Boolean.valueOf(hits.isEmpty())
	);

	/**
	 * The search query according to the <a href="https://lucene.apache.org/core/2_9_4/queryparsersyntax.html">Lucene Query Syntax</a>.
	 * See {@link Constants#ALL_FIELDS} for all index fields; {@link Constants#FIELD_CONTENTS} is the default field.
	 * The query parameter is mandatory.
	 * <p>If the query starts with {@code preset:}, then a predfined query with the name following the prefix and provided by
	 * {@link PresetProvider} instances is used.</p>
	 */
	@Parameter(name = Constants.PARAM_QUERY, property = Constants.PARAM_QUERY, required = true)
	protected String query;

	/**
	 * Comma-separated regular expressions for whitelisting search results.
	 */
	@Parameter(name = Constants.PARAM_WHITELIST, property = Constants.PARAM_WHITELIST, required = false)
	protected String whitelist;

	/**
	 * Flag to indicate if the goal should fail if search hits occur. Defaults to {@code never}.
	 */
	@Parameter(name = Constants.PARAM_FAIL_ON, property = Constants.PARAM_FAIL_ON, required = false)
	protected String failOn = Constants.FailCondition.never.name();

	protected List<Pattern> patterns;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		final Constants.FailCondition condition = Optional.ofNullable(failOn)
			.map(Constants.FailCondition::valueOf)
			.orElse(Constants.FailCondition.never);
		if (whitelist != null) {
			patterns = Arrays.stream(whitelist.split(","))
				.map(Pattern::compile)
				.collect(Collectors.toList());
		} else {
			patterns = List.of();
		}
		try {
			if (query.startsWith(Constants.PRESET_PREFIX)) {
				loadPresetQuery();
			}
			final Path indexPath = getIndex(root, indexFolder);
			if (!Files.exists(indexPath)) {
				final Indexer indexer = new Indexer(
					rootFolder,
					indexPath,
					Indexer.createPathMatchers(indexFolder, Constants.DEFAULT_IGNORE_FOLDERS),
					ignoreMimeTypes,
					getLog()
				);
				indexer.index(clean);
			}
			try (final Directory directory = FSDirectory.open(indexPath)) {
				final IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
				final TopDocs topDocs = searcher.search(new QueryParser(Constants.FIELD_CONTENTS, new CaseSensitiveAnalyzer()).parse(query), MAX);
				final Deque<String> ignored = new ArrayDeque<>();
				final String rootFolderAbsolute = rootFolder.toAbsolutePath().toString();
				final List<String> hits = Arrays.stream(topDocs.scoreDocs)
					.map(scoreDoc -> {
						try {
							return searcher.storedFields().document(scoreDoc.doc, Constants.RESULT_FIELDS);
						} catch (IOException e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.map(document -> String.format("%s%s%s%s%s", rootFolderAbsolute, File.separatorChar, document.get(Constants.FIELD_PATH), File.separatorChar, document.get(Constants.FIELD_FILENAME)))
					.filter(str -> accepted(str, ignored))
					.sorted()
					.collect(Collectors.toList());
				if (hits.isEmpty()) {
					getLog().info("Nothing found.");
				} else {
					getLog().info("Found:");
					hits.forEach(str -> getLog().info(String.format("  %s", str)));
				}
				if (!ignored.isEmpty()) {
					getLog().info("");
					getLog().info("The following results were ignored:");
					ignored
						.stream()
						.sorted()
						.forEach(str -> getLog().info(String.format("  %s", str)));
				}
				if (FAIL_CONDITIONS.getOrDefault(condition, all -> Boolean.FALSE).apply(hits).booleanValue()) {
					throw new MojoFailureException("Unwanted search results.");
				}
			}
		} catch (IOException|ParseException e) {
			throw new MojoExecutionException("Execution error", e);
		}
	}

	protected void loadPresetQuery() throws MojoExecutionException {
		final Map<String, String> presets = new HashMap<>();
		ServiceLoader
			.load(PresetProvider.class)
			.forEach(provider -> presets.putAll(provider.get()));
		final String key = query.substring(Constants.PRESET_PREFIX.length());
		query = presets.get(key);
		if (query == null) {
			throw new MojoExecutionException(String.format("Unknown preset: %s", key));
		}
	}

	protected boolean accepted(final String str, final Deque<String> ignored) {
		if (patterns
			.stream()
			.anyMatch(pattern -> pattern.matcher(str).find())) {
			ignored.push(str);
			return false;
		}
		return true;
	}
}
