package serilogj.sinks.rollingfile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import serilogj.core.ILogEventSink;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEvent;
import serilogj.formatting.ITextFormatter;

public class RollingFileSink implements ILogEventSink, Closeable {
	private TemplatedPathRoller roller;
	private Long fileSizeLimitBytes;
	private Integer retainedFileCountLimit;
	private boolean buffered;
	private Object syncLock = new Object();

	private boolean isDisposed;
	private LocalDateTime nextCheckpoint;
	private FileSink currentFile;
	private ITextFormatter formatter;

	public RollingFileSink(String pathFormat, Long fileSizeLimitBytes, Integer retainedFileCountLimit, boolean buffered,
			ITextFormatter formatter) {
		if (pathFormat == null) {
			throw new IllegalArgumentException("pathFormat");
		}
		if (formatter == null) {
			throw new IllegalArgumentException("formatter");
		}
		roller = new TemplatedPathRoller(pathFormat);
		this.fileSizeLimitBytes = fileSizeLimitBytes;
		this.retainedFileCountLimit = retainedFileCountLimit;
		this.buffered = buffered;
		this.formatter = formatter;
	}

	@Override
	public void emit(LogEvent logEvent) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}

		synchronized (syncLock) {
			if (isDisposed) {
				throw new IllegalStateException("The rolling file has been disposed");
			}

			alignCurrentFileTo(LocalDateTime.now());

			if (currentFile != null) {
				currentFile.emit(logEvent);
			}
		}
	}

	private void alignCurrentFileTo(LocalDateTime date) {
		if (nextCheckpoint == null) {
			openFile(date);
		} else if (date.isAfter(nextCheckpoint) || date.isAfter(nextCheckpoint)) {
			closeFile();
			openFile(date);
		}
	}

	private void sortFiles(ArrayList<RollingLogFile> files) {
		files.sort(new Comparator<RollingLogFile>() {
			@Override
			public int compare(RollingLogFile o1, RollingLogFile o2) {
				if (o1.getDate().isEqual(o2.getDate())) {
					return Integer.compare(o2.getSequenceNumber(), o1.getSequenceNumber());
				}
				return o2.getDate().compareTo(o1.getDate());
			}
		});
	}

	private void openFile(LocalDateTime now) {
		LocalDate today = now.toLocalDate();
		nextCheckpoint = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0).plusDays(1);

		File folder = new File(roller.getLogFileDirectory());
		ArrayList<RollingLogFile> files = roller.getMatches(folder.list());

		int sequenceNumber = 0;
		if (files.size() > 0) {
			sortFiles(files);
			sequenceNumber = files.get(0).getSequenceNumber();
		}

		int maxAttempts = 3;
		for (int attempt = 0; attempt < maxAttempts; attempt++) {
			String path = roller.getLogFilePath(today, sequenceNumber);

			try {
				currentFile = new FileSink(path, fileSizeLimitBytes, buffered, formatter);
			} catch (IOException e) {
				SelfLog.writeLine("Rolling file target %s was locked, attempting to open next in sequence (%d)", path,
						sequenceNumber);
				sequenceNumber++;
				continue;
			}

			applyRetentionPolicy(path);
			break;
		}
	}

	private void closeFile() {
		nextCheckpoint = null;
		if (currentFile != null) {
			try {
				currentFile.close();
			} catch (IOException e) {
				SelfLog.writeLine("Failed to close %s", currentFile.getFilename());
			}
			currentFile = null;
		}
	}

	private void applyRetentionPolicy(String currentFilePath) {
		if (retainedFileCountLimit == null) {
			return;
		}

		// Create a list of rolling log files
		File folder = new File(roller.getLogFileDirectory());
		ArrayList<RollingLogFile> files = roller.getMatches(folder.list());

		// Add our current log file (if it already exists, then first remove it,
		// saves us checking if it's in the list)
		String currentFilename = new File(currentFilePath).getName();
		files.removeIf(f -> f.getFilename().compareToIgnoreCase(currentFilename) == 0);
		files.addAll(roller.getMatches(new String[] { currentFilename }));

		// Sort it
		sortFiles(files);

		// Nothing to remove?
		if (files.size() <= retainedFileCountLimit) {
			return;
		}

		// Remove first x files from "remove" list, since we want to keep those
		for (int i = 0; i < retainedFileCountLimit; i++) {
			files.remove(0);
		}

		for (RollingLogFile file : files) {
			try {
				if (!new File(roller.getLogFileDirectory(), file.getFilename()).delete()) {
					throw new IOException("delete returned false");
				}
			} catch (IOException e) {
				SelfLog.writeLine("Error %s while removing obsolete file %s", e.getMessage(), file.getFilename());
			}
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (syncLock) {
			if (!isDisposed) {
				closeFile();
				isDisposed = true;
			}
		}
	}

}
