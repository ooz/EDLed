package de.mpg.cbs.edledplugin.stimulus;

import java.awt.Dimension;
import java.util.Observable;

public class Screen extends Observable {
	
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	
	private Dimension size;
	
	Screen() {
		this.size = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
	Screen(final Dimension size) {
		if (size == null) {
			this.size = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		} else {
			this.size = new Dimension(size);
		}
	}
	
	synchronized int getWidth() {
		return this.size.width;
	}
	synchronized int getHeight() {
		return this.size.height;
	}
	synchronized Dimension getSize() {
		return new Dimension(this.size);
	}
	
	synchronized void setWidth(final int newWidth) {
		this.size.width = newWidth;
		
		setChanged();
		notifyObservers();
	}
	synchronized void setHeight(final int newHeight) {
		this.size.height = newHeight;
		
		setChanged();
		notifyObservers();
	}
	synchronized void setSize(final Dimension newSize) {
		this.size = new Dimension(newSize);
		
		setChanged();
		notifyObservers();
	}
	
	synchronized void clear() {
		this.size.width = DEFAULT_WIDTH;
		this.size.height = DEFAULT_HEIGHT;
		setChanged();
		notifyObservers();
	}

}
