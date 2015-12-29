package com.glaringnotebook.digitechpatchviewer;

import android.content.Context;
import android.content.SharedPreferences;

public class Chip {

	public static boolean getOptional(Context context) {
		SharedPreferences sr = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
	    return sr.getBoolean("optional", false);
	}
	
	public static void setOptional(Context context, boolean value) {
		SharedPreferences sr = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    	SharedPreferences.Editor sre = sr.edit();
    	sre.putBoolean("optional", value);
    	sre.commit();
	}

}