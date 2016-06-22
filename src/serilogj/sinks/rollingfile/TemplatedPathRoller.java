package serilogj.sinks.rollingfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.regex.*;

import serilogj.debugging.SelfLog;

// Copyright 2013-2016 Serilog Contributors
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

// Rolls files based on the current date, using a path
// formatting pattern like:
// Logs/log-{Date}.txt
//
public class TemplatedPathRoller {
	private final static String OldStyleDateSpecifier = "{0}";
	private final static String DateSpecifier = "{date}";
	private final static String DateFormat = "yyyyMMdd";
	private final static DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern(DateFormat);
	private final static String DefaultSeparator = "-";

	private Pattern filenameMatcher;
	private String directorySearchPattern;
	private String logFileDirectory;
	private String prefix;
	private String suffix;

	public TemplatedPathRoller(String pathTemplate) {
		if (pathTemplate == null) {
			throw new IllegalArgumentException("pathTemplate");
		}
		if (pathTemplate.contains(OldStyleDateSpecifier)) {
			throw new IllegalArgumentException("pathTemplate");
		}

		File file = new File(pathTemplate);
		String directory = file.getParent();
		if (directory == null || directory.equals("")) {
			directory = System.getProperty("user.dir");
		}

		File path = new File(directory);
		try {
			directory = path.getCanonicalPath();
		} catch (IOException e) {
			SelfLog.writeLine("TemplatePathRoller threw an exception: %s", e.getMessage());
			throw new IllegalArgumentException("pathTemplate");
		}
		if (directory.toLowerCase().contains(DateSpecifier)) {
			throw new IllegalArgumentException("pathTemplate");
		}

		String filenameTemplate = file.getName();
		if (filenameTemplate == null || filenameTemplate.equals("")) {
			throw new IllegalArgumentException("pathTemplate");
		}

		if (!filenameTemplate.toLowerCase().contains(DateSpecifier)) {
			String extension = "";
			int extensionIndex = filenameTemplate.lastIndexOf('.');
			if (extensionIndex >= 0) {
				extension = filenameTemplate.substring(extensionIndex);
				filenameTemplate = filenameTemplate.substring(0, extensionIndex);
			}

			filenameTemplate = filenameTemplate + DefaultSeparator + DateSpecifier + extension;
		}

		int indexOfSpecifier = filenameTemplate.toLowerCase().indexOf(DateSpecifier);
		prefix = filenameTemplate.substring(0, indexOfSpecifier);
		suffix = filenameTemplate.substring(indexOfSpecifier + DateSpecifier.length());

		filenameMatcher = Pattern.compile("^" + Pattern.quote(prefix) + "(?<date>\\d{" + DateFormat.length() + "})"
				+ "(?<inc>_[0-9]{3,}){0,1}" + Pattern.quote(suffix) + "$");

		directorySearchPattern = filenameTemplate.replace(DateSpecifier, "*");
		logFileDirectory = directory;
	}

	public String getLogFileDirectory() {
		return logFileDirectory;
	}

	public String getDirectorySearchPattern() {
		return directorySearchPattern;
	}

	public String getLogFilePath(LocalDate date, int sequenceNumber) {
		String tok = DateFormatter.format(date);
		if (sequenceNumber != 0) {
			tok += "_" + String.format("%03d", sequenceNumber);
		}
		return Paths.get(logFileDirectory, prefix + tok + suffix).toString();
	}

	public ArrayList<RollingLogFile> getMatches(String[] filenames) {
		ArrayList<RollingLogFile> result = new ArrayList<RollingLogFile>();
		for (String filename : filenames) {
			Matcher matcher = filenameMatcher.matcher(filename);
			if (matcher.matches()) {
				int inc = 0;
				String incGroup = matcher.group("inc");
				if (incGroup != null) {
					String incPart = incGroup.substring(1);
					try {
						inc = Integer.parseInt(incPart);
					} catch (NumberFormatException e) {
						inc = 0;
						SelfLog.writeLine("Failed to parse 'incGroup' %s", incGroup);
					}
				}

				LocalDate date;
				try {
					date = LocalDate.parse(matcher.group("date"), DateFormatter);
				} catch (DateTimeParseException e) {
					SelfLog.writeLine("Failed to parse 'date' %s", matcher.group("date"));
					continue;
				}

				result.add(new RollingLogFile(filename, date, inc));
			}
		}
		return result;
	}
}
