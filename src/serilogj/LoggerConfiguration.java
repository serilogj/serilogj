package serilogj;

import serilogj.core.*;
import serilogj.events.*;
import serilogj.parameters.*;
import serilogj.core.sinks.*;
import java.util.*;

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
 * Configuration object for creating <see cref="ILogger"/> instances.
 */
public class LoggerConfiguration {
	private final ArrayList<ILogEventSink> logEventSinks = new ArrayList<ILogEventSink>();
	private final ArrayList<ILogEventEnricher> enrichers = new ArrayList<ILogEventEnricher>();
	private final ArrayList<ILogEventFilter> filters = new ArrayList<ILogEventFilter>();
	private final ArrayList<Class<?>> additionalScalarTypes = new ArrayList<Class<?>>();
	private final ArrayList<IDestructuringPolicy> additionalDestructuringPolicies = new ArrayList<IDestructuringPolicy>();

	private LogEventLevel minimumLevel = LogEventLevel.Information;
	private LoggingLevelSwitch levelSwitch;
	private int maximumDestructuringDepth = 10;
	private boolean loggerCreated;

	public LoggerConfiguration with(ILogEventEnricher enricher) {
		if (enricher == null) {
			throw new IllegalArgumentException("enricher");
		}
		enrichers.add(enricher);
		return this;
	}

	public LoggerConfiguration with(IDestructuringPolicy destructuringPolicy) {
		if (destructuringPolicy == null) {
			throw new IllegalArgumentException("destructuringPolicy");
		}
		additionalDestructuringPolicies.add(destructuringPolicy);
		return this;
	}

	public LoggerConfiguration writeTo(ILogEventSink sink) {
		if (sink == null) {
			throw new IllegalArgumentException("sink");
		}

		logEventSinks.add(sink);
		return this;
	}

	public LoggerConfiguration writeTo(ILogEventSink sink, LogEventLevel minimumLevel) {
		if (sink == null) {
			throw new IllegalArgumentException("sink");
		}
		if (minimumLevel == null) {
			throw new IllegalArgumentException("minimumLevel");
		}

		if (minimumLevel.ordinal() > LevelAlias.Minimum.ordinal()) {
			sink = new RestrictedSink(sink, new LoggingLevelSwitch(minimumLevel));
		}
		return writeTo(sink);
	}

	public LoggerConfiguration writeTo(ILogEventSink sink, LoggingLevelSwitch levelSwitch) {
		if (sink == null) {
			throw new IllegalArgumentException("sink");
		}
		if (levelSwitch == null) {
			throw new IllegalArgumentException("levelSwitch");
		}

		sink = new RestrictedSink(sink, levelSwitch);
		return writeTo(sink);
	}

	public LoggerConfiguration asScalar(Class<?> type) {
		if (type == null) {
			throw new IllegalArgumentException("type");
		}

		additionalScalarTypes.add(type);
		return this;
	}

	public LoggerConfiguration setMaximumDestructuringDepth(int maximumDestructuringDepth) {
		this.maximumDestructuringDepth = maximumDestructuringDepth;
		return this;
	}

	public LoggerConfiguration setMinimumLevel(LogEventLevel minimumLevel) {
		this.minimumLevel = minimumLevel;
		return this;
	}

	/**
	 * Create a logger using the configured sinks, enrichers and minimum level.
	 * 
	 * @return The logger. To free resources held by sinks ahead of program
	 *         shutdown, the returned logger may be cast to
	 *         <see cref="IDisposable"/> and disposed.
	 */
	public Logger createLogger() {
		if (loggerCreated) {
			throw new IllegalStateException("CreateLogger was previously called and can only be called once.");
		}
		loggerCreated = true;

		SafeAggregateSink sink = new SafeAggregateSink(logEventSinks);

		if (filters.size() > 0) {
			sink = new SafeAggregateSink(new FilteringSink[] { new FilteringSink(sink, filters) });
		}

		PropertyValueConverter converter = new PropertyValueConverter(maximumDestructuringDepth,
				additionalScalarTypes.toArray(new Class<?>[0]),
				additionalDestructuringPolicies.toArray(new IDestructuringPolicy[0]));
		MessageTemplateProcessor processor = new MessageTemplateProcessor(converter);

		return new Logger(processor, minimumLevel, sink, enrichers.toArray(new ILogEventEnricher[0]), levelSwitch, true);
	}
}
