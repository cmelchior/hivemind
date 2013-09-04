package dk.ilios.hivemind.ai.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * StopWatch class used for various statistics across classes
 */
public class StopWatch {

    private static final String SINGLE_WATCH_ID = "StopWatch.class#1";

	static private StopWatch instance;
	private Map<String, Watch> watches = new HashMap<String, Watch>();

	public static synchronized StopWatch getInstance() {
		if (instance == null) {
			instance = new StopWatch();
		}

		return instance;
	}

	private StopWatch() {}

	public Watch start() {
		return start(SINGLE_WATCH_ID);
	}

	public Watch start(String name) {
		Watch watch = new Watch();
		watches.put(name, watch);
		watch.start();
		return watch;
	}

	public Watch stop(String name) {
		Watch watch = watches.get(name);
		if (watch != null) {
			watch.stop();
		}

		return watch;
	}

    public Watch stop() {
        return stop(SINGLE_WATCH_ID);
    }

	public Watch get(String name) {
		return watches.get(name);
	}

	public static class Watch {
		public String name;
		public long start;
		public long end;
		public boolean running;

		public boolean isRunning() {
			return running;
		}

		public Watch start() {
			if (running) return this;
			running = true;
			start = System.nanoTime();
			return this;
		}

		public Watch stop() {
			if (!running) return this;
			running = false;
			end = System.nanoTime();
			return this;
		}

		public long getElapsedTimeInMillis() {
			long elapsed;
			if (running) {
				elapsed = ((System.nanoTime() - start) / 1000000);
			}
			else {
				elapsed = ((end - start) / 1000000);
			}
			return elapsed;
		}

		public long getElapsedTimeInSeconds() {
			long elapsed;
			if (running) {
				elapsed = ((System.nanoTime() - start) / 1000000000);
			}
			else {
				elapsed = ((end - start) / 1000000000);
			}
			return elapsed;
		}

		public void log(String prefix) {
			System.out.println(prefix + " Elapsed time: " + getElapsedTimeInMillis() + " ms.");
		}
	}
}