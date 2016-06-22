package serilogj.sinks.seq;

import serilogj.debugging.SelfLog;
import serilogj.events.LogEventLevel;

// Serilog.Sinks.Seq Copyright 2016 Serilog Contributors
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

public class SeqApi {
	// Why not use a JSON parser here? For a very small case, it's not
	// worth taking on the extra payload/dependency management issues that
	// a full-fledged parser will entail. If things get more sophisticated
	// we'll reevaluate.
	private final static String LevelMarker = "\"MinimumLevelAccepted\":\"";

	public static LogEventLevel readEventInputResult(String eventInputResult) {
		if (eventInputResult == null) {
			return null;
		}

		int startProp = eventInputResult.toLowerCase().indexOf(LevelMarker.toLowerCase());
		if (startProp == -1) {
			return null;
		}

		int startValue = startProp + LevelMarker.length();
		if (startValue >= eventInputResult.length()) {
			return null;
		}

		int endValue = eventInputResult.indexOf('"', startValue);
		if (endValue == -1) {
			return null;
		}

		String value = eventInputResult.substring(startValue, endValue);
		try {
			return LogEventLevel.valueOf(value);
		} catch (Exception ex) {
			SelfLog.writeLine("Seq returned a minimum level of %s which could not be mapped to LogEventLevel", value);
			return null;
		}
	}
}
