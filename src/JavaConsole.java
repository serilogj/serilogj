import serilogj.ILogger;
import serilogj.Log;
import serilogj.core.IDestructuringPolicy;
import serilogj.core.ILogEventEnricher;
import serilogj.core.Logger;
import serilogj.core.pipeline.MessageTemplateCache;
import serilogj.debugging.SelfLog;
import serilogj.events.*;
import serilogj.parameters.MessageTemplateProcessor;
import serilogj.parameters.PropertyValueConverter;
import serilogj.parsing.*;
import serilogj.sinks.coloredconsole.ColoredConsoleSink;

import java.util.*;

public class JavaConsole {

	public static void main(String[] args) {
		SelfLog.setOut(System.out);
		
/*		MessageTemplateParser parser = new MessageTemplateParser();
		MessageTemplate template = parser.parse("hello world {knal}");
		ArrayList<LogEventProperty> properties = new ArrayList<LogEventProperty>();
		properties.add(new LogEventProperty("knal", new ScalarValue("blaat")));
		LogEvent event = new LogEvent(new Date(), LogEventLevel.Information, null, template, properties);
		System.out.println(event.renderMessage(null));
		
		HashMap<String, Integer> test = new HashMap<String, Integer>();
		//test.put("hello", "world");
		test.put("helloworld", 1234);
		test.put("helloworld2", 1264);
		//test.put(1234, "51231");
		
		PropertyValueConverter converter = new PropertyValueConverter(10, new java.lang.Class<?>[0], new IDestructuringPolicy[0]);
		LogEventPropertyValue value = converter.createPropertyValue(test);
		System.out.printf("%s", value);
		
		User user = new User();
		user.userId = 1234;
		user.setUserName("blaat");
		value = converter.createPropertyValue(user, true);
		System.out.printf("%s", value);
*/		
		Log.setLogger(new Logger(
				new MessageTemplateProcessor(new PropertyValueConverter(10, new java.lang.Class<?>[0], new IDestructuringPolicy[0])), 
				LogEventLevel.Verbose,
				new ColoredConsoleSink("{Timestamp} {Message}", null),
				new ILogEventEnricher[0],
				null
				));
		Log.information("Hello {world}", "wereld");
		Log.information("Hello {world}", "wereld");
		Log.information("Hello {world}", "wereld");
		Log.information("Hello {world}", "wereld");
		Log.information("Hello {world}", "wereld");
	}
}
