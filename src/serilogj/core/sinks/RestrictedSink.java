package serilogj.core.sinks;

import serilogj.events.*;
import serilogj.core.*;

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

public class RestrictedSink implements ILogEventSink {
	private ILogEventSink sink;
	private LoggingLevelSwitch levelSwitch;

	public RestrictedSink(ILogEventSink sink, LoggingLevelSwitch levelSwitch) {
		if (sink == null) {
			throw new IllegalArgumentException("sink");
		}
		if (levelSwitch == null) {
			throw new IllegalArgumentException("levelSwitch");
		}

		this.sink = sink;
		this.levelSwitch = levelSwitch;
	}

	public void emit(LogEvent logEvent) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}

		if (logEvent.getLevel().ordinal() < levelSwitch.getMinimumLevel().ordinal()) {
			return;
		}

		sink.emit(logEvent);
	}
}
