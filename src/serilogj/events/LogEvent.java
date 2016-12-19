package serilogj.events;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LogEvent {
	private Map<String, LogEventPropertyValue> properties;
	private Date timestamp;
	private LogEventLevel level;
	private Throwable exception;
	private MessageTemplate messageTemplate;

	public LogEvent(Date timestamp, LogEventLevel level, Throwable exception, MessageTemplate messageTemplate,
			ArrayList<LogEventProperty> properties) {
		if (messageTemplate == null) {
			throw new IllegalArgumentException("messageTemplate");
		}
		if (properties == null) {
			throw new IllegalArgumentException("properties");
		}

		this.timestamp = timestamp;
		this.level = level;
		this.exception = exception;
		this.messageTemplate = messageTemplate;
		this.properties = new HashMap<String, LogEventPropertyValue>();
		for (LogEventProperty property : properties) {
			addOrUpdateProperty(property);
		}
	}

	public Map<String, LogEventPropertyValue> getProperties() {
		return properties;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public LogEventLevel getLevel() {
		return level;
	}

	public Throwable getException() {
		return exception;
	}

	public MessageTemplate getMessageTemplate() {
		return messageTemplate;
	}

	public void addOrUpdateProperty(LogEventProperty property) {
		if (property == null) {
			throw new IllegalArgumentException("property");
		}
		properties.put(property.getName(), property.getValue());
	}

	public void addPropertyIfAbsent(LogEventProperty property) {
		if (property == null) {
			throw new IllegalArgumentException("property");
		}
		if (properties.containsKey(property.getName())) {
			return;
		}
		properties.put(property.getName(), property.getValue());
	}

	public void remotePropertyIfPresent(String propertyName) {
		properties.remove(propertyName);
	}

	public void renderMessage(Writer output, Locale locale) throws IOException {
		messageTemplate.render(properties, output, locale);
	}

	public String renderMessage(Locale locale) {
		return messageTemplate.render(properties, locale);
	}
}
