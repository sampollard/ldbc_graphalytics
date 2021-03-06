/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Results of the execution of a single benchmark. Includes timestamps to compute the makespan, a flag for successful
 * completion, and platform-specific information about the benchmark execution.
 *
 * @author Tim Hegeman
 */
public final class BenchmarkResult implements Serializable {

	private final Benchmark benchmark;
	private final PlatformBenchmarkResult platformBenchmarkResult;

	private final Date startOfBenchmark;
	private final Date endOfBenchmark;
	private final boolean completedSuccessfully;

	/**
	 * @param benchmark               the benchmark executed to obtain this result
	 * @param platformBenchmarkResult platform-specific information regarding the execution of the benchmark
	 * @param startOfBenchmark        the start time of the benchmark execution
	 * @param endOfBenchmark          the completion time of the benchmark execution
	 * @param completedSuccessfully   true iff the benchmark completed successfully
	 */
	private BenchmarkResult(Benchmark benchmark, PlatformBenchmarkResult platformBenchmarkResult,
	                        Date startOfBenchmark, Date endOfBenchmark, boolean completedSuccessfully) {
		this.benchmark = benchmark;
		this.platformBenchmarkResult = platformBenchmarkResult;
		this.startOfBenchmark = startOfBenchmark;
		this.endOfBenchmark = endOfBenchmark;
		this.completedSuccessfully = completedSuccessfully;
	}

	/**
	 * Creates an empty BenchmarkResult for a benchmark that has not been run.
	 *
	 * @param benchmark the benchmark that has not been run
	 * @return a new empty BenchmarkResult
	 */
	public static BenchmarkResult forBenchmarkNotRun(Benchmark benchmark) {
		return new BenchmarkResult(benchmark, new PlatformBenchmarkResult(NestedConfiguration.empty()),
				new Date(0), new Date(0), false);
	}

	/**
	 * @return the benchmark executed to obtain this result
	 */
	public Benchmark getBenchmark() {
		return benchmark;
	}

	/**
	 * @return platform-specific information regarding the execution of the benchmark
	 */
	public PlatformBenchmarkResult getPlatformBenchmarkResult() {
		return platformBenchmarkResult;
	}

	/**
	 * @return the start time of the benchmark execution
	 */
	public Date getStartOfBenchmark() {
		return new Date(startOfBenchmark.getTime());
	}

	/**
	 * @return the completion time of the benchmark execution
	 */
	public Date getEndOfBenchmark() {
		return new Date(endOfBenchmark.getTime());
	}

	/**
	 * @return true iff the benchmark completed successfully
	 */
	public boolean isCompletedSuccessfully() {
		return completedSuccessfully;
	}

	/**
	 * @return the elapsed time from start to end in milliseconds
	 */
	public long getElapsedTimeInMillis() {
		return endOfBenchmark.getTime() - startOfBenchmark.getTime();
	}

	/**
	 * Factory class for the BenchmarkResult class.
	 */
	public static class BenchmarkResultBuilder {
		private Benchmark benchmark;
		private Date startOfBenchmark;
		private Date endOfBenchmark;
		private boolean completedSuccessfully = false;

		/**
		 * Constructs a new BenchmarkResultBuilder that can be used to create a new BenchmarkResult.
		 *
		 * @param benchmark the benchmark to be executed to obtain a new result
		 * @throws IllegalArgumentException iff benchmark is null
		 */
		public BenchmarkResultBuilder(Benchmark benchmark) {
			if (benchmark == null)
				throw new IllegalArgumentException("Parameter \"benchmark\" must not be null.");

			this.benchmark = benchmark;
			startOfBenchmark = endOfBenchmark = new Date();
		}

		/**
		 * Sets the start of the benchmark execution to be the current time.
		 */
		public void markStartOfBenchmark() {
			startOfBenchmark = new Date();
		}

		/**
		 * Sets the end of the benchmark execution to be the current time. Also records the completion
		 * status of the benchmark.
		 *
		 * @param completedSuccessfully true iff the benchmark completed successfully
		 */
		public void markEndOfBenchmark(boolean completedSuccessfully) {
			endOfBenchmark = new Date();
			this.completedSuccessfully = completedSuccessfully;
		}

		/**
		 * @param platformBenchmarkResult platform-specific information regarding the execution of the benchmark
		 * @return a new BenchmarkResult
		 * @throws IllegalArgumentException iff benchmark is null
		 */
		public BenchmarkResult buildFromResult(PlatformBenchmarkResult platformBenchmarkResult) {
			if (platformBenchmarkResult == null)
				throw new IllegalArgumentException("Parameter \"platformBenchmarkResult\" must not be null.");

			return new BenchmarkResult(benchmark, platformBenchmarkResult, startOfBenchmark,
					endOfBenchmark, completedSuccessfully);
		}

	}

}
