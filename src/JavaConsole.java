import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

import serilogj.Log;
import serilogj.core.IDestructuringPolicy;
import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventSink;
import serilogj.core.Logger;
import serilogj.core.sinks.SafeAggregateSink;
import serilogj.debugging.SelfLog;
import serilogj.events.*;
import serilogj.formatting.display.MessageTemplateTextFormatter;
import serilogj.parameters.MessageTemplateProcessor;
import serilogj.parameters.PropertyValueConverter;
import serilogj.sinks.coloredconsole.ColoredConsoleSink;
import serilogj.sinks.rollingfile.RollingFileSink;

public class JavaConsole {

	public static void main(String[] args) {
		SelfLog.setOut(System.out);
		
		ArrayList<ILogEventSink> sinks = new ArrayList<ILogEventSink>();
		sinks.add(new ColoredConsoleSink("{Timestamp} {Message}", null));
		sinks.add(new RollingFileSink("test-{Date}.log", null, 10, false, new MessageTemplateTextFormatter("{Timestamp:yyyy-MM-dd HH:mm:ss.SSS ZZZ} [{Level}] {Message}{NewLine}{Exception}", null)));

		Log.setLogger(new Logger(
				new MessageTemplateProcessor(new PropertyValueConverter(10, new java.lang.Class<?>[0], new IDestructuringPolicy[0])), 
				LogEventLevel.Verbose,
				new SafeAggregateSink(sinks),
				new ILogEventEnricher[0],
				null
				));

		User user = new User();
		user.userId = 1234;
		user.setUserName("blaat");
		
		Log.verbose("Hello {world} {@user}", "wereld", user);
		Log.debug("Hello {world} {@user}", "wereld", user);
		Log.information("Hello {world} {@user}", "wereld", user);
		Log.warning("Hello {world} {@user}", "wereld", user);
		Log.error("Hello {world} {@user}", "wereld", user);
		Log.fatal("Hello {world} {@user}", "wereld", user);
	}
}
