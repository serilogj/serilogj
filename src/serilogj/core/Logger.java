package serilogj.core;

import java.io.Closeable;
import java.util.Date;
import java.io.IOException;

import serilogj.ILogger;
import serilogj.core.enrichers.FixedPropertyEnricher;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEvent;
import serilogj.events.LogEventLevel;
import serilogj.parameters.MessageTemplateProcessor;
import serilogj.parameters.MessageTemplateProcessorResult;

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

public class Logger implements ILogger, ILogEventSink, Closeable {
	private final MessageTemplateProcessor messageTemplateProcessor;
	private final ILogEventSink sink;
	private final ILogEventEnricher[] enrichers;
	private final Boolean closeSink;

	// It's important that checking minimum level is a very
	// quick (CPU-cacheable) read in the simple case, hence
	// we keep a separate field from the switch, which may
	// not be specified. If it is, we'll set _minimumLevel
	// to its lower limit and fall through to the secondary check.
	private LogEventLevel minimumLevel = LogEventLevel.Verbose;
	private LoggingLevelSwitch levelSwitch;

	public Logger(MessageTemplateProcessor messageTemplateProcessor, LogEventLevel minimumLevel, ILogEventSink sink,
			ILogEventEnricher[] enrichers, LoggingLevelSwitch levelSwitch, Boolean closeSink) {
		if (sink == null) {
			throw new IllegalArgumentException("sink");
		}
		if (enrichers == null) {
			throw new IllegalArgumentException("enrichers");
		}

		this.messageTemplateProcessor = messageTemplateProcessor;
		this.minimumLevel = minimumLevel;
		this.sink = sink;
		this.levelSwitch = levelSwitch;
		this.enrichers = enrichers;
		this.closeSink = closeSink;
	}

	@Override
	public void emit(LogEvent logEvent) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}
		write(logEvent);
	}

	@Override
	public ILogger forContext(ILogEventEnricher[] enrichers) {
		return new Logger(messageTemplateProcessor, minimumLevel, this,
				enrichers != null ? enrichers : new ILogEventEnricher[0], levelSwitch, false);
	}

	@Override
	public ILogger forContext(String propertyName, Object value) {
		return forContext(propertyName, value, false);
	}

	@Override
	public ILogger forContext(String propertyName, Object value, boolean destructureObjects) {
		int found = -1;
		for (int i = 0; i < this.enrichers.length; i++) {
			FixedPropertyEnricher property = this.enrichers[i] instanceof FixedPropertyEnricher
					? (FixedPropertyEnricher) this.enrichers[i] : null;
			if (property == null || !propertyName.equals(property.getName())) {
				continue;
			}

			found = i;
			break;
		}

		ILogEventEnricher[] enrichers = new ILogEventEnricher[this.enrichers.length + (found == -1 ? 1 : 0)];
		for (int i = 0; i < this.enrichers.length; i++) {
			enrichers[i] = this.enrichers[i];
		}
		enrichers[found == -1 ? enrichers.length - 1 : found] = new FixedPropertyEnricher(
				messageTemplateProcessor.createProperty(propertyName, value, destructureObjects));
		return forContext(enrichers);
	}

	@Override
	public ILogger forContext(Class<?> source) {
		if (source == null) {
			throw new IllegalArgumentException("source");
		}
		return forContext(Constants.SourceContextPropertyName, source.getName(), false);
	}

	private void dispatch(LogEvent logEvent) {
		for (ILogEventEnricher enricher : enrichers) {
			try {
				enricher.enrich(logEvent, messageTemplateProcessor);
			} catch (Exception ex) {
				SelfLog.writeLine("Exception %s caught while enriching %s with %s.", ex, logEvent, enricher);
			}
		}

		sink.emit(logEvent);
	}
	
	@Override
	public void close() throws IOException {
		if (closeSink && sink instanceof java.io.Closeable) {
			((java.io.Closeable) sink).close();
		}
	}

	@Override
	public void write(LogEvent logEvent) {
		if (logEvent == null) {
			return;
		}
		if (!isEnabled(logEvent.getLevel())) {
			return;
		}
		dispatch(logEvent);
	}

	@Override
	public void write(LogEventLevel level, String messageTemplate, Object... propertyValues) {
		write(level, null, messageTemplate, propertyValues);
	}

	@Override
	public void write(LogEventLevel level, Throwable exception, String messageTemplate, Object... propertyValues) {
		if (messageTemplate == null) {
			return;
		}
		if (!isEnabled(level)) {
			return;
		}
		if (propertyValues != null && propertyValues.length == 1 && propertyValues[0] != null
				&& propertyValues[0].getClass() == Object[].class) {
			propertyValues = (Object[]) propertyValues[0];
		}

		Date now = new Date();
		MessageTemplateProcessorResult result = messageTemplateProcessor.process(messageTemplate, propertyValues);
		LogEvent event = new LogEvent(now, level, exception, result.template, result.properties);
		dispatch(event);
	}

	@Override
	public boolean isEnabled(LogEventLevel level) {
		if (level.ordinal() < minimumLevel.ordinal()) {
			return false;
		}
		return levelSwitch == null || level.ordinal() >= levelSwitch.getMinimumLevel().ordinal();
	}

	@Override
	public void verbose(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Verbose, messageTemplate, propertyValues);
	}

	@Override
	public void verbose(Throwable exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Verbose, exception, messageTemplate, propertyValues);
	}

	@Override
	public void debug(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Debug, messageTemplate, propertyValues);
	}

	@Override
	public void debug(Throwable exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Debug, exception, messageTemplate, propertyValues);
	}

	@Override
	public void information(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Information, messageTemplate, propertyValues);
	}

	@Override
	public void information(Throwable exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Information, exception, messageTemplate, propertyValues);
	}

	@Override
	public void warning(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Warning, messageTemplate, propertyValues);
	}

	@Override
	public void warning(Throwable exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Warning, exception, messageTemplate, propertyValues);
	}

	@Override
	public void error(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Error, messageTemplate, propertyValues);
	}

	@Override
	public void error(Throwable exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Error, exception, messageTemplate, propertyValues);
	}

	@Override
	public void fatal(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Fatal, messageTemplate, propertyValues);
	}

	@Override
	public void fatal(Throwable exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Fatal, exception, messageTemplate, propertyValues);
	}
}
