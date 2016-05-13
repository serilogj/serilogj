package serilogj.events;

import java.io.*;
import java.util.*;

public class ScalarValue extends LogEventPropertyValue {
	private Object value;
	
	public ScalarValue(Object value) {
		this.value = value;
	}
	
	@Override
	public void render(Writer output, String format, Locale locale) throws IOException {
		if (output == null) {
			throw new IllegalArgumentException("output");
		}
		
		if (value == null) {
			output.write("null");
			return;
		}
		
		// TODO: Something with format
		
		String str = (String)((value instanceof String) ? value : null);
		if (str != null && (format == null || !format.equals("l"))) {
			output.write("\"");
			output.write(str.replace("\"", "\\\""));
			output.write("\"");
		} else {
			output.write(value.toString());
		}

		// TODO: Do something with format
	}
	
	@Override
	public boolean equals(Object obj) {
		ScalarValue sv = (ScalarValue)((obj instanceof ScalarValue) ? obj : null);
		return sv != null && value == sv.value;
	}
	
	@Override
	public int hashCode() {
		return value == null ? 0 : value.hashCode();
	}
}
