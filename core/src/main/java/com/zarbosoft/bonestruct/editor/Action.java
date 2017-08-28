package com.zarbosoft.bonestruct.editor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class Action {
	@Retention(RetentionPolicy.RUNTIME)
	public @interface StaticID {
		String id();
	}

	public Action() {
		if (getClass().getAnnotation(StaticID.class) == null)
			throw new AssertionError();
	}

	public abstract boolean run(Context context);

	public String id() {
		return getClass().getAnnotation(StaticID.class).id();
	}
}
