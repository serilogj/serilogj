package serilogj.formatting.json;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import serilogj.events.DictionaryValue;
import serilogj.events.LogEvent;
import serilogj.events.LogEventProperty;
import serilogj.events.LogEventPropertyValue;
import serilogj.events.ScalarValue;
import serilogj.events.SequenceValue;
import serilogj.events.StructureValue;
import serilogj.formatting.ITextFormatter;
import serilogj.parsing.MessageTemplateToken;
import serilogj.parsing.PropertyToken;

// Copyright 2013-2015 Serilog Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Formats log events in a simple JSON structure. Instances of this class are
 * safe for concurrent access by multiple threads.
 */
public class JsonFormatter implements ITextFormatter {
	@FunctionalInterface
	interface WriterConsumer {
		void apply(Object value, Boolean quote, Writer writer) throws IOException;
	}

	private boolean omitEnclosingObject;
	private String closingDelimiter;
	private boolean renderMessage;
	private Map<Class<?>, WriterConsumer> literalWriters;
	private Locale locale;

	/**
	 * Construct a <see cref="JsonFormatter"/>.
	 * 
	 * @param omitEnclosingObject
	 *            If true, the properties of the event will be written to the
	 *            output without enclosing braces. Otherwise, if false, each
	 *            event will be written as a well-formed JSON object.
	 * @param closingDelimiter
	 *            A string that will be written after each log event is
	 *            formatted. If null, <see cref="Environment.NewLine"/> will be
	 *            used. Ignored if <paramref name="omitEnclosingObject"/> is
	 *            true.
	 * @param renderMessage
	 *            If true, the message will be rendered and written to the
	 *            output as a property named RenderedMessage.
	 * @param formatProvider
	 *            Supplies culture-specific formatting information, or null.
	 */
	public JsonFormatter(boolean omitEnclosingObject, String closingDelimiter, boolean renderMessage, Locale locale) {
		this.omitEnclosingObject = omitEnclosingObject;
		this.closingDelimiter = closingDelimiter;
		this.renderMessage = renderMessage;
		this.locale = locale;

		literalWriters = new HashMap<Class<?>, WriterConsumer>();
		literalWriters.put(boolean.class, (v, q, o) -> writeBoolean(v, q, o));
		literalWriters.put(Character.class, (v, q, o) -> writeString(v, q, o));
		literalWriters.put(Byte.class, (v, q, o) -> writeToString(v, q, o));
		literalWriters.put(Short.class, (v, q, o) -> writeToString(v, q, o));
		literalWriters.put(Integer.class, (v, q, o) -> writeToString(v, q, o));
		literalWriters.put(Long.class, (v, q, o) -> writeToString(v, q, o));
		literalWriters.put(Float.class, (v, q, o) -> writeFloat(v, q, o));
		literalWriters.put(Double.class, (v, q, o) -> writeFloat(v, q, o));
		literalWriters.put(String.class, (v, q, o) -> writeString(v, q, o));
		literalWriters.put(ScalarValue.class, (v, q, o) -> writeLiteral(((ScalarValue) v).getValue(), q, o));
		literalWriters.put(SequenceValue.class, (v, q, o) -> writeSequence(v, q, o));
		literalWriters.put(DictionaryValue.class, (v, q, o) -> writeDictionary(v, q, o));
		literalWriters.put(StructureValue.class, (v, q, o) -> writeStructure(v, q, o));
		literalWriters.put(Date.class, (v, q, o) -> writeDate(v, q, o));
		literalWriters.put(LocalDate.class, (v, q, o) -> writeTemporal(v, q, o));
		literalWriters.put(LocalDateTime.class, (v, q, o) -> writeTemporal(v, q, o));
		literalWriters.put(ZonedDateTime.class, (v, q, o) -> writeTemporal(v, q, o));
	}

	@Override
	public void format(LogEvent logEvent, Writer output) throws IOException {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}
		if (output == null) {
			throw new IllegalArgumentException("output");
		}

		if (!omitEnclosingObject) {
			output.write("{");
		}

		writeJsonProperty("Timestamp", logEvent.getTimestamp(), output, false);
		writeJsonProperty("Level", logEvent.getLevel(), output, true);
		writeJsonProperty("MessageTemplate", logEvent.getMessageTemplate(), output, true);
		if (renderMessage) {
			String message = logEvent.renderMessage(locale);
			writeJsonProperty("Message", message, output, true);
		}

		Throwable ex = logEvent.getException();
		if (ex != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			writeJsonProperty("Exception", sw.toString(), output, true);
		}

		if (logEvent.getProperties().size() != 0) {
			writeProperties(logEvent.getProperties(), output);
		}

		ArrayList<PropertyToken> tokensWithFormat = new ArrayList<PropertyToken>();
		for (MessageTemplateToken token : logEvent.getMessageTemplate().getTokens()) {
			if (!(token instanceof PropertyToken)) {
				continue;
			}
			PropertyToken pt = (PropertyToken) token;
			if (pt.getFormat() == null || pt.getFormat().equals("")) {
				continue;
			}
			tokensWithFormat.add(pt);
		}

		// We do not support renderings (yet)
		tokensWithFormat.clear();
		if (tokensWithFormat.size() > 0) {
			writeRenderings(tokensWithFormat, logEvent.getProperties(), output);
		}

