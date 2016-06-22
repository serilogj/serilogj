package serilogj.formatting.display;

import java.util.Locale;

public final class Casing {
	public static String format(String value) {
		return format(value, null);
	}

	/**
	 * Apply upper or lower casing to <paramref name="value"/> when
	 * <paramref name="format"/> is provided. Returns
	 * <paramref name="value"/> when no or invalid format provided
	 * 
	 * @return The provided <paramref name="value"/> with formatting applied
	 */
	public static String format(String value, String format) {
		if (format == null) {
			return value;
		}

		switch (format) {
		case "u":
			return value.toUpperCase(Locale.US);
		case "w":
			return value.toLowerCase(Locale.US);
		default:
			return value;
		}
	}
}