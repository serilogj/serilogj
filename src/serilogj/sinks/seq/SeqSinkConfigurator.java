package serilogj.sinks.seq;

import serilogj.core.ILogEventSink;

public class SeqSinkConfigurator {
	public static ILogEventSink seq(String serverUrl) {
		return seq(serverUrl, null);
	}

	public static ILogEventSink seq(String serverUrl, String apiKey) {
		return new SeqSink(serverUrl, apiKey, null, null, null, null);
	}
}
