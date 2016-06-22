package serilogj.formatting.display;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import serilogj.events.LogEvent;
import serilogj.events.LogEventPropertyValue;
import serilogj.events.ScalarValue;

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
 * Describes the properties available in standard message template-based output
 * format strings.
 */
public class OutputProperties {
	/**
	 * The message rendered from the log event.
	 */
	public static final String MessagePropertyName = "Message";

	/**
	 * The timestamp of the log event.
	 */
	public static final String TimestampPropertyName = "Timestamp";

	/**
	 * The level of the log event.
	 */
	public static final String LevelPropertyName = "Level";

	/**
	 * A new line.
	 */
	public static final String NewLinePropertyName = "NewLine";

	/**
	 * The exception associated with the log event.
	 */
	public static final String ExceptionPropertyName = "Exception";

	/**
	 * Create properties from the provided log event.
	 * 
	 * @param logEvent
	 *            The log event.
	 * @return A dictionary with properties representing the log event.
	 */
	public static Map<String, LogEventPropertyValue> GetOutputProperties(LogEvent logEvent) {
		Map<String, LogEventPropertyValue> result = new HashMap<String, LogEventPropertyValue>();
		logEvent.getProperties().forEach((k, v) -> result.put(k, v));

		// "Special" output properties like Message will override any properties
		// with the same name
		// when used in format strings; this doesn't affect the rendering of the
		// message template,
		// which uses only the log event properties.

		result.put(MessagePropertyName,
				new LogEventPropertyMessageValue(logEvent.getMessageTemplate(), logEvent.getProperties()));
		result.put(TimestampPropertyName, new ScalarValue(logEvent.getTimestamp()));
		result.put(LevelPropertyName, new LogEventLevelValue(logEvent.getLevel()));
		result.put(NewLinePropertyName, new LiteralStringValue(System.lineSeparator()));

		String exception = "";
		if (logEvent.getException() != null) {
			exception = logEvent.getException() + System.lineSeparator();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			logEvent.getException().printStackTrace(pw);
			exception += sw.toString();
		}

		result.put(ExceptionPropertyName, new LiteralStringValue(exception));
		return result;
	}
}
