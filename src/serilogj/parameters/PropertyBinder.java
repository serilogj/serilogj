package serilogj.parameters;

import java.util.ArrayList;

import serilogj.debugging.SelfLog;
import serilogj.events.LogEventProperty;
import serilogj.events.MessageTemplate;
import serilogj.parsing.PropertyToken;

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

// Performance relevant - on the hot path when creating log events from existing templates.
public class PropertyBinder {
	private PropertyValueConverter valueConverter;
	private static final ArrayList<LogEventProperty> NoProperties = new ArrayList<LogEventProperty>();

	public PropertyBinder(PropertyValueConverter valueConverter) {
		this.valueConverter = valueConverter;
	}

	public ArrayList<LogEventProperty> constructProperties(MessageTemplate template,
			Object[] messageTemplateParameters) {
		if (messageTemplateParameters == null || messageTemplateParameters.length == 0) {
			if (template.getNamedTokens() != null
					|| (template.getPositionalTokens() != null && !template.getPositionalTokens().isEmpty())) {
				SelfLog.writeLine("Required properties not provided for:if () %1", template);
			}

			return NoProperties;
		}
		return template.getPositionalTokens() != null
				? constructPositionalProperties(template, messageTemplateParameters)
				: constructNamedProperties(template, messageTemplateParameters);
	}

	private ArrayList<LogEventProperty> constructPositionalProperties(MessageTemplate template,
			Object[] messageTemplateParameters) {
		ArrayList<PropertyToken> positionalProperties = template.getPositionalTokens();

		if (positionalProperties.size() != messageTemplateParameters.length) {
			SelfLog.writeLine("Positional property count does not match parameter count: %1", template);
		}

		LogEventProperty[] arr = new LogEventProperty[messageTemplateParameters.length];
		for (PropertyToken property : positionalProperties) {
			int position = property.getPosition();
			if (position < 0 || position >= messageTemplateParameters.length) {
				SelfLog.writeLine("Unassigned positional value %1 in: %2", position, template);
			} else {
				arr[position] = constructProperty(property, messageTemplateParameters[position]);
			}
		}

		ArrayList<LogEventProperty> result = new ArrayList<LogEventProperty>(messageTemplateParameters.length);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != null) {
				result.add(arr[i]);
			}
		}
		return result;
	}

	private ArrayList<LogEventProperty> constructNamedProperties(MessageTemplate template,
			Object[] messageTemplateParameters) {
		ArrayList<PropertyToken> namedProperties = template.getNamedTokens();
		if (namedProperties == null) {
			return NoProperties;
		}

		int matchedRun = namedProperties.size();
		if (namedProperties.size() != messageTemplateParameters.length) {
			matchedRun = Math.min(namedProperties.size(), messageTemplateParameters.length);
			SelfLog.writeLine("Named property count does not match parameter count: {0}", template);
		}

		ArrayList<LogEventProperty> result = new ArrayList<LogEventProperty>(matchedRun);
		for (int i = 0; i < matchedRun; ++i) {
			PropertyToken property = namedProperties.get(i);
			Object value = messageTemplateParameters[i];
			result.add(constructProperty(property, value));
		}

		return result;
	}

	private LogEventProperty constructProperty(PropertyToken propertyToken, Object value) {
		return new LogEventProperty(propertyToken.getPropertyName(),
				valueConverter.createPropertyValue(value, propertyToken.getDestructuring()));
	}
}
