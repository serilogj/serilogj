package serilogj.sinks.periodicbatching;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

import serilogj.core.ILogEventSink;
import serilogj.debugging.SelfLog;
import serilogj.events.LogEvent;

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

/**
 * Base class for sinks that log events in batches. Batching is triggered
 * asynchronously on a timer.
 * 
 * To avoid unbounded memory growth, events are discarded after attempting to
 * send a batch, regardless of whether the batch succeeded or not. Implementers
 * that want to change this behavior need to either implement from scratch, or
 * embed retry logic in the batch emitting functions.
 */
public abstract class PeriodicBatchingSink implements ILogEventSink, Closeable {
	private int batchSizeLimit;
	private ConcurrentLinkedQueue<LogEvent> queue;
	private BatchedConnectionStatus status;
	private Queue<LogEvent> waitingBatch;

	private Object syncLock = new Object();
	private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	private volatile boolean unloading;

	/**
	 * Construct a sink posting to the specified database.
	 */
	public PeriodicBatchingSink(int batchSizeLimit, Duration period) {
		if (period == null) {
			throw new IllegalArgumentException("period");
		}

		this.batchSizeLimit = batchSizeLimit;
		queue = new ConcurrentLinkedQueue<LogEvent>();
		waitingBatch = new LinkedList<LogEvent>();
		status = new BatchedConnectionStatus(period);
		
		// Assumes that subclass constructors will have finished
		// executing within the interval.
		setTimer(status.getNextInterval());
	}

	@Override
	public void emit(LogEvent logEvent) {
		if (logEvent == null) {
			throw new IllegalArgumentException("logEvent");
		}

		if (unloading) {
			return;
		}

		queue.add(logEvent);
	}

	private void setTimer(Duration delay) {
		if (unloading) {
			return;
		}

		ses.schedule(() -> execute(), delay.getSeconds(), TimeUnit.SECONDS);
	}

	private void execute() {
		try {
			boolean batchWasFull;
			do {
				LogEvent next;
				while (waitingBatch.size() < batchSizeLimit && (next = queue.poll()) != null) {
					if (canInclude(next)) {
						waitingBatch.add(next);
					}
				}

				if (waitingBatch.size() == 0) {
					onEmptyBatch();
					return;
				}

				emitBatch(waitingBatch);

				batchWasFull = waitingBatch.size() == batchSizeLimit;
				waitingBatch.clear();
				status.markSuccess();
			} while (batchWasFull);

		} catch (Exception ex) {
			SelfLog.writeLine("Exception while emitting periodic batch from %s: %s", getClass().getName(),
					ex.getMessage());
			status.markFailure();
		} finally {
			if (status.getShouldDropBatch()) {
				waitingBatch.clear();
			}

			if (status.getShouldDropQueue()) {
				queue.clear();
			}

			synchronized (syncLock) {
				setTimer(status.getNextInterval());
			}
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (syncLock) {
			if (unloading) {
				return;
			}

			unloading = true;
		}

		ses.shutdownNow();
		try {
			ses.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			// No problem, we're shutting down
		}
		execute();
	}

	protected boolean canInclude(LogEvent event) {
		return true;
	}

	protected void onEmptyBatch() {
	}

	// We possibly need to do something with synchronized context but it's not
	// supported by serilogj yet
	protected abstract void emitBatch(Queue<LogEvent> events);
}
