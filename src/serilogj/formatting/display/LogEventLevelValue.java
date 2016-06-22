package serilogj.formatting.display;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import serilogj.events.LogEventLevel;
import serilogj.events.LogEventPropertyValue;
import serilogj.parsing.Alignment;

public class LogEventLevelValue extends LogEventPropertyValue {
	private LogEventLevel value;

	private static final String[][] shortenedLevelMap = { new String[] { "V", "Vb", "Vrb", "Verb" },
			new String[] { "D", "De", "Dbg", "Dbug" }, new String[] { "I", "In", "Inf", "Info" },
			new String[] { "W", "Wn", "Wrn", "Warn" }, new String[] { "E", "Er", "Err", "Eror" },
			new String[] { "F", "Fa", "Ftl", "Fatl" } };

	public LogEventLevelValue(LogEventLevel value) {
		this.value = value;
	}

	/**
	 * This method will apply only upper or lower case formatting, not fixed
	 * width
	 */
	@Override
	public void render(Writer output, String format, Locale locale) throws IOException {
		applyFormatting(output, value.toString(), null, format);
	}

	public final void render(Writer output, Alignment alignment, String format) throws IOException {
		applyFormatting(output, alignedValue(alignment), alignment, format);
	}

	private void applyFormatting(Writer output, String value, Alignment alignment, String format) throws IOException {
		Padding.apply(output, Casing.format(value, format), alignment);
	}

	private String alignedValue(Alignment alignment) {
		if (alignment == null || alignment.getWidth() <= 0) {
			return value.toString();
		}

		if (isCustomWidthSupported(alignment.getWidth())) {
			return shortLevelFor(value, alignment.getWidth());
		}

		String stringValue = value.toString();
		if (isOutputStringTooWide(alignment, stringValue)) {
			return stringValue.substring(0, alignment.getWidth());
		}

		return stringValue;
	}

	private static String shortLevelFor(LogEventLevel value, int width) {
		int index = value.ordinal();
		if (index < 0 || index > LogEventLevel.Fatal.ordinal()) {
			return "";
		}

		return shortenedLevelMap[index][width - 1];
	}

	private static boolean isOutputStringTooWide(Alignment alignmentValue, String formattedValue) {
		return alignmentValue.getWidth() < formattedValue.length();
	}

	private static boolean isCustomWidthSupported(int width) {
		return width > 0 && width < 5;
	}
}
