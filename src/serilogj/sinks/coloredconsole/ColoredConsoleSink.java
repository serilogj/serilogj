package serilogj.sinks.coloredconsole;

import java.util.*;
import java.io.*;
import serilogj.core.*;
import serilogj.events.*;
import serilogj.formatting.display.*;
import serilogj.parsing.*;

public class ColoredConsoleSink implements ILogEventSink {
	private static String Prefix = "\033[";
	private static String Suffix = "m";
	private static String ANSI_RESET = Prefix + "0" + Suffix;

	private static class Palette {
		private Palette(String base, String highlight) {
			this.base = Prefix + base + Suffix;
			this.highlight = Prefix + highlight + Suffix;
		}

		public String base;
		public String highlight;
	}

	private static Palette DefaultPalette;
	private static Map<LogEventLevel, Palette> LevelPalettes = new HashMap<LogEventLevel, Palette>();

	static {
		DefaultPalette = new Palette("37;40", "36;40");
		LevelPalettes.put(LogEventLevel.Verbose, new Palette("36;40", "35;40"));
		LevelPalettes.put(LogEventLevel.Debug, new Palette("35;40", "34;40"));
		LevelPalettes.put(LogEventLevel.Information, new Palette("37;40", "37;44"));
		LevelPalettes.put(LogEventLevel.Warning, new Palette("33;40", "37;43"));
		LevelPalettes.put(LogEventLevel.Error, new Palette("31;40", "30;41"));
		LevelPalettes.put(LogEventLevel.Fatal, new Palette("30;41", "33;41"));
	}

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

		Palette palette = DefaultPalette;
		if (LevelPalettes.containsKey(logEvent.getLevel())) {
			palette = LevelPalettes.get(logEvent.getLevel());
		}

		Map<String, LogEventPropertyValue> properties = OutputProperties.GetOutputProperties(logEvent);
		try {
			for (MessageTemplateToken token : outputTemplate.getTokens()) {
				if (token instanceof PropertyToken) {
					PropertyToken propertyToken = (PropertyToken) token;
					if (!properties.containsKey(propertyToken.getPropertyName())) {
						continue;
					}

					switch (propertyToken.getPropertyName()) {
					case OutputProperties.MessagePropertyName:
						renderMessageToken(logEvent, palette, properties);
						break;
					case OutputProperties.ExceptionPropertyName:
						renderExceptionToken(token, palette, properties);
						break;
					default:
						renderOutputToken(token, palette, properties);
						break;
					}
				} else {
					renderOutputToken(token, palette, properties);
				}
			}
		} finally {
			System.out.print(ANSI_RESET);
		}
	}

	private void setBaseColors(Palette palette) {
		System.out.print(palette.base);
	}

	private void setHighlightColors(Palette palette) {
		System.out.print(palette.highlight);
	}

	private void renderMessageToken(LogEvent logEvent, Palette palette, Map<String, LogEventPropertyValue> properties) {
		for (MessageTemplateToken token : logEvent.getMessageTemplate().getTokens()) {
			if (token instanceof PropertyToken) {
				setHighlightColors(palette);
				renderToken(token, properties);
			} else {
				setBaseColors(palette);
				renderToken(token, properties);
			}
		}
	}

	private void renderExceptionToken(MessageTemplateToken outputToken, Palette palette,
			Map<String, LogEventPropertyValue> properties) {
		setHighlightColors(palette);
		renderToken(outputToken, properties);
	}

	private void renderOutputToken(MessageTemplateToken outputToken, Palette palette,
			Map<String, LogEventPropertyValue> properties) {
		setBaseColors(palette);
		renderToken(outputToken, properties);
	}

	private void renderToken(MessageTemplateToken outputToken, Map<String, LogEventPropertyValue> properties) {
		StringWriter writer = new StringWriter();
		try {
			outputToken.render(properties, writer, locale);
			System.out.print(writer.toString());
		} catch (IOException e) {
			// Won't happen
		}
	}
}
