package serilogj.core.enrichers;

import serilogj.context.LogContext;
import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventPropertyFactory;
import serilogj.events.LogEvent;

public class LogContextEnricher implements ILogEventEnricher {
	@Override
	public void enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory) {
		for(ILogEventEnricher enricher : LogContext.getEnrichers()) {
			enricher.enrich(logEvent, propertyFactory);
		}
	}
}
