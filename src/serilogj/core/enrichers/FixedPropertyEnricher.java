package serilogj.core.enrichers;

import serilogj.core.ILogEventEnricher;
import serilogj.core.ILogEventPropertyFactory;
import serilogj.events.LogEvent;
import serilogj.events.LogEventProperty;

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

public class FixedPropertyEnricher implements ILogEventEnricher {
	private LogEventProperty logEventProperty;

	public FixedPropertyEnricher(LogEventProperty logEventProperty) {
		this.logEventProperty = logEventProperty;
	}

	@Override
	public void enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory) {
		logEvent.addPropertyIfAbsent(logEventProperty);
	}

	public String getName() {
		return logEventProperty.getName();
	}
}
