package com.kcrason.kselectimages.event;

public class RemoveImageEvent {

	private int index;

	public RemoveImageEvent(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
