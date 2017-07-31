import serilogj.Log;
import serilogj.LoggerConfiguration;
import serilogj.context.LogContext;
import serilogj.core.enrichers.LogContextEnricher;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEventLevel;
import static serilogj.sinks.coloredconsole.ColoredConsoleSinkConfigurator.*;
import static serilogj.sinks.rollingfile.RollingFileSinkConfigurator.*;
import static serilogj.sinks.seq.SeqSinkConfigurator.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class JavaConsole {

	public static void main(String[] args) {
		SelfLog.setOut(System.out);

		Log.setLogger(new LoggerConfiguration()
				.setMinimumLevel(LogEventLevel.Verbose)
				.with(new LogContextEnricher())
				.with(new UserDestructor())
				.writeTo(coloredConsole("[{Timestamp} {Level}] {Message} ({Operation}){NewLine}{Exception}"))
				.writeTo(rollingFile("test-{Date}.log"))
				.writeTo(seq("http://localhost:5341/"))
				.createLogger());

		try {
			try (AutoCloseable property = LogContext.pushProperty("Operation", 1)) {
				Log.information("In outer operation");
				try (AutoCloseable other = LogContext.pushProperty("Operation", 2)) {
					Log.information("In inner operation");
				}
			}

			for (int i = 0; i < 10; ++i) {
				Log.debug("Running iteration {Number}", i);
				Thread.sleep(2000);
			}

			User user = new User();
			user.setUserName(System.getProperty("user.name"));
			Log.forContext(JavaConsole.class).warning("Hello {name} from {@user}", "World", user);

			throw new Exception("Something went wrong");
		} catch (Exception ex) {
			Log.error(ex, "An error occurred");
		}

		Log.closeAndFlush();
	}
}
