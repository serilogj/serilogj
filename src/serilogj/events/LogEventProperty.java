package serilogj.events;

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
 * A property associated with a <see cref="LogEvent"/>.
 */
public class LogEventProperty {
	private String name;
	private LogEventPropertyValue value;

	/**
	 * Construct a <see cref="LogEventProperty"/> with the specified name and
	 * value.
	 * 
	 * @param name
	 *            The name of the property.
	 * @param value
	 *            The value of the property.
	 * @exception ArgumentException
	 * @exception ArgumentNullException
	 */
	public LogEventProperty(String name, LogEventPropertyValue value) {
		if (value == null) {
			throw new IllegalArgumentException("value");
		}
		if (!isValidName(name)) {
			throw new IllegalArgumentException("Property name is not valid.");
		}

		this.name = name;
		this.value = value;
	}

	/**
	 * The name of the property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The value of the property.
	 */
	public LogEventPropertyValue getValue() {
		return value;
	}

	/**
	 * Test <paramref name="name" /> to determine if it is a valid property
	 * name.
	 * 
	 * @param name
	 *            The name to check.
	 * @return True if the name is valid; otherwise, false.
	 */
	public static boolean isValidName(String name) {
		if (name == null) {
			return false;
		}

		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) != ' ') {
				return true;
			}
		}

		return false;
	}
}
