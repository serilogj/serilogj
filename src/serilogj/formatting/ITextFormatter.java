package serilogj.formatting;

import serilogj.events.*;
import java.io.*;

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

/**
 * Formats log events in a textual representation.
 */
public interface ITextFormatter {
	/**
	 * Format the log event into the output.
	 * 
	 * @param logEvent
	 *            The event to format.
	 * @param output
	 *            The output.
	 */
	void format(LogEvent logEvent, Writer output) throws IOException;
}