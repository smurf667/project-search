# Lucene-based project indexer

A standalone Maven plugin which can index a folder (and its sub-folders) with [Apache Lucene](https://lucene.apache.org/) for fast queries about
files and their content.

Build the plugin with

	mvn install

To create the index run 

	mvn de.engehausen:project-search:index

in the folder to index, or specify locations via `-Droot=...` and `-DindexFolder=...`.

For the two following goals: If the index was not built before, it will be built based on default indexing options.

Single queries can be run via

	mvn de.engehausen:project-search:search -Dquery=<lucene-query-syntax>|preset:<name>

The goal can be made to fail on search hits or misses with `-DfailOn=<hits|misses>`.

Interactive queries can be run via

	mvn de.engehausen:project-search:shell

Help is available via

	mvn de.engehausen:project-search:help

## Presets

Presets are predefined queries that can be used with the `search` goal.
They are loaded with the Java service loader for the `de.engehausen.maven.projectsearch.PresetProvider` interface.
Core presets are loaded from [`presets.properties`](src/main/resources/presets.properties).
