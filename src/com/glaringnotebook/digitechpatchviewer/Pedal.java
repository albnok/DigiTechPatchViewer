package com.glaringnotebook.digitechpatchviewer;

import java.util.ArrayList;
import java.util.List;

class Pedal {
	String name;
	Integer type, enabled, position, cachedSize;
	boolean optional = false;
	List<Knob> knobs = new ArrayList<Knob>();
	List<Knob> cachedKnobs = new ArrayList<Knob>();
	
	@Override
	public String toString() {
		String list = "";
		for (Knob knob: knobs) {
			list = list + knob.name + ": " + knob.id;
			if (knob.optional) list+=" OPTIONAL";
			list+=" ";
		}
		return name + " #" + type + " enabled: " + enabled + " " + list;
	}
	
	/** returns the size, after a count */
	public Integer getSize(Integer desiredCondition) {
		if (cachedSize==null) {
			//Log.e("getSize for " + name, "condition: " + (desiredCondition==null?"null":desiredCondition));
			int slotCount = 0, conditionalSlotCount = 0;
			for (Knob knob: knobs) {
				//Log.d("knobs under " + name, knob.toString());
				if (knob.condition!=null) {
					if (knob.condition.equals(desiredCondition)) {
						if (knob.slot + 1>conditionalSlotCount) conditionalSlotCount = knob.slot + 1;
					}
				} else {
					// condition is null therefore we always add this.
					slotCount++;
				}
			}
			cachedSize = Math.max(slotCount, conditionalSlotCount);
			//Log.e("getSize 99", "returns: " + cachedSize);
		}
		return cachedSize;
	}
	
	/** returns the pedal setting based on condition, if any, and slot override */
	public Knob getPosition(int desiredSlot, Integer desiredCondition) {
		Knob possibleKnob = null;
		int slot = 0;
		if (cachedKnobs.size()<=desiredSlot) {
			//Log.d("getPosition #" + desiredSlot + " for " + name, "condition: " + (desiredCondition==null?"null":desiredCondition));
			for (Knob knob: knobs) {
				//Log.d("knobs under " + name, knob.toString());
				if (knob.condition!=null) {
					if (knob.condition.equals(desiredCondition) && knob.slot.equals(desiredSlot)) {
						//Log.e("knob MATCH!", "based on same condition and slot");
						possibleKnob = knob;
					}
				} else {
					if (slot==desiredSlot) {
						//Log.e("knob MATCH!", "based on NO condition and auto-incrementing slot");
						possibleKnob = knob;
					}
					slot++;
				}
			}
			cachedKnobs.add(desiredSlot, possibleKnob);
		}
		return cachedKnobs.get(desiredSlot);
	}
}