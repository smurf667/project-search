package de.engehausen.maven.projectsearch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Assertions;

class AbstractMojoTest {

	public static final String OUTPUT_LOG = "output.log";
	private static final String NOT = "!";

	protected void runAndCheckOutput(final ProcessBuilder processBuilder, final String ...expected) throws IOException, InterruptedException {
		runAndCheckOutput(processBuilder, null, expected);
	}

	protected void runAndCheckOutput(final ProcessBuilder processBuilder, final byte[] inBytes, final String ...expected) throws IOException, InterruptedException {
		final File log = new File(processBuilder.directory(), OUTPUT_LOG);
		processBuilder.redirectOutput(Redirect.to(log));
		processBuilder.redirectError(Redirect.to(log));

		final Process process = processBuilder.start();
		if (inBytes != null) {
			final OutputStream stdin = process.getOutputStream();
			stdin.write(inBytes);
			stdin.flush();
		}
		if (process.waitFor(1, TimeUnit.MINUTES)) {
			final String logContents = Files.readString(log.toPath(), StandardCharsets.UTF_8);
			for (final String needle : expected) {
				if (needle.startsWith(NOT)) {
					Assertions.assertFalse(logContents.contains(needle.substring(1)), String.format("'%s' found", needle));
				} else {
					Assertions.assertTrue(logContents.contains(needle), String.format("'%s' not found", needle));
				}
			}
		} else {
			process.destroyForcibly();
			Assertions.fail("Timeout after one minutes");
		}
	}

	protected String getMaven() {
		return SystemUtils.IS_OS_WINDOWS ? "mvn.cmd" : "mvn";
	}
}
