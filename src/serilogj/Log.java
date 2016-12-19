package serilogj;

import java.io.IOException;

import serilogj.core.ILogEventEnricher;
import serilogj.core.pipeline.SilentLogger;
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
 * An optional static entry point for logging that can be easily referenced by
 * different parts of an application. To configure the <see cref="Log"/> set the
 * Logger static property to a logger instance.
 * 
 * <example> Log.Logger = new LoggerConfiguration() .WithConsoleSink()
 * .CreateLogger();
 * 
 * var thing = "World"; Log.Logger.Information("Hello, {Thing}!", thing);
 * </example>
 * 
 * The methods on <see cref="Log"/> (and its dynamic sibling
 * <see cref="ILogger"/>) are guaranteed never to throw exceptions. Methods on
 * all other types may.
 * 
 */
public class Log {
	private static ILogger _logger = new SilentLogger();

	/**
	 * The globally-shared logger.
	 * 
	 * @exception ArgumentNullException
	 */
	public synchronized static ILogger getLogger() {
		return _logger;
	}

	public synchronized static void setLogger(ILogger value) {
		if (value == null) {
			throw new IllegalArgumentException("value");
		}
		_logger = value;
	}

	/**
	 * Resets <see cref="Logger"/> to the default and disposes the original if
	 * possible
	 */
	public synchronized static void closeAndFlush() {
		ILogger logger = _logger;
		_logger = new SilentLogger();

		if (logger instanceof java.io.Closeable) {
			try {
				((java.io.Closeable) logger).close();
			} catch (IOException e) {
				// Never throw errors
			}
		}
	}

	/**
	 * Create a logger that enriches log events via the provided enrichers.
	 * 
	 * @param enrichers
	 *            Enrichers that apply in the context.
	 * @return A logger that will enrich log events as specified.
	 */
	public static ILogger forContext(ILogEventEnricher[] enrichers) {
		return getLogger().forContext(enrichers);
	}

	/**
	 * Create a logger that enriches log events with the specified property.
	 * 
	 * @return A logger that will enrich log events as specified.
	 */
	public static ILogger forContext(String propertyName, Object value) {
		return forContext(propertyName, value, false);
	}

	public static ILogger forContext(String propertyName, Object value, boolean destructureObjects) {
		return getLogger().forContext(propertyName, value, destructureObjects);
	}

	/**
	 * Create a logger that marks log events as being from the specified source
	 * type.
	 * 
	 * @param source
	 *            Type generating log messages in the context.
	 * @return A logger that will enrich log events as specified.
	 */
	public static ILogger forContext(Class<?> source) {
		return getLogger().forContext(source);
	}

	/**
	 * Write an event to the log.
	 * 
	 * @param logEvent
	 *            The event to write.
	 */
	public static void write(LogEvent logEvent) {
		getLogger().write(logEvent);
	}

	/**
	 * Write a log event with the specified level.
	 * 
	 * @param level
	 *            The level of the event.
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 */
	public static void write(LogEventLevel level, String messageTemplate, Object... propertyValues) {
		getLogger().write(level, messageTemplate, propertyValues);
	}

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
	public static void write(LogEventLevel level, Throwable exception, String messageTemplate,
			Object... propertyValues) {
		getLogger().write(level, exception, messageTemplate, propertyValues);
	}

	/**
	 * Determine if events at the specified level will be passed through to the
	 * log sinks.
	 * 
	 * @param level
	 *            Level to check.
	 * @return True if the level is enabled; otherwise, false.
	 */
	public static boolean isEnabled(LogEventLevel level) {
		return getLogger().isEnabled(level);
	}

	/**
	 * Write a log event with the <see cref="LogEventLevel.Verbose"/> level.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Verbose("Staring into space, wondering if we're
	 *            alone."); </example>
	 */
	public static void verbose(String messageTemplate, Object... propertyValues) {
		getLogger().verbose(messageTemplate, propertyValues);
	}

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
	public static void verbose(Throwable exception, String messageTemplate, Object... propertyValues) {
		getLogger().verbose(exception, messageTemplate, propertyValues);
	}

	/**
	 * Write a log event with the <see cref="LogEventLevel.Debug"/> level.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Debug("Starting up at {StartedAt}.",
	 *            DateTime.Now); </example>
	 */
	public static void debug(String messageTemplate, Object... propertyValues) {
		getLogger().debug(messageTemplate, propertyValues);
	}

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
	public static void debug(Throwable exception, String messageTemplate, Object... propertyValues) {
		getLogger().debug(exception, messageTemplate, propertyValues);
	}

	/**
	 * Write a log event with the <see cref="LogEventLevel.Information"/> level.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Information("Processed {RecordCount} records in
	 *            {TimeMS}.", records.Length, sw.ElapsedMilliseconds);
	 *            </example>
	 */
	public static void information(String messageTemplate, Object... propertyValues) {
		getLogger().information(messageTemplate, propertyValues);
	}

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
	public static void information(Throwable exception, String messageTemplate, Object... propertyValues) {
		getLogger().information(exception, messageTemplate, propertyValues);
	}

	/**
	 * Write a log event with the <see cref="LogEventLevel.Warning"/> level.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Warning("Skipped {SkipCount} records.",
	 *            skippedRecords.Length); </example>
	 */
	public static void warning(String messageTemplate, Object... propertyValues) {
		getLogger().warning(messageTemplate, propertyValues);
	}

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
	public static void warning(Throwable exception, String messageTemplate, Object... propertyValues) {
		getLogger().warning(exception, messageTemplate, propertyValues);
	}

	/**
	 * Write a log event with the <see cref="LogEventLevel.Error"/> level.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Error("Failed {ErrorCount} records.",
	 *            brokenRecords.Length); </example>
	 */
	public static void error(String messageTemplate, Object... propertyValues) {
		getLogger().error(messageTemplate, propertyValues);
	}

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
	public static void error(Throwable exception, String messageTemplate, Object... propertyValues) {
		getLogger().error(exception, messageTemplate, propertyValues);
	}

	/**
	 * Write a log event with the <see cref="LogEventLevel.Fatal"/> level.
	 * 
	 * @param messageTemplate
	 *            Message template describing the event.
	 * @param propertyValues
	 *            Objects positionally formatted into the message template.
	 *            <example> Log.Fatal("Process terminating."); </example>
	 */
	public static void fatal(String messageTemplate, Object... propertyValues) {
		getLogger().fatal(messageTemplate, propertyValues);
	}

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
	public static void fatal(Throwable exception, String messageTemplate, Object... propertyValues) {
		getLogger().fatal(exception, messageTemplate, propertyValues);
	}
}
