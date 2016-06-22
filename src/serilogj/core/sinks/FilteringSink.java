package serilogj.core.sinks;

import java.util.*;
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

public class FilteringSink implements ILogEventSink {
	private ILogEventSink sink;
	private ArrayList<ILogEventFilter> filters;

	public FilteringSink(ILogEventSink sink, ArrayList<ILogEventFilter> filters) {
		if (sink == null) {
			throw new IllegalArgumentException("sink");
		}
		if (filters == null) {
			throw new IllegalArgumentException("filters");
		}

		this.sink = sink;
		this.filters = filters;
	}

	public final void emit(LogEvent logEvent) {
		for (ILogEventFilter logEventFilter : filters) {
			if (!logEventFilter.isEnabled(logEvent)) {
				return;
			}
		}

		sink.emit(logEvent);
	}
}
