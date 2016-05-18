import serilogj.Log;
import serilogj.LoggerConfiguration;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEventLevel;

import static serilogj.sinks.coloredconsole.ColoredConsoleSinkConfigurator.*;
import static serilogj.sinks.rollingfile.RollingFileSinkConfigurator.*;

public class JavaConsole {

	public static void main(String[] args) {
		SelfLog.setOut(System.out);
		
		Log.setLogger(new LoggerConfiguration()
			.writeTo(coloredConsole())
			.writeTo(rollingFile("test-{Date}.log"), LogEventLevel.Information)
			.setMinimumLevel(LogEventLevel.Verbose)
			.createLogger());
		
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
