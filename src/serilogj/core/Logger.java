package serilogj.core;

import serilogj.core.enrichers.*;
import serilogj.debugging.*;
import serilogj.events.*;
import serilogj.parameters.*;
import java.util.Date;
import serilogj.*;

//Copyright 2013-2015 Serilog Contributors
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

public class Logger implements ILogger, ILogEventSink {
	private MessageTemplateProcessor messageTemplateProcessor;
	private ILogEventSink sink;
	private ILogEventEnricher[] enrichers;
	
	// It's important that checking minimum level is a very
	// quick (CPU-cacheable) read in the simple case, hence
	// we keep a separate field from the switch, which may
	// not be specified. If it is, we'll set _minimumLevel
	// to its lower limit and fall through to the secondary check.
	private LogEventLevel minimumLevel = LogEventLevel.Verbose;
	private LoggingLevelSwitch levelSwitch;

	public Logger(MessageTemplateProcessor messageTemplateProcessor, 
			LogEventLevel minimumLevel, 
			ILogEventSink sink, 
			ILogEventEnricher[] enrichers, 
			LoggingLevelSwitch levelSwitch) {
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
		return new Logger(messageTemplateProcessor, minimumLevel, this, enrichers != null ? enrichers : new ILogEventEnricher[0], levelSwitch);
	}

	@Override
	public ILogger forContext(String propertyName, Object value, boolean destructureObjects) {
		ILogEventEnricher[] enrichers = new ILogEventEnricher[1];
		enrichers[0] = new FixedPropertyEnricher(messageTemplateProcessor.createProperty(propertyName, value, destructureObjects));
		return forContext(enrichers);
	}

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
			} catch (RuntimeException ex) {
				SelfLog.writeLine("Exception %s caught while enriching %s with %s.", ex, logEvent, enricher);
			}
		}

		sink.emit(logEvent);
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
	public void write(LogEventLevel level, Exception exception, String messageTemplate, Object... propertyValues) {
		if (messageTemplate == null) {
			return;
		}
		if (!isEnabled(level)) {
			return;
		}
		if (propertyValues != null && propertyValues.length == 1 && propertyValues[0] != null && propertyValues[0].getClass() == Object[].class) {
			propertyValues = (Object[])propertyValues[0];
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
	public void verbose(Exception exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Verbose, exception, messageTemplate, propertyValues);	
	}

	@Override
	public void debug(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Debug, messageTemplate, propertyValues);	
	}

	@Override
	public void debug(Exception exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Debug, exception, messageTemplate, propertyValues);	
	}

	@Override
	public void information(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Information, messageTemplate, propertyValues);	
	}

	@Override
	public void information(Exception exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Information, exception, messageTemplate, propertyValues);	
	}

	@Override
	public void warning(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Warning, messageTemplate, propertyValues);	
	}

	@Override
	public void warning(Exception exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Warning, exception, messageTemplate, propertyValues);	
	}

	@Override
	public void error(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Error, messageTemplate, propertyValues);	
	}

	@Override
	public void error(Exception exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Error, exception, messageTemplate, propertyValues);	
	}

	@Override
	public void fatal(String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Fatal, messageTemplate, propertyValues);	
	}

	@Override
	public void fatal(Exception exception, String messageTemplate, Object... propertyValues) {
		write(LogEventLevel.Fatal, exception, messageTemplate, propertyValues);	
	}
}
