package serilogj.policies;

import serilogj.core.ILogEventPropertyValueFactory;
import serilogj.core.IScalarConversionPolicy;
import serilogj.core.ScalarConversionPolicyResult;
import serilogj.events.ScalarValue;

public class BooleanScalarConversionPolicy implements IScalarConversionPolicy {
	private final static ScalarValue TrueScalarValue = new ScalarValue(true);
	private final static ScalarValue FalseScalarValue = new ScalarValue(false);

	@Override
	public ScalarConversionPolicyResult tryConvertToScalar(Object value,
			ILogEventPropertyValueFactory propertyValueFactory) {
		ScalarConversionPolicyResult result = new ScalarConversionPolicyResult();

		ScalarValue resultValue = null;
		try {
			if (value instanceof Boolean) {
				resultValue = ((Boolean) value) ? TrueScalarValue : FalseScalarValue;
			} else if (value.getClass().isPrimitive() && value.getClass().getName().equals("boolean")) {
				resultValue = ((boolean) value) ? TrueScalarValue : FalseScalarValue;
			}
		} catch (Exception e) {
			// We ignore this error
		}

		result.isValid = resultValue != null;
		return result;
	}
}
