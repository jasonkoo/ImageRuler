package cn.ac.cafs.bean;

public class Line {
	private char knob;
	private int position;
	
	public Line(char knob, int position) {
		this.knob = knob;
		this.position = position;
	}
	
	public char getKnob() {
		return knob;
	}
	public void setKnob(char knob) {
		this.knob = knob;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
}
