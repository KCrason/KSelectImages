package com.kcrason.kselectimages.event;


import com.kcrason.kselectimages.model.Image;

public class RefreshImageSelect {
	
	private Image image;

	public RefreshImageSelect(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}
}
