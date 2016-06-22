package serilogj.sinks.rollingfile;

import java.time.LocalDate;

public class RollingLogFile {
	private String filename;
	private LocalDate date;
	private int sequenceNumber;

	public RollingLogFile(String filename, LocalDate date, int sequenceNumber) {
		this.filename = filename;
		this.date = date;
		this.sequenceNumber = sequenceNumber;
	}

	public String getFilename() {
		return filename;
	}

	public LocalDate getDate() {
		return date;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}
}
