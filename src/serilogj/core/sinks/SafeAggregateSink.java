package serilogj.core.sinks;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import serilogj.events.*;
import serilogj.core.*;
import serilogj.debugging.SelfLog;

// Copyright 2013-2015 Serilog Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

public class SafeAggregateSink implements ILogEventSink, Closeable {
	private ArrayList<ILogEventSink> sinks;

	public SafeAggregateSink(ILogEventSink[] sinks) {
		if (sinks == null) {
			throw new IllegalArgumentException("sinks");
		}

		this.sinks = new ArrayList<ILogEventSink>();
		for (ILogEventSink sink : sinks) {
			this.sinks.add(sink);
		}
	}

	public SafeAggregateSink(ArrayList<ILogEventSink> sinks) {
		if (sinks == null) {
			throw new IllegalArgumentException("sinks");
		}
		this.sinks = sinks;
	}

	public final void emit(LogEvent logEvent) {
		for (ILogEventSink sink : sinks) {
			try {
				sink.emit(logEvent);
			} catch (RuntimeException ex) {
				SelfLog.writeLine("Caught exception %s while emitting to sink %s.", ex.getMessage(), sink);
			}
		}
	}

	@Override
	public void close() throws IOException {
		for (ILogEventSink sink : sinks) {
			if (sink instanceof java.io.Closeable) {
				((java.io.Closeable) sink).close();
			}
		}
	}
}
