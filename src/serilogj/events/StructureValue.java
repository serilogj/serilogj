package serilogj.events;

import java.io.*;
import java.util.*;

public class StructureValue extends LogEventPropertyValue {
	private ArrayList<LogEventProperty> properties;
	private String typeTag;

	public StructureValue(ArrayList<LogEventProperty> properties, String typeTag) {
		if (properties == null) {
			throw new IllegalArgumentException("properties");
		}

		this.properties = properties;
		this.typeTag = typeTag;
	}

	public ArrayList<LogEventProperty> getProperties() {
		return properties;
	}

	public void setTypeTag(String typeTag) {
		this.typeTag = typeTag;
	}

	public String getTypeTag() {
		return typeTag;
	}

	@Override
	public void render(Writer output, String format, Locale locale) throws IOException {
		if (output == null) {
			throw new IllegalArgumentException("output");
		}

		if (typeTag != null) {
			output.write(typeTag);
			output.write(' ');
		}

		output.write("{ ");

		String delim = "";
		for (int i = 0; i < properties.size(); i++) {
			output.write(delim);
			delim = ", ";

			output.write(properties.get(i).getName());
			output.write(": ");
			properties.get(i).getValue().render(output, format, locale);
		}

		output.write(" }");
	}
}