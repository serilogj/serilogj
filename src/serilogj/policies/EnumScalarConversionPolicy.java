package serilogj.policies;

import java.util.HashMap;
import java.util.Map;

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

public class EnumScalarConversionPolicy implements IScalarConversionPolicy {
	private Map<Class<?>, Map<Integer, ScalarValue>> _values = new HashMap<Class<?>, Map<Integer, ScalarValue>>();

	@Override
	public ScalarConversionPolicyResult tryConvertToScalar(Object value,
			ILogEventPropertyValueFactory propertyValueFactory) {
		ScalarConversionPolicyResult result = new ScalarConversionPolicyResult();
		if (!value.getClass().isEnum()) {
			return result;
		}

		result.isValid = true;
		synchronized (_values) {
			Map<Integer, ScalarValue> enumValues = _values.get(value.getClass());
			if (enumValues == null) {
				enumValues = new HashMap<Integer, ScalarValue>();
				_values.put(value.getClass(), enumValues);
			}

			@SuppressWarnings("rawtypes")
			Integer enumOrdinal = ((Enum) value).ordinal();

			ScalarValue resultValue = enumValues.get(enumOrdinal);
			if (resultValue == null) {
				resultValue = new ScalarValue(value);
				enumValues.put(enumOrdinal, resultValue);
			}

			result.result = resultValue;
		}
		return result;
	}
}
