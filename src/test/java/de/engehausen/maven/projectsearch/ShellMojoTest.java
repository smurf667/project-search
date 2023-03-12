package de.engehausen.maven.projectsearch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(3)
class ShellMojoTest extends AbstractMojoTest {

	@Test
	void interactive() throws IOException, InterruptedException {
		final File root = Paths.get("").toAbsolutePath().resolve("target/test-classes").toFile();
		Assertions.assertTrue(root.exists());

		final ProcessBuilder processBuilder = new ProcessBuilder(getMaven(),
			"de.engehausen:project-search:shell",
			"-DindexFolder=.testindex",
			"-Droot=" + root.getAbsolutePath());
		processBuilder.directory(root);
		runAndCheckOutput(processBuilder,
			"Hello\n?quit\n".getBytes(StandardCharsets.UTF_8),
			"Found:",
			"test3.txt"
		);
	}

}
