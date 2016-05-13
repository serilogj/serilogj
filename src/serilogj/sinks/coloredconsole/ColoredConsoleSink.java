package serilogj.sinks.coloredconsole;

import java.util.*;
import java.io.*;
import serilogj.core.*;
import serilogj.events.*;
import serilogj.formatting.display.*;
import serilogj.parsing.*;

public class ColoredConsoleSink implements ILogEventSink {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	private MessageTemplate outputTemplate;
	private Locale locale;
	
	public ColoredConsoleSink(String outputTemplate, Locale locale) {
		if (outputTemplate == null) {
			throw new IllegalArgumentException("outputTemplate");
		}
		
		this.outputTemplate = new MessageTemplateParser().parse(outputTemplate);
		this.locale = locale;
	}
	
	@Override
	public synchronized void emit(LogEvent logEvent) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}
		
		Map<String, LogEventPropertyValue> properties = OutputProperties.GetOutputProperties(logEvent);
		try	{
			for(MessageTemplateToken token : outputTemplate.getTokens()) {
				if (token instanceof PropertyToken) {
					PropertyToken propertyToken = (PropertyToken)token;
					if (!properties.containsKey(propertyToken.getPropertyName())){
						continue;
					}
					
					switch(propertyToken.getPropertyName()) {
					case OutputProperties.MessagePropertyName:
						renderMessageToken(logEvent, properties);
						break;
					default:
						System.out.print(ANSI_YELLOW);
						renderOutputToken(token, properties);
						break;
					}
				} else {
					System.out.print(ANSI_WHITE);
					renderOutputToken(token, properties);
				}
			}
		} finally {
			System.out.println(ANSI_RESET);
		}
	}
	
	private void renderMessageToken(LogEvent logEvent, Map<String, LogEventPropertyValue> properties) {
		for(MessageTemplateToken token : logEvent.getMessageTemplate().getTokens()) {
			if (token instanceof PropertyToken) {
				System.out.print(ANSI_BLUE);
				renderOutputToken(token, properties);
			} else {
				System.out.print(ANSI_RED);
				renderOutputToken(token, properties);
			}
		}
	}
	
	private void renderOutputToken(MessageTemplateToken outputToken, Map<String, LogEventPropertyValue> properties) {
		StringWriter writer = new StringWriter();
		try	{
			outputToken.render(properties, writer, locale);
			System.out.print(writer.toString());
		} catch (IOException e) {
			// Won't happen
		}
	}
}
