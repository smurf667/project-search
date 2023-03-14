package de.engehausen.maven.projectsearch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Indexer to create the Lucene index.
 */
public class Indexer implements FileVisitor<Path> {

	protected final Path rootFolder;
	protected final Path indexPath;
	protected final List<PathMatcher> ignoreMatchers;
	protected final Pattern ignoredMimeTypes;
	protected final Log log;
	private IndexWriter indexWriter;

	/**
	 * Creates the indexer.
	 * @param rootFolder the folder to start indexing from
	 * @param indexPath the folder to store the index in
	 * @param ignoreMatchers matchers for paths to ignore
	 * @param ignoreMimeTypes regular expression for MIME types to ignore
	 * @param log the logger
	 */
	public Indexer(
		final Path rootFolder,
		final Path indexPath,
		final List<PathMatcher> ignoreMatchers,
		final String ignoreMimeTypes,
		final Log log) {
		this.rootFolder = rootFolder.toAbsolutePath();
		this.indexPath = indexPath;
		this.ignoreMatchers = ignoreMatchers;
		this.ignoredMimeTypes = Pattern.compile(ignoreMimeTypes);
		this.log = log;
	}

	/**
	 * Creates the index.
	 * @param clean {@code true} to delete any existing index before indexing, {@code false} otherwise
	 * @throws MojoExecutionException in case of error
	 */
	public void index(final boolean clean) throws MojoExecutionException {
		try {
			if (clean && Files.exists(indexPath)) {
				Files
					.walk(indexPath)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
			}
			try (final Directory directory = FSDirectory.open(indexPath)) {
				indexWriter = new IndexWriter(directory, new IndexWriterConfig(new CaseSensitiveAnalyzer()));
				try {
					log.info(String.format("Building index for %s into %s", rootFolder, indexPath));
					Files.walkFileTree(rootFolder, this);
				} finally {
					indexWriter.close();
				}
			}
		} catch (IOException e) {
			throw new MojoExecutionException("I/O error", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
		if (!ignoreMatchers
			.stream()
			.filter(matcher -> matcher.matches(dir))
			.findAny()
			.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("ignoring folder " + dir);
			}
			return FileVisitResult.SKIP_SUBTREE;
		}
		if (log.isDebugEnabled()) {
			log.debug("visiting folder " + dir);
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
		final String mimeType = Optional.ofNullable(Files.probeContentType(file)).orElse("text/plain");
		if (!ignoredMimeTypes.matcher(mimeType).matches()) {
			addToIndex(file);
		} else if (log.isDebugEnabled()) {
			log.debug(String.format("skipping file %s because of mime type %s", file, mimeType));
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult visitFileFailed(final Path file, final IOException exception) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileVisitResult postVisitDirectory(final Path dir, final IOException exception) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	protected void addToIndex(final Path path) {
		try {
			if (Files.isRegularFile(path)) {
				if (log.isDebugEnabled()) {
					log.debug("adding file " + path);
				}
				final Document document = new Document();
				document.add(new StringField(Constants.FIELD_PATH, relativePath(path.getParent()), Field.Store.YES));
				final File file = path.toFile();
				document.add(new StringField(Constants.FIELD_FILENAME, file.getName(), Field.Store.YES));
				document.add(new TextField(Constants.FIELD_CONTENTS, new FileReader(file)));
				indexWriter.addDocument(document);
			}
		} catch(IOException e) {
			log.error(String.format("cannot add %s to index", path));
		}
	}

	protected String relativePath(final Path path) {
		return rootFolder.relativize(path).toString();
	}

	protected static List<PathMatcher> createPathMatchers(final String indexFolder, final String ignoreFolders) {
		final FileSystem fs = FileSystems.getDefault();
		return Stream.concat(
				Stream.of(indexFolder),
				ignoreFolders != null ? Arrays.stream(ignoreFolders.split(",")) : Stream.empty()
			)
			.map(name -> String.format("glob:**/%s", name))
			.map(fs::getPathMatcher)
			.collect(Collectors.toList());
	}
}