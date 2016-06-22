package serilogj.sinks.rollingfile;

import serilogj.core.ILogEventSink;
import serilogj.formatting.ITextFormatter;
import serilogj.formatting.display.MessageTemplateTextFormatter;

public class RollingFileSinkConfigurator {
	private static final int DefaultRetainedFileCountLimit = 31; // A long month
																	// of logs
	private static final long DefaultFileSizeLimitBytes = 1L * 1024 * 1024 * 1024;
	private static final String DefaultOutputTemplate = "{Timestamp:yyyy-MM-dd HH:mm:ss.SSS zzz} [{Level}] {Message}{NewLine}{Exception}";

	public static ILogEventSink rollingFile(String pathFormat) {
		return rollingFile(pathFormat, DefaultOutputTemplate);
	}

	public static ILogEventSink rollingFile(String pathFormat, int retainedFileCountLimit) {
		return rollingFile(pathFormat, DefaultOutputTemplate, retainedFileCountLimit);
	}

	public static ILogEventSink rollingFile(String pathFormat, String outputTemplate) {
		return rollingFile(pathFormat, DefaultFileSizeLimitBytes, DefaultRetainedFileCountLimit, false,
				new MessageTemplateTextFormatter(outputTemplate, null));
	}

	public static ILogEventSink rollingFile(String pathFormat, String outputTemplate, int retainedFileCountLimit) {
		return rollingFile(pathFormat, DefaultFileSizeLimitBytes, retainedFileCountLimit, false,
				new MessageTemplateTextFormatter(outputTemplate, null));
	}

	public static ILogEventSink rollingFile(String pathFormat, Long fileSizeLimitBytes, Integer retainedFileCountLimit,
			boolean buffered, ITextFormatter formatter) {
		return new RollingFileSink(pathFormat, fileSizeLimitBytes, retainedFileCountLimit, buffered, formatter);
	}
}
