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

/**
 * Adds a new property encricher to the log event.
 */
public class PropertyEnricher implements ILogEventEnricher {
	private String name;
	private Object value;
	private boolean destructureObjects;

	/**
	 * Create a new property enricher.
	 * 
	 * @param name
	 *            The name of the property.
	 * @param value
	 *            The value of the property.
	 * @return A handle to later remove the property from the context.
	 * @param destructureObjects
	 *            If true, and the value is a non-primitive, non-array type,
	 *            then the value will be converted to a structure; otherwise,
	 *            unknown types will be converted to scalars, which are
	 *            generally stored as strings.
	 * @return
	 * @exception ArgumentNullException
	 */
	public PropertyEnricher(String name, Object value, boolean destructureObjects) {
		if (!LogEventProperty.isValidName(name)) {
			throw new IllegalArgumentException("name");
		}

		this.name = name;
		this.value = value;
		this.destructureObjects = destructureObjects;
	}

	/**
	 * Enrich the log event.
	 * 
	 * @param logEvent
	 *            The log event to enrich.
	 * @param propertyFactory
	 *            Factory for creating new properties to add to the event.
	 */
	public final void enrich(LogEvent logEvent, ILogEventPropertyFactory propertyFactory) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}
		if (propertyFactory == null) {
			throw new IllegalArgumentException("propertyFactory");
		}
		LogEventProperty property = propertyFactory.createProperty(name, value, destructureObjects);
		logEvent.addPropertyIfAbsent(property);
	}

	public String getName() {
		return name;
	}
	
	public Object getValue() {
		return value;
	}
}