		if (!omitEnclosingObject) {
			output.write("}");
			output.write(closingDelimiter);
		}
	}

	private void writeRenderings(ArrayList<PropertyToken> tokensWithFormat,
			Map<String, LogEventPropertyValue> properties, Writer output) throws IOException {
		output.write(",\"Renderings\":{");
		output.write("}");
	}

	private void writeProperties(Map<String, LogEventPropertyValue> properties, Writer output) throws IOException {
		output.write(",\"Properties\":{");
		boolean delim = false;
		for (Map.Entry<String, LogEventPropertyValue> property : properties.entrySet()) {
			writeJsonProperty(property.getKey(), property.getValue(), output, delim);
			delim = true;
		}
		output.write("}");
	}

	private void writeJsonProperty(String name, Object value, Writer output, Boolean writeDelim) throws IOException {
		if (writeDelim) {
			output.write(",");
		}
		output.write("\"");
		output.write(name);
		output.write("\":");
		writeLiteral(value, false, output);
	}

	private void writeBoolean(Object value, Boolean quote, Writer output) throws IOException {
		output.write((boolean) value ? "true" : "false");
	}

	private void writeToString(Object value, Boolean quote, Writer output) throws IOException {
		if (quote) {
			output.write("\"");
		}

		output.write(value == null ? null : value.toString());

		if (quote) {
			output.write("\"");
		}
	}

	private void writeString(Object value, Boolean quote, Writer output) throws IOException {
		String content = escape(value.toString());
		output.write("\"");
		output.write(content);
		output.write("\"");
	}

	private void writeFloat(Object value, Boolean quote, Writer output) throws IOException {
		output.write(String.format(Locale.ROOT, "%f", value));
	}

	private void writeLiteral(Object value, Boolean quote, Writer output) throws IOException {
		if (value == null) {
			output.write("null");
			return;
		}

		WriterConsumer consumer = literalWriters.get(value.getClass());
		if (consumer != null) {
			consumer.apply(value, quote, output);
		} else {
			writeString(value, quote, output);
		}
	}

	private void writeSequence(Object value, Boolean quote, Writer output) throws IOException {
		output.write("[");
		String delim = "";
		for (Object element : ((SequenceValue) value).getElements()) {
			output.write(delim);
			delim = ",";
			writeLiteral(element, quote, output);
		}
		output.write("]");
	}

	private void writeDictionary(Object value, Boolean quote, Writer output) throws IOException {
		output.write("{");
		String delim = "";
		for (Map.Entry<ScalarValue, LogEventPropertyValue> element : ((DictionaryValue) value).getElements()
				.entrySet()) {
			output.write(delim);
			delim = ",";
			writeLiteral(element.getKey(), quote, output);
			output.write(":");
			writeLiteral(element.getValue(), quote, output);
		}
		output.write("}");
	}

	private void writeStructure(Object value, Boolean quote, Writer output) throws IOException {
		StructureValue sv = (StructureValue) value;
		output.write("{");
		String delim = "";

		if (sv.getTypeTag() != null && !sv.getTypeTag().equals("")) {
			writeString("_typeTag", quote, output);
			output.write(":");
			writeLiteral(sv.getTypeTag(), quote, output);
			delim = ",";
		}

		for (LogEventProperty property : sv.getProperties()) {
			output.write(delim);
			delim = ",";

			writeString(property.getName(), quote, output);
			output.write(":");
			writeLiteral(property.getValue(), quote, output);
		}
		output.write("}");
	}

	private void writeDate(Object value, Boolean quote, Writer output) throws IOException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		output.write("\"");
		output.write(formatter.format(value));
		output.write("\"");
	}

	private void writeTemporal(Object value, Boolean quote, Writer output) throws IOException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		output.write("\"");
		formatter.formatTo((TemporalAccessor) value, output);
		output.write("\"");
	}

	/**
	 * Perform simple JSON string escaping on <paramref name="s"/>.
	 * 
	 * @param s
	 *            A raw string.
	 * @return A JSON-escaped version of <paramref name="s"/>.
	 */
	public static String escape(String s) {
		if (s == null) {
			return null;
		}

		StringBuilder escapedResult = null;
		int cleanSegmentStart = 0;
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c < (char) 32 || c == '\\' || c == '"') {

				if (escapedResult == null) {
					escapedResult = new StringBuilder();
				}

				escapedResult.append(s.substring(cleanSegmentStart, i));
				cleanSegmentStart = i + 1;

				switch (c) {
				case '"':
					escapedResult.append("\\\"");
					break;
				case '\\':
					escapedResult.append("\\\\");
					break;
				case '\n':
					escapedResult.append("\\n");
					break;
				case '\r':
					escapedResult.append("\\r");
					break;
				case '\f':
					escapedResult.append("\\f");
					break;
				case '\t':
					escapedResult.append("\\t");
					break;
				default:
					escapedResult.append("\\u");
					escapedResult.append(String.format("%04X", new Integer(c)));
					break;
				}
			}
		}

		if (escapedResult != null) {
			if (cleanSegmentStart != s.length()) {
				escapedResult.append(s.substring(cleanSegmentStart));
			}

			return escapedResult.toString();
		}

		return s;
	}
}
