package com.glaringnotebook.digitechpatchviewer;

class Knob {
	String name;
	Integer id, condition, slot, position = null;
	boolean optional = false;
	@Override
	public String toString() {
		return (name==null?"null":name) + " #" + id + " POS:" + (position==null?"null":position) + " COND:" + (condition==null?"null":condition) + 
				" SLOT:" + (slot==null?"null":slot) + " " + (optional?"optional":"mandatory");  
	}
}