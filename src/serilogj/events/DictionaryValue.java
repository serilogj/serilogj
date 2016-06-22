package serilogj.events;

import java.io.*;
import java.util.*;

public class DictionaryValue extends LogEventPropertyValue {
	private Map<ScalarValue, LogEventPropertyValue> elements;

	public DictionaryValue(Iterable<Map.Entry<ScalarValue, LogEventPropertyValue>> elements) {
		if (elements == null) {
			throw new IllegalArgumentException("elements");
		}

		this.elements = new HashMap<ScalarValue, LogEventPropertyValue>();
		elements.forEach(e -> this.elements.put(e.getKey(), e.getValue()));
	}

	public DictionaryValue(Map<ScalarValue, LogEventPropertyValue> elements) {
		if (elements == null) {
			throw new IllegalArgumentException("elements");
		}

		this.elements = elements;
	}

	public Map<ScalarValue, LogEventPropertyValue> getElements() {
		return this.elements;
	}

	@Override
	public void render(Writer output, String format, Locale locale) throws IOException {
		if (output == null) {
			throw new IllegalArgumentException("output");
		}

		output.write('[');
		String delim = "(";
		for (Map.Entry<ScalarValue, LogEventPropertyValue> entry : this.elements.entrySet()) {
			output.write(delim);
			delim = ", (";
			entry.getKey().render(output, null, locale);
			output.write(": ");
			entry.getValue().render(output, null, locale);
			output.write(")");
		}
		output.write(']');
	}

}
