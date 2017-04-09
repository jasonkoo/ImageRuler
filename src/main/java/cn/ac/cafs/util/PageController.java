package cn.ac.cafs.util;

import java.util.List;
import java.util.ArrayList;

public class PageController {
	private int cur;
	private List<String> imageNames; 
	
	public PageController() {
		this.cur = 0;
		this.imageNames = new ArrayList<String>();
	}
	
	public void addImageName(String imageName) {
		imageNames.add(imageName);
	}
	
	public String prev() {
		if (cur > 0) {
			cur = cur -1;
		} else {
			cur = 0;
		}
		return imageNames.get(cur);
	}
	
	public String next() {
		if (cur < imageNames.size() -1) {
			cur = cur + 1;
		} else {
			cur = imageNames.size() -1;
		}
		return imageNames.get(cur);
	}
	
	public String cur() {
		return imageNames.get(cur);
	}	
	
	public int getCurNum() {
		return this.cur;
	}
}
