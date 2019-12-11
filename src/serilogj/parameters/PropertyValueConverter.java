package serilogj.parameters;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.lang.reflect.Array;

import serilogj.reflection.Reflection;
import serilogj.reflection.Property;
import serilogj.core.DestructuringPolicyResult;
import serilogj.core.IDestructuringPolicy;
import serilogj.core.ILogEventPropertyFactory;
import serilogj.core.ILogEventPropertyValueFactory;
import serilogj.core.IScalarConversionPolicy;
import serilogj.core.ScalarConversionPolicyResult;
import serilogj.debugging.SelfLog;
import serilogj.events.DictionaryValue;
import serilogj.events.LogEventProperty;
import serilogj.events.LogEventPropertyValue;
import serilogj.events.ScalarValue;
import serilogj.events.SequenceValue;
import serilogj.events.StructureValue;
import serilogj.parsing.Destructuring;
import serilogj.policies.BooleanScalarConversionPolicy;
import serilogj.policies.ByteArrayScalarConversionPolicy;
import serilogj.policies.EnumScalarConversionPolicy;
import serilogj.policies.SimpleScalarConversionPolicy;

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

// Values in Serilog are simplified down into a lowest-common-denominator internal
// type system so that there is a better chance of code written with one sink in
// mind working correctly with any other. This technique also makes the programmer
// writing a log event (roughly) in control of the cost of recording that event.
public class PropertyValueConverter implements ILogEventPropertyValueFactory, ILogEventPropertyFactory {
	private static final ScalarValue NullScalarValue = new ScalarValue(null);
	private static final HashSet<java.lang.Class<?>> BuiltInScalarTypes = new HashSet<java.lang.Class<?>>();
	static {
		BuiltInScalarTypes.add(Boolean.class);
		BuiltInScalarTypes.add(Character.class);
		BuiltInScalarTypes.add(Byte.class);
		BuiltInScalarTypes.add(Short.class);
		BuiltInScalarTypes.add(Short.class);
		BuiltInScalarTypes.add(Integer.class);
		BuiltInScalarTypes.add(Long.class);
		BuiltInScalarTypes.add(Float.class);
		BuiltInScalarTypes.add(Double.class);
		BuiltInScalarTypes.add(java.math.BigDecimal.class);
		BuiltInScalarTypes.add(String.class);
		BuiltInScalarTypes.add(LocalDate.class);
		BuiltInScalarTypes.add(LocalDateTime.class);
		BuiltInScalarTypes.add(ZonedDateTime.class);
		BuiltInScalarTypes.add(Date.class);
		BuiltInScalarTypes.add(URI.class);
	}

	private ArrayList<IDestructuringPolicy> destructuringPolicies;
	private ArrayList<IScalarConversionPolicy> scalarConversionPolicies;
	private int maximumDestructuringDepth;

	public PropertyValueConverter(int maximumDestructuringDepth, java.lang.Class<?>[] additionalScalarTypes,
			IDestructuringPolicy[] additionalDestructuringPolicies) {
		if (additionalScalarTypes == null) {
			throw new IllegalArgumentException("additionalScalarTypes");
		}
		if (additionalDestructuringPolicies == null) {
			throw new IllegalArgumentException("additionalDestructuringPolicies");
		}
		if (maximumDestructuringDepth < 0) {
			throw new IllegalArgumentException("maximumDestructuringDepth");
		}

		this.maximumDestructuringDepth = maximumDestructuringDepth;

		HashSet<Class<?>> scalarTypes = new HashSet<Class<?>>(BuiltInScalarTypes);
		for (Class<?> type : additionalScalarTypes) {
			scalarTypes.add(type);
		}

		this.scalarConversionPolicies = new ArrayList<IScalarConversionPolicy>();
		this.scalarConversionPolicies.add(new SimpleScalarConversionPolicy(scalarTypes));
		this.scalarConversionPolicies.add(new EnumScalarConversionPolicy());
		this.scalarConversionPolicies.add(new ByteArrayScalarConversionPolicy());
		this.scalarConversionPolicies.add(new BooleanScalarConversionPolicy());
		// Nullable converter not build (Java doesn't have nullable types)

		this.destructuringPolicies = new ArrayList<IDestructuringPolicy>();
		for (IDestructuringPolicy policy : additionalDestructuringPolicies) {
			this.destructuringPolicies.add(policy);
		}
		// Destructuring policies not build (I don't think they are needed in
		// java)
	}

	public LogEventProperty createProperty(String name, Object value) {
		return createProperty(name, value, false);
	}

	public LogEventProperty createProperty(String name, Object value, boolean destructureObjects) {
		return new LogEventProperty(name, createPropertyValue(value, destructureObjects));
	}

	public final LogEventPropertyValue createPropertyValue(Object value) {
		return createPropertyValue(value, false);
	}

	public LogEventPropertyValue createPropertyValue(Object value, Destructuring destructuring) {
		return createPropertyValue(value, destructuring, 1);
	}

