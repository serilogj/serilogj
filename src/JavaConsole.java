import serilogj.Log;
import serilogj.core.IDestructuringPolicy;
import serilogj.core.ILogEventEnricher;
import serilogj.core.Logger;
import serilogj.debugging.SelfLog;
import serilogj.events.*;
import serilogj.parameters.MessageTemplateProcessor;
import serilogj.parameters.PropertyValueConverter;
import serilogj.sinks.coloredconsole.ColoredConsoleSink;

public class JavaConsole {

	public static void main(String[] args) {
		SelfLog.setOut(System.out);

		Log.setLogger(new Logger(
				new MessageTemplateProcessor(new PropertyValueConverter(10, new java.lang.Class<?>[0], new IDestructuringPolicy[0])), 
				LogEventLevel.Verbose,
				new ColoredConsoleSink("{Timestamp} {Message}", null),
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
