package serilogj.core.pipeline;

import java.util.*;
import serilogj.core.*;
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
public class MessageTemplateCache implements IMessageTemplateParser {
	private IMessageTemplateParser innerParser;
	private Map<String, MessageTemplate> templates = new HashMap<String, MessageTemplate>();

	private static final int MaxCacheItems = 1000;
	private static final int MaxCachedTemplateLength = 1024;

	public MessageTemplateCache(IMessageTemplateParser innerParser) {
		if (innerParser == null) {
			throw new IllegalArgumentException("innerParser");
		}
		this.innerParser = innerParser;
	}

	public MessageTemplate parse(String messageTemplate) {
		if (messageTemplate == null) {
			throw new IllegalArgumentException("messageTemplate");
		}

		if (messageTemplate.length() > MaxCachedTemplateLength) {
			return innerParser.parse(messageTemplate);
		}

		MessageTemplate result = null;
		synchronized (templates) {
			if (templates.containsKey(messageTemplate)) {
				result = templates.get(messageTemplate);
				return result;
			}
		}

		result = innerParser.parse(messageTemplate);

		synchronized (templates) {
			// Exceeding MaxCacheItems is *not* the sunny day scenario; all
			// we're doing here is preventing out-of-memory
			// conditions when the library is used incorrectly. Correct use
			// (templates, rather than
			// direct message strings) should barely, if ever, overflow this
			// cache.

			// Changing workloads through the lifecycle of an app instance mean
			// we can gain some ground by
			// potentially dropping templates generated only in startup, or only
			// during specific infrequent
			// activities.

			if (templates.size() == MaxCacheItems) {
				templates.clear();
			}

			templates.put(messageTemplate, result);
		}

		return result;
	}
}
