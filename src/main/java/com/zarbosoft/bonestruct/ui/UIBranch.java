package com.zarbosoft.bonestruct.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;

public class UIBranch extends Region {
	
	double availableWidth;
	
	boolean wrapped;
	
	Canvas canvas = new Canvas();

	@Override
	protected double computePrefWidth(double height) {
		return 0;
	}

	@Override
	protected double computePrefHeight(double width) {
		// TODO Auto-generated method stub
		return super.computePrefHeight(width);
	}

	@Override
	protected void layoutChildren() {
		if (wrapped) {
			
		} else {
			
		}
	}

}
