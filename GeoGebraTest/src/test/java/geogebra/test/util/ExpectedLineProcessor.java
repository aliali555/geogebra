package geogebra.test.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.LineProcessor;

public class ExpectedLineProcessor implements LineProcessor<ImmutableMap<String, String>> {

	private final ImmutableMap.Builder<String, String> inputToExpected = ImmutableMap.builder();
	private final Splitter splitter = Splitter.on("==>").trimResults();
	
	public boolean processLine(String line) {
		if (!line.trim().startsWith("#")) {
			String[] elements = Iterables.toArray(splitter.split(line), String.class);
			inputToExpected.put(elements[0], elements[1]);
		}		
		return true;
	}

	public ImmutableMap<String, String> getResult() {
		return inputToExpected.build();
	}

}
