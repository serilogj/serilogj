package serilogj.events;

import java.util.*;
import java.io.*;

import serilogj.debugging.SelfLog;
import serilogj.parsing.*;

public class MessageTemplate {
	private String text;
	private ArrayList<MessageTemplateToken> tokens;
	private ArrayList<PropertyToken> namedTokens;
	private ArrayList<PropertyToken> positionalTokens;

	public MessageTemplate(String text, ArrayList<MessageTemplateToken> tokens) {
		if (text == null) {
			throw new IllegalArgumentException("text");
		}
		if (tokens == null) {
			throw new IllegalArgumentException("tokens");
		}

		this.text = text;
		this.tokens = tokens;

		ArrayList<PropertyToken> propertyTokens = new ArrayList<PropertyToken>();
		boolean anyPositional = false;
		boolean allPositional = true;
		for (MessageTemplateToken t : tokens) {
			if (!(t instanceof PropertyToken)) {
				continue;
			}

			PropertyToken token = (PropertyToken) t;
			propertyTokens.add(token);

			if (token.getIsPositional()) {
				anyPositional = true;
			} else {
				allPositional = false;
			}
		}

		if (allPositional) {
			positionalTokens = propertyTokens;
		} else {
			if (anyPositional) {
				SelfLog.writeLine("Message template is malformed: %1", text);
			}

			namedTokens = propertyTokens;
		}
	}

	public ArrayList<MessageTemplateToken> getTokens() {
		return tokens;
	}

	public String getText() {
		return text;
	}

	public ArrayList<PropertyToken> getNamedTokens() {
		return namedTokens;
	}

	public ArrayList<PropertyToken> getPositionalTokens() {
		return positionalTokens;
	}

	public void render(Map<String, LogEventPropertyValue> properties, Writer output, Locale locale) throws IOException {
		for (MessageTemplateToken token : tokens) {
			token.render(properties, output, locale);
		}
	}

	public String render(Map<String, LogEventPropertyValue> properties, Locale locale) {
		try {
			StringWriter output = new StringWriter();
			render(properties, output, locale);
			return output.toString();
		} catch (IOException e) {
			return "";
		}
	}

	@Override
	public String toString() {
		return getText();
	}
}
