package de.engehausen.maven.projectsearch;

import java.util.Set;

/**
 * Various constants used in the mojos.
 */
public final class Constants {

	private Constants() {}

	/** Index mojo name ({@code index}) */
	public static final String INDEX_NAME = "index";

	/** Search mojo name ({@code search}) */
	public static final String SEARCH_NAME = "search";

	/** Shell mojo name ({@code shell}) */
	public static final String SHELL_NAME = "shell";

	/**
	 * {@code indexFile} parameter: The name of the index database.
	 */
	public static final String PARAM_INDEX_FOLDER = "indexFolder";

	/** {@code root} parameter: The file system root folder to index from. */
	public static final String PARAM_ROOT = "root";

	/** {@code clean} parameter: Flag to build a clean index. */
	public static final String PARAM_CLEAN = "clean";

	/** {@code ignoreFolders} parameter: Comma-separated list of folder names to ignore. */
	public static final String PARAM_IGNORE_FOLDERS = "ignoreFolders";

	/** {@code ignoreMimeTypes} parameter: Comma-separated list of regular expressions for MIME types to ignore. */
	public static final String PARAM_IGNORE_MIME_TYPES = "ignoreMimeTypes";

	/** {@code query} parameter: The search query. */
	public static final String PARAM_QUERY = "query";

	/** {@code whitelist} parameter: Comma-separated list of regular expressions used for whitelisting. */
	public static final String PARAM_WHITELIST = "whitelist";

	/** {@code failOn} parameter: Condition to make the execution fail, see {@link FailCondition}. */
	public static final String PARAM_FAIL_ON = "failOn";

	/** Default name of the index database. */
	public static final String DEFAULT_INDEX = ".psindex";

	/** Default root folder (current directory) */
	public static final String DEFAULT_ROOT = "";

	/** Default folders to ignore */
	public static final String DEFAULT_IGNORE_FOLDERS = ".git,.m2,.metadata,.settings,.yarn,build,generated,node_modules,target";

	/** Default MIME types to ignore */
	public static final String DEFAULT_IGNORE_MIME_TYPES = "(audio/.+)|(image/.+)|(video/.+)";

	/** path field */
	public static final String FIELD_PATH = "path";

	/** filename field */
	public static final String FIELD_FILENAME = "filename";

	/** contents field */
	public static final String FIELD_CONTENTS = "contents";

	/** all index fields */
	public static final Set<String> ALL_FIELDS = Set.of(FIELD_CONTENTS, FIELD_PATH, FIELD_FILENAME);

	/** fields to extract from search result document */
	public static final Set<String> RESULT_FIELDS = Set.of(FIELD_PATH, FIELD_FILENAME);

	/** special preset search query. */
	public static final String PRESET_PREFIX = "preset:";

	/**
	 * Conditions under which to fail the Maven execution.
	 */
	public enum FailCondition {
		/** Succeed normally, no matter if search results or not. */
		never,
		/** Fail when result has hits. */
		hit,
		/** Fail when result has no hits. */
		miss
	}
}
