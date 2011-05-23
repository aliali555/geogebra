package geogebra.cas;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import geogebra.CommandLineArguments;
import geogebra.kernel.Kernel;
import geogebra.main.Application;
import geogebra.test.util.ExpectedLineProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

public class GeoGebraCasIntegrationTest {

	static final String OUTPUT_DIRECTORY = "test-output";
	static final String LOG_FILE_ALL = "CasTestFunctionsResults-all.txt";
	static final String LOG_FILE_FAILURES ="CasTestFunctionsResults-failures.txt";
	
	@Test
	public void simpleEvaluationFromInputFile() throws Exception {
		Application app = new Application(new CommandLineArguments(new String[0]), new JFrame(), false);
		GeoGebraCAS cas = new GeoGebraCAS(new Kernel(app));

		ImmutableMap<String, String> inputToExpected = loadExpected("CasTestFunctionsExpected.txt");
		
		List<String> results = newArrayListWithCapacity(inputToExpected.size());
		List<String> failures = newArrayList();
		
		for (Entry<String, String> entry : inputToExpected.entrySet()) {
			String input = entry.getKey();
			String expected = entry.getValue();
			String result;
			boolean fail = false;
			try {
				result = cas.evaluateGeoGebraCAS(input);
			} catch (Throwable ex) {
				result = "An exception occured: " + ex;
				fail = true;
			}
			
			fail = fail || isUnexpectedResult(result, expected);
			String line = String.format("%s - Input: '%s' Expected: '%s' Result: '%s'",
					fail ? "FAIL" : "PASS", input, expected, result);
			results.add(line);
			if (fail) {
				failures.add(line);
			}
		}
		
		writeLinesToFile(LOG_FILE_ALL, results);
		writeLinesToFile(LOG_FILE_FAILURES, failures);
		
		int failCount = results.size() - failures.size();
		String msg = String.format("Evaluation returned unexpected results for %d function(s), see file '%s/%s'",
				failCount, OUTPUT_DIRECTORY, LOG_FILE_FAILURES);
		assertThat(msg, failCount, equalTo(0));
	}
	
	private static ImmutableMap<String, String> loadExpected(String resourceName) throws IOException, URISyntaxException {
		File file = new File(GeoGebraCasIntegrationTest.class.getResource(resourceName).toURI());
		return Files.readLines(file, Charsets.ISO_8859_1, new ExpectedLineProcessor());
	}
	
	private static boolean isUnexpectedResult(String result, String expected) {
		return !customTrim(result).equals(customTrim(expected));
	}
	
	private static String customTrim(String str) {
		return str.replace(" ", "");
	}
	
	private static void writeLinesToFile(String fileName, Iterable<String> lines) throws IOException {
		File testDir = new File(OUTPUT_DIRECTORY);
		if (!testDir.exists()) {
			testDir.mkdir();
		}
		String fileContent = Joiner.on("\n").join(lines);
		Files.write(fileContent, new File(testDir, fileName), Charsets.ISO_8859_1);
	}
}
