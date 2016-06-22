package serilogj.policies;

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
public class ByteArrayScalarConversionPolicy implements IScalarConversionPolicy {
	private static final int MaximumByteArrayLength = 1024;

	@Override
	public ScalarConversionPolicyResult tryConvertToScalar(Object value,
			ILogEventPropertyValueFactory propertyValueFactory) {
		ScalarConversionPolicyResult result = new ScalarConversionPolicyResult();
		if (value instanceof byte[]) {
			byte[] arr = (byte[]) value;
			if (arr.length > MaximumByteArrayLength) {
				String description = "";
				for (int i = 0; i < 16; i++) {
					description = description + String.format("%02X", arr[i]);
				}
				description = description + "... (" + arr.length + " bytes)";

				result.isValid = true;
				result.result = new ScalarValue(description);
			} else {
				byte[] clone = new byte[arr.length];
				for (int i = 0; i < arr.length; i++) {
					clone[i] = arr[i];
				}

				result.isValid = true;
				result.result = new ScalarValue(clone);
			}
		}
		return result;
	}
}
