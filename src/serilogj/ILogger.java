package serilogj;

import serilogj.core.ILogEventEnricher;
import serilogj.events.LogEvent;
import serilogj.events.LogEventLevel;

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
 * The core Serilog logging API, used for writing log events.
 * 
 * <example> var log = new LoggerConfiguration() .WithConsoleSink()
 * .CreateLogger();
 * 
 * var thing = "World"; log.Information("Hello, {Thing}!", thing); </example>
 * 
 * The methods on <see cref="ILogger"/> (and its static sibling
 * <see cref="Log"/>) are guaranteed never to throw exceptions. Methods on all
 * other types may.
 * 
 */
public interface ILogger {
	/**
	 * Create a logger that enriches log events via the provided enrichers.
	 * 
	 * @param enrichers
	 *            Enrichers that apply in the context.
	 * @return A logger that will enrich log events as specified.
	 */
	ILogger forContext(ILogEventEnricher[] enrichers);

	/**
	 * Create a logger that enriches log events with the specified property.
	 * 
	 * @return A logger that will enrich log events as specified.
	 */

	ILogger forContext(String propertyName, Object value);

	/**
	 * Create a logger that enriches log events with the specified property.
	 * 
	 * @return A logger that will enrich log events as specified.
	 */

	ILogger forContext(String propertyName, Object value, boolean destructureObjects);

	/**
	 * Create a logger that marks log events as being from the specified source
	 * type.
	 * 
	 * @param source
	 *            Type generating log messages in the context.
	 * @return A logger that will enrich log events as specified.
	 */
	ILogger forContext(Class<?> source);

	/**
	 * Write an event to the log.
	 * 
	 * @param logEvent
	 *            The event to write.
	 */
	void write(LogEvent logEvent);

	/**
	 * Write a log event with the specified level.
	 * 
	 * @param level
	 *            The level of the event.
	 * @param messageTemplate
	 * @param propertyValues
	 */
	void write(LogEventLevel level, String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the specified level and associated exception.
	 * 
	 * @param level
	 *            The level of the event.
	 * @param exception
	 *            Exception related to the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 */
	void write(LogEventLevel level, Throwable exception, String messageTemplate, Object... propertyValues);

	/**
	 * Determine if events at the specified level will be passed through to the
	 * log sinks.
	 * 
	 * @param level
	 *            Level to check.
	 * @return True if the level is enabled; otherwise, false.
	 */
	boolean isEnabled(LogEventLevel level);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Verbose"/> level and
	 * associated exception.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Verbose("Staring into space, wondering if we're
	 *            alone."); </example>
	 */
	void verbose(String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Verbose"/> level and
	 * associated exception.
	 * 
	 * @param exception
	 *            Exception related to the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Verbose(ex, "Staring into space, wondering where
	 *            this comet came from."); </example>
	 */
	void verbose(Throwable exception, String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Debug"/> level and
	 * associated exception.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Debug("Starting up at {StartedAt}.",
	 *            DateTime.Now); </example>
	 */
	void debug(String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Debug"/> level and
	 * associated exception.
	 * 
	 * @param exception
	 *            Exception related to the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Debug(ex, "Swallowing a mundane exception.");
	 *            </example>
	 */
	void debug(Throwable exception, String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Information"/> level
	 * and associated exception.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Information("Processed {RecordCount} records in
	 *            {TimeMS}.", records.Length, sw.ElapsedMilliseconds);
	 *            </example>
	 */
	void information(String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Information"/> level
	 * and associated exception.
	 * 
	 * @param exception
	 *            Exception related to the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Information(ex, "Processed {RecordCount} records
	 *            in {TimeMS}.", records.Length, sw.ElapsedMilliseconds);
	 *            </example>
	 */
	void information(Throwable exception, String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Warning"/> level and
	 * associated exception.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Warning("Skipped {SkipCount} records.",
	 *            skippedRecords.Length); </example>
	 */
	void warning(String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Warning"/> level and
	 * associated exception.
	 * 
	 * @param exception
	 *            Exception related to the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Warning(ex, "Skipped {SkipCount} records.",
	 *            skippedRecords.Length); </example>
	 */
	void warning(Throwable exception, String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Error"/> level and
	 * associated exception.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Error("Failed {ErrorCount} records.",
	 *            brokenRecords.Length); </example>
	 */
	void error(String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Error"/> level and
	 * associated exception.
	 * 
	 * @param exception
	 *            Exception related to the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Error(ex, "Failed {ErrorCount} records.",
	 *            brokenRecords.Length); </example>
	 */
	void error(Throwable exception, String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Fatal"/> level and
	 * associated exception.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Fatal("Process terminating."); </example>
	 */
	void fatal(String messageTemplate, Object... propertyValues);

	/**
	 * Write a log event with the <see cref="LogEventLevel.Fatal"/> level and
	 * associated exception.
	 * 
	 * @param exception
	 *            Exception related to the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Fatal(ex, "Process terminating."); </example>
	 */
	void fatal(Throwable exception, String messageTemplate, Object... propertyValues);
}
