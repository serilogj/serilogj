package serilogj.parameters;

import java.util.ArrayList;

import serilogj.events.LogEventProperty;
import serilogj.events.MessageTemplate;

public class MessageTemplateProcessorResult {
	public MessageTemplate template;
	public ArrayList<LogEventProperty> properties;
}
