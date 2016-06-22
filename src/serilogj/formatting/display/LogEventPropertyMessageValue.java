package serilogj.formatting.display;

import java.util.*;
import java.io.*;
import serilogj.events.*;

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

public class LogEventPropertyMessageValue extends LogEventPropertyValue {
	private MessageTemplate template;
	private Map<String, LogEventPropertyValue> properties;

	public LogEventPropertyMessageValue(MessageTemplate template, Map<String, LogEventPropertyValue> properties) {
		this.template = template;
		this.properties = properties;
	}

	@Override
	public void render(Writer output, String format, Locale locale) throws IOException {
		template.render(properties, output, locale);
	}
}
