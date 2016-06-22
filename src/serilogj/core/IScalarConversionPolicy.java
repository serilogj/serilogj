package serilogj.core;

/**
 * Determine how a simple value is carried through the logging pipeline as an
 * immutable <see cref="ScalarValue"/>.
 */
public interface IScalarConversionPolicy {
	/**
	 * If supported, convert the provided value into an immutable scalar.
	 * 
	 * @param value
	 *            The value to convert.
	 * @param propertyValueFactory
	 *            Recursively apply policies to convert additional values.
	 * @param result
	 *            The converted value, or null.
	 * @return True if the value could be converted under this policy.
	 */
	ScalarConversionPolicyResult tryConvertToScalar(Object value, ILogEventPropertyValueFactory propertyValueFactory);
}
