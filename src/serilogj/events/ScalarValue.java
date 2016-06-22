package serilogj.events;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import serilogj.debugging.SelfLog;

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

		String str = (String) ((value instanceof String) ? value : null);
		if (str != null && (format == null || !format.equals("l"))) {
			output.write("\"");
			output.write(str.replace("\"", "\\\""));
			output.write("\"");
		} else if (value instanceof Date && format != null && !format.equals("")) {
			try {
				SimpleDateFormat formatter = locale == null ? new SimpleDateFormat(format)
						: new SimpleDateFormat(format, locale);
				output.write(formatter.format(value));
			} catch (Exception e) {
				output.write(value.toString());
				SelfLog.writeLine("Invalid date format \"%s\", exception %s", format, e.getMessage());
			}
		} else if (value instanceof TemporalAccessor && format != null && format.equals("")) {
			try {
				DateTimeFormatter formatter = locale == null ? DateTimeFormatter.ofPattern(format)
						: DateTimeFormatter.ofPattern(format, locale);
				output.write(formatter.format((TemporalAccessor) value));
			} catch (Exception e) {
				output.write(value.toString());
				SelfLog.writeLine("Invalid date format \"%s\", exception %s", format, e.getMessage());
			}
		} else {
			output.write(value.toString());
		}

		// TODO: Do something with format
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		ScalarValue sv = (ScalarValue) ((obj instanceof ScalarValue) ? obj : null);
		return sv != null && value == sv.value;
	}

	@Override
	public int hashCode() {
		return value == null ? 0 : value.hashCode();
	}
}
