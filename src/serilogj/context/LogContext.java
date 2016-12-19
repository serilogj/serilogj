package serilogj.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import serilogj.core.ILogEventEnricher;
import serilogj.core.enrichers.PropertyEnricher;

public class LogContext {
	private static class LogContextItem implements AutoCloseable {
		private PropertyEnricher enricher;

		private LogContextItem(String name, Object value, boolean destructureObjects) {
			enricher = new PropertyEnricher(name, value, destructureObjects);
		}

		private PropertyEnricher getEnricher() {
			return enricher;
		}

		@Override
		public void close() {
			popProperty(this);
		}
	}

	private static ThreadLocal<ArrayList<LogContextItem>> properties = new ThreadLocal<ArrayList<LogContextItem>>() {
		protected ArrayList<LogContextItem> initialValue() {
			return new ArrayList<LogContextItem>();
		};
	};

	public static AutoCloseable pushProperty(String name, Object value, boolean destructureObjects) {
		LogContextItem item = new LogContextItem(name, value, destructureObjects);
		properties.get().add(item);
		return item;
	}

	public static AutoCloseable pushProperty(String name, Object value) {
		return pushProperty(name, value, false);
	}

	private static void popProperty(LogContextItem item) {
		properties.get().remove(item);
	}

	public static ILogEventEnricher[] getEnrichers() {
		Map<String, ILogEventEnricher> enrichers = new HashMap<String, ILogEventEnricher>();
		for (int i = properties.get().size() - 1; i >= 0; i--) {
			PropertyEnricher enricher = properties.get().get(i).getEnricher();
			if (enrichers.containsKey(enricher.getName())) {
				continue;
			}
			enrichers.put(enricher.getName(), enricher);
		}
		return enrichers.values().toArray(new ILogEventEnricher[0]);
	}
}
