package serilogj.formatting.display;

import java.io.*;
import java.util.*;

import serilogj.events.LogEventPropertyValue;

public class LiteralStringValue extends LogEventPropertyValue {
	private String value;

	public LiteralStringValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("value");
		}

		this.value = value;
	}

	@Override
	public void render(Writer output, String format, Locale locale) throws IOException {
		output.write(Casing.format(value, format));
	}

	@Override
	public boolean equals(Object obj) {
		LiteralStringValue sv = (LiteralStringValue) ((obj instanceof LiteralStringValue) ? obj : null);
		return sv != null && value.equals(sv.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
