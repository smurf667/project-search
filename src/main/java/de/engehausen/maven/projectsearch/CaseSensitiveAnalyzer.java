package de.engehausen.maven.projectsearch;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Case-sensitive analyzer. Similar to the case insensitive
 * {@link StandardAnalyzer}.
 */
public class CaseSensitiveAnalyzer extends StopwordAnalyzerBase {

	public static final int MAX_TOKEN_LENGTH = 255;

	protected int maxTokenLength;

	/**
	 * Creates the analyzer with the default maximal token value
	 * of {@link #MAX_TOKEN_LENGTH}.
	 */
	public CaseSensitiveAnalyzer() {
		this(MAX_TOKEN_LENGTH);
	}

	/**
	 * Creates the analyzer with the given maximal token value.
	 * @param maxTokenLength the maximal token value.
	 */
	public CaseSensitiveAnalyzer(final int maxTokenLength) {
		this.maxTokenLength = maxTokenLength;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {
		final StandardTokenizer src = new StandardTokenizer();
		src.setMaxTokenLength(maxTokenLength);
		return new TokenStreamComponents(r -> {
			src.setMaxTokenLength(maxTokenLength);
			src.setReader(r);
		}, new StopFilter(src, stopwords));
	}
}