package serilogj.debugging;

import java.io.PrintStream;
import java.util.Date;

//Copyright 2013-2015 Serilog Contributors
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.


/** 
A simple source of information generated by Serilog itself,
for example when exceptions are thrown and caught internally.
*/
public class SelfLog {
	private static PrintStream out;

	/** 
	 The output mechanism for self-log events.
	 
	 <example>
	 SelfLog.Out = Console.Error;
	 </example>
	*/
	public synchronized static PrintStream getOut() {
		return out;
	}
	
	public synchronized static void setOut(PrintStream value) {
		out = value;
	}

	/** 
	 Write a message to the self-log.
	 
	 @param format Standard .NET format string containing the message.
	 @param arg0 First argument, if supplied.
	 @param arg1 Second argument, if supplied.
	 @param arg2 Third argument, if supplied.
	*/
	public synchronized static void writeLine(String format, Object... parameters) {
		PrintStream o = getOut();
		if (o != null) {
			o.printf(new Date() + format, parameters);
			o.flush();
		}
	}

}