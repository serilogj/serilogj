package serilogj.core;

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
 * Determine how, when destructuring, a supplied value is represented as a
 * complex log event property.
 */
public interface IDestructuringPolicy {
	/**
	 * If supported, destructure the provided value.
	 * 
	 * @param value
	 *            The value to destructure.
	 * @param propertyValueFactory
	 *            Recursively apply policies to destructure additional values.
	 * @param result
	 *            The destructured value, or null.
	 * @return True if the value could be destructured under this policy.
	 */
	DestructuringPolicyResult tryDestructure(Object value, ILogEventPropertyValueFactory propertyValueFactory);
}