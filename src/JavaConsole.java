import serilogj.Log;
import serilogj.LoggerConfiguration;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEventLevel;
import serilogj.sinks.seq.SeqSink;

import static serilogj.sinks.coloredconsole.ColoredConsoleSinkConfigurator.*;
import static serilogj.sinks.rollingfile.RollingFileSinkConfigurator.*;
import static serilogj.sinks.seq.SeqSinkConfigurator.*;

import java.io.IOException;

public class JavaConsole {

	public static void main(String[] args) {
		SelfLog.setOut(System.out);
		
		Log.setLogger(new LoggerConfiguration()
			.writeTo(coloredConsole())
			.writeTo(rollingFile("test-{Date}.log"), LogEventLevel.Information)
			.writeTo(seq("http://localhost:5341/", "P0lxYRO7pZ5cYPfLB8eh"))
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
		
		try {
	        System.in.read();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
}
