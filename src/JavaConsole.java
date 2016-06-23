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

		Log.setLogger(new LoggerConfiguration().writeTo(coloredConsole("{blaat} {Message} {NewLine}"))
				.writeTo(rollingFile("test-{Date}.log"), LogEventLevel.Verbose)
				.writeTo(seq("http://localhost:5341/")).setMinimumLevel(LogEventLevel.Verbose)
				.with(new LogContextEnricher()).with(new UserDestructor()).createLogger());

		try (AutoCloseable property = LogContext.pushProperty("blaat", "1")) {
			Log.information("test");
			try (AutoCloseable other = LogContext.pushProperty("blaat", "2")) {
				Log.information("test");
			}
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * Log.verbose("Hello {world} {@user}", "wereld", user); Log.debug(
		 * "Hello {world} {@user}", "wereld", user); Log.information(
		 * "Hello {world} {@user}", "wereld", user); Log.warning(
		 * "Hello {world} {@user}", "wereld", user); Log.error(
		 * "Hello {world} {@user}", "wereld", user); Log.fatal(
		 * "Hello {world} {@user}", "wereld", user);
		 */

		//Log.forContext(JavaConsole.class).forContext(User.class).information("blaat");
	}
}
