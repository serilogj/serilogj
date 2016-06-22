package serilogj.parsing;

import java.io.*;
import java.util.*;

import serilogj.events.LogEventPropertyValue;
import serilogj.formatting.display.Padding;

public class PropertyToken extends MessageTemplateToken {
	private String rawText;
	private Integer position = null;
	private String propertyName;
	private String format;
	private Destructuring destructuring;
	private Alignment alignment;

	public PropertyToken(String propertyName, String rawText, String format, Alignment alignment,
			Destructuring destructuring, int startIndex) {
		super(startIndex);

		if (propertyName == null) {
			throw new IllegalArgumentException("propertyName");
		}
		if (rawText == null) {
			throw new IllegalArgumentException("rawText");
		}

		this.rawText = rawText;
		this.propertyName = propertyName;
		this.format = format;
		this.destructuring = destructuring;
		this.alignment = alignment;

		try {
			int position = Integer.parseInt(propertyName);
			if (position >= 0) {
				this.position = position;
			}
		} catch (NumberFormatException e) {
			// Ignore
		}
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Destructuring getDestructuring() {
		return destructuring;
	}

	public String getFormat() {
		return format;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public boolean getIsPositional() {
		return position != null && position >= 0;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public int getLength() {
		return rawText.length();
	}

	@Override
	public void render(Map<String, LogEventPropertyValue> properties, Writer output, Locale locale) throws IOException {
		if (properties == null) {
			throw new IllegalArgumentException("properties");
		}

		if (output == null) {
			throw new IllegalArgumentException("output");
		}

		if (!properties.containsKey(propertyName)) {
			output.write(rawText);
			return;
		}

		LogEventPropertyValue propertyValue = properties.get(propertyName);
		if (alignment == null) {
			propertyValue.render(output, format, locale);
			return;
		}

		StringWriter valueOutput = new StringWriter();
		propertyValue.render(valueOutput, format, locale);
		String value = valueOutput.toString();

		if (value.length() >= alignment.getWidth()) {
			output.write(value);
			return;
		}

		Padding.apply(output, value, alignment);
	}

	@Override
	public String toString() {
		return rawText;
	}

	@Override
	public boolean equals(Object obj) {
		PropertyToken sv = (PropertyToken) ((obj instanceof PropertyToken) ? obj : null);
		return sv != null && rawText.equals(sv.rawText);
	}

	@Override
	public int hashCode() {
		return propertyName.hashCode();
	}
}
