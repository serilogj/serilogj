package serilogj.policies;

import java.util.HashSet;

import serilogj.core.ILogEventPropertyValueFactory;
import serilogj.core.IScalarConversionPolicy;
import serilogj.core.ScalarConversionPolicyResult;
import serilogj.events.ScalarValue;

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

public class SimpleScalarConversionPolicy implements IScalarConversionPolicy {
	private HashSet<Class<?>> scalarTypes;

	public SimpleScalarConversionPolicy(HashSet<Class<?>> scalarTypes) {
		this.scalarTypes = scalarTypes;
	}

	@Override
	public ScalarConversionPolicyResult tryConvertToScalar(Object value,
			ILogEventPropertyValueFactory propertyValueFactory) {
		ScalarConversionPolicyResult result = new ScalarConversionPolicyResult();
		if (scalarTypes.contains(value.getClass()) || value.getClass().isPrimitive()) {
			result.isValid = true;
			result.result = new ScalarValue(value);
		}
		return result;
	}
}
