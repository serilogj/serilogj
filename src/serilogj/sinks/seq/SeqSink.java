package serilogj.sinks.seq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

import serilogj.core.LoggingLevelSwitch;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEvent;
import serilogj.events.LogEventLevel;
import serilogj.formatting.ITextFormatter;
import serilogj.formatting.json.JsonFormatter;
import serilogj.sinks.periodicbatching.PeriodicBatchingSink;

public class SeqSink extends PeriodicBatchingSink {
	public static final int DefaultBatchPostingLimit = 1000;
	public static final Duration DefaultPeriod = Duration.ofSeconds(2);
	private static final Duration RequiredLevelCheckInterval = Duration.ofMinutes(2);
	private static final String BulkUploadResource = "api/events/raw";
	private static final String ApiKeyHeaderName = "X-Seq-ApiKey";
	
	private URL baseUrl;
	private String serverUrl;
	private String apiKey;
	private Long eventBodyLimitBytes;
	private LoggingLevelSwitch levelSwitch;
	private LocalDateTime nextRequiredLevelCheck = LocalDateTime.now().plus(RequiredLevelCheckInterval);
		
	public SeqSink(String serverUrl, String apiKey, Integer batchSizeLimit, Duration period, Long eventBodyLimitBytes, LoggingLevelSwitch levelSwitch) {
		super(batchSizeLimit == null ? DefaultBatchPostingLimit : batchSizeLimit, period == null ? DefaultPeriod : period);
		
		if (serverUrl == null) {
			throw new IllegalArgumentException("serverUrl");
		}
		
		this.serverUrl = serverUrl;
		if (!this.serverUrl.endsWith("/")) {
			this.serverUrl += "/";
		}
		this.apiKey = apiKey;
		this.eventBodyLimitBytes = eventBodyLimitBytes;
		this.levelSwitch = levelSwitch;
		
		try {
			baseUrl = new URL(this.serverUrl + BulkUploadResource);
		} catch (MalformedURLException e) {
			SelfLog.writeLine("Invalid server url format: %s", this.serverUrl + BulkUploadResource);
			throw new IllegalArgumentException("serverUrl");
		}
	}

	@Override
	protected void emitBatch(Queue<LogEvent> events) {
		nextRequiredLevelCheck = LocalDateTime.now().plus(RequiredLevelCheckInterval);
		
		StringWriter payload = new StringWriter();
		payload.write("{\"Events\":[");
		
		ITextFormatter formatter = new JsonFormatter(false, "", false, null);
		String delimStart = "";
		try {
			for(LogEvent logEvent : events) {
				if (eventBodyLimitBytes != null) {
					StringWriter buffer = new StringWriter();
					formatter.format(logEvent, buffer);
					String buffered = buffer.toString();
					
					if (buffered.length() > eventBodyLimitBytes) {
						SelfLog.writeLine("Event JSON representation exceeds the byte size limit of %d set for this sink and will be dropped; data: %s", eventBodyLimitBytes, buffered);
					} else {
						payload.write(delimStart);
						formatter.format(logEvent, payload);
						delimStart = ",";
					}
				} else {
					payload.write(delimStart);
					formatter.format(logEvent, payload);
					delimStart = ",";
				}
			}
		} catch (IOException e) {
			// Never happens
		}
		
		payload.write("]}");
		
		try {
			HttpURLConnection con = (HttpURLConnection)baseUrl.openConnection();
			con.setRequestMethod("POST");
			if (apiKey != null && !apiKey.equals("")) {
				con.setRequestProperty(ApiKeyHeaderName, apiKey);
			}
			con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			con.setDoOutput(true);
		
			OutputStream os = con.getOutputStream();
			os.write(payload.toString().getBytes("UTF8"));
			os.flush();
			os.close();
			
			InputStream stream;
			int responseCode = con.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				stream = con.getErrorStream();
			} else {
				stream = con.getInputStream();
			}
			
			String line = "";
			String response = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF8"));
			while ((line = in.readLine()) != null) {
				response += line;
			}
			in.close();

			if (responseCode < 200 || responseCode >= 300) {
				throw new IOException(response);
			}
			
			LogEventLevel level = SeqApi.readEventInputResult(response);
			if (level != null && levelSwitch == null) {
				levelSwitch = new LoggingLevelSwitch(level);
			}
		} catch (IOException e) {
			SelfLog.writeLine("Error sending events to seq, exception %s", e.getMessage());
		}
	}
	
	@Override
	protected void onEmptyBatch() {
		if (levelSwitch != null && 
			nextRequiredLevelCheck.isBefore(LocalDateTime.now())) {
			emitBatch(new LinkedList<LogEvent>());
		}
	}
}
