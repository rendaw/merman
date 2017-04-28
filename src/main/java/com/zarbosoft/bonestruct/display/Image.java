package com.zarbosoft.bonestruct.display;

import com.zarbosoft.bonestruct.editor.Context;

import java.nio.file.Path;

public interface Image extends DisplayNode {
	void setImage(Context context, Path path);

	void rotate(Context context, double rotate);
}
