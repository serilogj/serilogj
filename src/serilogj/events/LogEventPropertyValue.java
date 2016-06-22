package serilogj.events;

import java.io.*;
import java.util.*;

public abstract class LogEventPropertyValue {
	/**
	 * Render the value to the output.
	 * 
	 * @param output
	 *            The output.
	 * @param format
	 *            A format string applied to the value, or null.
	 * @param formatProvider
	 *            A format provider to apply to the value, or null to use the
	 *            default. {@link LogEventPropertyValue.ToString(string,
	 *            IFormatProvider)} .
	 */
	public void render(Writer output, String format) throws IOException {
		render(output, format, null);
	}

	public void render(Writer output) throws IOException {
		render(output, null, null);
	}

	public abstract void render(Writer output, String format, Locale locale) throws IOException;

	/**
	 * Returns a string that represents the current object.
	 * 
	 * @return A string that represents the current object.
	 * 
	 *         <filterpriority>2</filterpriority>
	 */
	@Override
	public String toString() {
		return toString(null, null);
	}

	/**
	 * Formats the value of the current instance using the specified format.
	 * 
	 * @return The value of the current instance in the specified format.
	 * 
	 * @param format
	 *            The format to use.-or- A null reference (Nothing in Visual
	 *            Basic) to use the default format defined for the type of the
	 *            <see cref="T:System.IFormattable"/> implementation.
	 * @param formatProvider
	 *            The provider to use to format the value.-or- A null reference
	 *            (Nothing in Visual Basic) to obtain the numeric format
	 *            information from the current locale setting of the operating
	 *            system. <filterpriority>2</filterpriority>
	 */
	public final String toString(String format, Locale locale) {
		try {
			StringWriter output = new StringWriter();
			render(output, format, locale);
			return output.toString();
		} catch (IOException e) {
			return "";
		}
	}
}
