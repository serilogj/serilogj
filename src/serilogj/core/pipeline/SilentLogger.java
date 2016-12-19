package serilogj.core.pipeline;

import serilogj.ILogger;
import serilogj.core.ILogEventEnricher;
import serilogj.events.LogEvent;
import serilogj.events.LogEventLevel;

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

public class SilentLogger implements ILogger {

	@Override
	public ILogger forContext(ILogEventEnricher[] enrichers) {
		return this;
	}

	@Override
	public ILogger forContext(String propertyName, Object value) {
		return this;
	}

	@Override
	public ILogger forContext(String propertyName, Object value, boolean destructureObjects) {
		return this;
	}

	@Override
	public ILogger forContext(Class<?> source) {
		return this;
	}

	@Override
	public void write(LogEvent logEvent) {
	}

	@Override
	public void write(LogEventLevel level, String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void write(LogEventLevel level, Throwable exception, String messageTemplate, Object... propertyValues) {
	}

	@Override
	public boolean isEnabled(LogEventLevel level) {
		return false;
	}

	@Override
	public void verbose(String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void verbose(Throwable exception, String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void debug(String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void debug(Throwable exception, String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void information(String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void information(Throwable exception, String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void warning(String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void warning(Throwable exception, String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void error(String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void error(Throwable exception, String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void fatal(String messageTemplate, Object... propertyValues) {
	}

	@Override
	public void fatal(Throwable exception, String messageTemplate, Object... propertyValues) {
	}
}
