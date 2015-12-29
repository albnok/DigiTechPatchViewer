package com.glaringnotebook.digitechpatchviewer;

public class Param {
	int id, position;
	final static String UNSETTABLE = "";
	final static String BLANK = "------";
	String value, name = "", text = BLANK, idposition;
	public String toString() {
		return "#" + id + " pos: " + position + " " + name + " = " + text + " (" + value + ")";
	}
}
