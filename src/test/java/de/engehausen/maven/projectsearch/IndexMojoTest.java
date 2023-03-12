package de.engehausen.maven.projectsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
class IndexMojoTest extends AbstractMojoTest {

	@Test
	void buildIndex() throws IOException, InterruptedException {
		final File root = Paths.get("").toAbsolutePath().resolve("target/test-classes").toFile();
		Assertions.assertTrue(root.exists());

		final ProcessBuilder processBuilder = new ProcessBuilder(getMaven(),
			"de.engehausen:project-search:index",
			"-DindexFolder=.testindex",
			"-DignoreFolders=de",
			"-Dclean=true",
			"-Droot=" + root.getAbsolutePath());
		processBuilder.directory(root);
		runAndCheckOutput(processBuilder,
			"building index for",
			".testindex",
			"into",
			root.getAbsolutePath());
	}

}