	private LogEventPropertyValue createPropertyValue(Object value, boolean destructureObjects, int depth) {
		return createPropertyValue(value, destructureObjects ? Destructuring.Destructure : Destructuring.Default,
				depth);
	}

	@Override
	public LogEventPropertyValue createPropertyValue(Object value, boolean destructureObjects) {
		return createPropertyValue(value, destructureObjects, 1);
	}

	private LogEventPropertyValue createPropertyValue(Object value, Destructuring destructuring, int depth) {
		if (value == null) {
			return NullScalarValue;
		}

		if (destructuring == Destructuring.Stringify) {
			return new ScalarValue(value.toString());
		}

		Class<?> valueType = value.getClass();

		DepthLimiter limiter = new DepthLimiter(depth, maximumDestructuringDepth, this);
		for (IScalarConversionPolicy scalarConversionPolicy : scalarConversionPolicies) {
			ScalarConversionPolicyResult result = scalarConversionPolicy.tryConvertToScalar(value, limiter);
			if (result.isValid) {
				return result.result;
			}
		}

		if (destructuring == Destructuring.Destructure) {
			for (IDestructuringPolicy destructuringPolicy : destructuringPolicies) {
				DestructuringPolicyResult result = destructuringPolicy.tryDestructure(value, limiter);
				if (result.isValid) {
					return result.result;
				}
			}
		}

		if (Map.class.isAssignableFrom(valueType)) {
			@SuppressWarnings("unchecked")
			Map<Object, Object> map = (Map<Object, Object>) value;
			Map<ScalarValue, LogEventPropertyValue> dict = new HashMap<ScalarValue, LogEventPropertyValue>();
			map.forEach((k, v) -> dict.put((ScalarValue) limiter.createPropertyValue(k, false),
					limiter.createPropertyValue(v, destructuring)));
			return new DictionaryValue(dict);
		}

		if (value instanceof Iterable || valueType.isArray()) {
			ArrayList<Object> list = new ArrayList<Object>();
			Reflection.unifyListOrArray(value, list);

			ArrayList<LogEventPropertyValue> elements = new ArrayList<LogEventPropertyValue>();
			for (Object o : list) {
				elements.add(limiter.createPropertyValue(o, destructuring));
			}
			return new SequenceValue(elements);
		}

		if (destructuring == Destructuring.Destructure) {
			String typeTag = valueType.getName();
			if (typeTag.length() <= 0 || isCompilerGeneratedType(valueType)) {
				typeTag = null;
			}

			return new StructureValue(getProperties(value, limiter), typeTag);
		}

		return new ScalarValue(value.toString());
	}


	private ArrayList<LogEventProperty> getProperties(Object value, ILogEventPropertyValueFactory recursive) {
		ArrayList<LogEventProperty> result = new ArrayList<LogEventProperty>();
		if (value == null) {
			return result;
		}
		Class<?> valueType = value.getClass();
		Map<String, Property> fields = null;
		try {
			fields = Reflection.getProperties(valueType);
		} catch (Exception ex) {
			SelfLog.writeLine("Exception %s caught while getting properties for %s.", ex, valueType.getName());
			return result;
		}

		for (Map.Entry<String, Property> pair : fields.entrySet()) {
			try {
				Object propertyValue = pair.getValue().getValue(value);
				result.add(new LogEventProperty(pair.getValue().getAlias(),
						recursive.createPropertyValue(propertyValue, true)));
			} catch (Exception ex) {
				SelfLog.writeLine("Exception %s caught while getting property %s.", ex, pair.getValue().getAlias());
			}
		}
		return result;
	}

	private boolean isCompilerGeneratedType(Class<?> type) {
		// TODO: Add more checks
		return type.isSynthetic();
	}

	private class DepthLimiter implements ILogEventPropertyValueFactory {
		private int maximumDestructuringDepth;
		private int currentDepth;
		private PropertyValueConverter propertyValueConverter;

		public DepthLimiter(int currentDepth, int maximumDepth, PropertyValueConverter propertyValueConverter) {
			this.maximumDestructuringDepth = maximumDepth;
			this.currentDepth = currentDepth;
			this.propertyValueConverter = propertyValueConverter;
		}

		public LogEventPropertyValue createPropertyValue(Object value, Destructuring destructuring) {
			LogEventPropertyValue tempVar = defaultIfMaximumDepth();
			return (tempVar != null) ? tempVar
					: propertyValueConverter.createPropertyValue(value, destructuring, currentDepth + 1);
		}

		public final LogEventPropertyValue createPropertyValue(Object value, boolean destructureObjects) {
			LogEventPropertyValue tempVar = defaultIfMaximumDepth();
			return (tempVar != null) ? tempVar
					: propertyValueConverter.createPropertyValue(value, destructureObjects, currentDepth + 1);
		}

		private LogEventPropertyValue defaultIfMaximumDepth() {
			if (currentDepth == maximumDestructuringDepth) {
				SelfLog.writeLine("Maximum destructuring depth reached.");
				return new ScalarValue(null);
			}

			return null;
		}
	}
}
