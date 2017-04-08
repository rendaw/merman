package com.zarbosoft.bonestruct.syntax;

import com.zarbosoft.interface1.Configuration;

import java.time.Duration;

@Configuration
public abstract class ModelDuration {
	public abstract Duration get();

	@Configuration(name = "m")
	public static class Minutes extends ModelDuration {
		@Configuration
		public double length;

		@Override
		public Duration get() {
			return Duration.ofMillis((long) (length * 60 * 1000));
		}
	}

	@Configuration(name = "s")
	public static class Seconds extends ModelDuration {
		@Configuration
		public double length;

		@Override
		public Duration get() {
			return Duration.ofMillis((long) (length * 1000));
		}
	}
}
