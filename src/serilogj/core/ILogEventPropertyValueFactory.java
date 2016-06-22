package serilogj.core;

import serilogj.events.LogEventPropertyValue;

/**
 * Supports the policy-driven construction of
 * <see cref="LogEventPropertyValue"/>s given regular .NET objects.
 */
public interface ILogEventPropertyValueFactory {
	/**
	 * Create a <see cref="LogEventPropertyValue"/> given a .NET object and
	 * destructuring strategy.
	 * 
	 * @param value
	 *            The value of the property.
	 * @param destructureObjects
	 *            If true, and the value is a non-primitive, non-array type,
	 *            then the value will be converted to a structure; otherwise,
	 *            unknown types will be converted to scalars, which are
	 *            generally stored as strings.
	 * @return The value.
	 */
	LogEventPropertyValue createPropertyValue(Object value, boolean destructureObjects);
}
