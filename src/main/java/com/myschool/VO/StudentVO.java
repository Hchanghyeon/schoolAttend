package com.myschool.VO;


public class StudentVO {
	private String num;
	private String name;
	private String attend;
	private String date;
	private String time;
	
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setNum(String num) {
		this.num = num;
	}
	
	public String getNum() {
		return num;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAttend() {
		return attend;
	}
	public void setAttend(String attend) {
		this.attend = attend;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
}
