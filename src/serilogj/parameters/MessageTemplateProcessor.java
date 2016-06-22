package serilogj.parameters;

import serilogj.core.*;
import serilogj.core.pipeline.*;
import serilogj.events.*;
import serilogj.parsing.*;

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

public class MessageTemplateProcessor implements ILogEventPropertyFactory {
	private IMessageTemplateParser parser = new MessageTemplateCache(new MessageTemplateParser());
	private PropertyBinder propertyBinder;
	private PropertyValueConverter propertyValueConverter;

	public MessageTemplateProcessor(PropertyValueConverter propertyValueConverter) {
		this.propertyValueConverter = propertyValueConverter;
		this.propertyBinder = new PropertyBinder(propertyValueConverter);
	}

	public MessageTemplateProcessorResult process(String messageTemplate, Object[] messageTemplateParameters) {
		MessageTemplateProcessorResult result = new MessageTemplateProcessorResult();
		result.template = parser.parse(messageTemplate);
		result.properties = propertyBinder.constructProperties(result.template, messageTemplateParameters);
		return result;
	}

	@Override
	public LogEventProperty createProperty(String name, Object value, boolean destructureObjects) {
		return propertyValueConverter.createProperty(name, value, destructureObjects);
	}
}
