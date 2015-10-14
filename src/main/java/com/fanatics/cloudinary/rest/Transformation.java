package com.fanatics.cloudinary.rest;

public class Transformation {

	public Transformation(String width, String height, String crop,
			String gravity) {
		super();
		this.width = width;
		this.height = height;
		this.crop = crop;
		this.gravity = gravity;
	}

	private String width;
	private String height;
	private String crop;
	private String gravity;
	
	public String getWidth() {
		return width;
	}
	
	public void setWidth(String width) {
		this.width = width;
	}
	
	public String getHeight() {
		return height;
	}
	
	public void setHeight(String height) {
		this.height = height;
	}
	
	public String getCrop() {
		return crop;
	}
	
	public void setCrop(String crop) {
		this.crop = crop;
	}
	
	public String getGravity() {
		return gravity;
	}
	
	public void setGravity(String gravity) {
		this.gravity = gravity;
	}
	
	
}
