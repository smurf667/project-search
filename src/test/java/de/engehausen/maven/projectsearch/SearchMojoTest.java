package de.engehausen.maven.projectsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
class SearchMojoTest extends AbstractMojoTest {

	@Test
	void customSearch() throws IOException, InterruptedException {
		final File root = Paths.get("").toAbsolutePath().resolve("target/test-classes").toFile();
		Assertions.assertTrue(root.exists());

		final ProcessBuilder processBuilder = new ProcessBuilder(getMaven(),
			"de.engehausen:project-search:search",
			"-Dquery=fox OR Lorem",
			"-Dwhitelist=test2.txt",
			"-DindexFolder=.testindex",
			"-Droot=" + root.getAbsolutePath());
		processBuilder.directory(root);
		runAndCheckOutput(processBuilder,
			"Found:",
			"test1.txt",
			"The following results were ignored:",
			"test2.txt");
	}

	@Test
	void presetSearch() throws IOException, InterruptedException {
		final File root = Paths.get("").toAbsolutePath().resolve("target/test-classes").toFile();
		Assertions.assertTrue(root.exists());

		final ProcessBuilder processBuilder = new ProcessBuilder(getMaven(),
			"de.engehausen:project-search:search",
			"-Dquery=preset:test",
			"-DindexFolder=.testindex",
			"-Droot=" + root.getAbsolutePath());
		processBuilder.directory(root);
		processBuilder.environment().put(TestPresets.TEST_PRESETS, Boolean.toString(true));
		runAndCheckOutput(processBuilder,
			"Found:",
			"test1.txt",
			"test2.txt",
			"!The following results were ignored:");
	}

	@Test
	void failWithHits() throws IOException, InterruptedException {
		final File root = Paths.get("").toAbsolutePath().resolve("target/test-classes").toFile();
		Assertions.assertTrue(root.exists());

		final ProcessBuilder processBuilder = new ProcessBuilder(getMaven(),
			"de.engehausen:project-search:search",
			"-Dquery=pharetra",
			"-DfailOn=hit",
			"-DindexFolder=.testindex",
			"-Droot=" + root.getAbsolutePath());
		processBuilder.directory(root);
		runAndCheckOutput(processBuilder, "Unwanted search results.");
	}

	@Test
	void failWitMisses() throws IOException, InterruptedException {
		final File root = Paths.get("").toAbsolutePath().resolve("target/test-classes").toFile();
		Assertions.assertTrue(root.exists());

		final ProcessBuilder processBuilder = new ProcessBuilder(getMaven(),
			"de.engehausen:project-search:search",
			"-Dquery=this-does-not-exist",
			"-DfailOn=miss",
			"-DindexFolder=.testindex",
			"-Droot=" + root.getAbsolutePath());
		processBuilder.directory(root);
		runAndCheckOutput(processBuilder, "Unwanted search results.");
	}

}
