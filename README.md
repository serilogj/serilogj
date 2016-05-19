# serilogj

serilogj is a structured logger that is an almost 1-on-1 code conversion of Serilog for .NET (by Nicholas Blumhardt). Not everything has been converted, but the a lot of functionality is included in this conversion. Using this in combination with Seq (www.getseq.net) will make searching through log files a lot easier.

## usage

Setup the logger using the desired settings, as such:
```
		Log.setLogger(new LoggerConfiguration()
			.writeTo(coloredConsole())
			.writeTo(rollingFile("test-{Date}.log"), LogEventLevel.Information)
			.writeTo(seq("http://localhost:5341/"))
			.setMinimumLevel(LogEventLevel.Verbose)
			.createLogger());
```

After this you can log using the following example code:
```
		User user = new User();
		user.userId = 1234;
		user.setUserName("blaat");
		
		Log.verbose("Hello {world} {@user}", "wereld", user);
		Log.debug("Hello {world} {@user}", "wereld", user);
		Log.information("Hello {world} {@user}", "wereld", user);
		Log.warning("Hello {world} {@user}", "wereld", user);
		Log.error("Hello {world} {@user}", "wereld", user);
		Log.fatal("Hello {world} {@user}", "wereld", user);
```

## known issues

* Formatting is only partially supported (Only for date's)
* Only a limited amount of sinks have been converted (colored console, rolling file and seq)
* Cleanup of the logger is not properly done if the application closes (events might not all be send to Seq or written to disk)
* ...
 

