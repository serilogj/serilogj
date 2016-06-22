package serilogj.parsing;

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
 * Instructs the logger on how to store information about provided parameters.
 */
public enum Destructuring {
	/**
	 * Convert known types and objects to scalars, arrays to sequences.
	 */
	Default,

	/**
	 * Convert all types to scalar strings. Prefix name with '$'.
	 */
	Stringify,

	/**
	 * Convert known types to scalars, destructure objects and collections into
	 * sequences and structures. Prefix name with '@'.
	 */
	Destructure;

	public int getValue() {
		return this.ordinal();
	}

	public static Destructuring forValue(int value) {
		return values()[value];
	}
}
