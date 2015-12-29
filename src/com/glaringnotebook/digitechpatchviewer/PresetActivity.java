package com.glaringnotebook.digitechpatchviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PresetActivity extends Activity {
	static boolean optional;
	boolean isHoneycomb;
	PedalAdapter pedalAdapter;
	static int colorSetting, colorSettingOpt, colorValue, colorValueOpt, colorSettingBackground, colorSettingBackgroundOpt;
	static int colorPedalSetOpt, colorPedalSet, colorPedal, colorPedalOpt;
	static List<Pedal> listPedals = new ArrayList<Pedal>(), listPedalsOptional = new ArrayList<Pedal>();
	static HashMap<Integer, Param> hashmapParams;
	static HashMap<String, Param> hashmapParamsWithPosition;
	final static Param blankParam = new Param();
	static Param unsettableParam = new Param();
	static boolean loadedEverything = false;
	static String savedTitle;	
	String tonePath;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isHoneycomb = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        // Theme_Holo_Light not so pretty because the white area goes behind the listview!
        //if (isHoneycomb) setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.main);
        if (tonePath==null) tonePath = "//mnt//sdcard//download//pedalswitch/FAASped13eq.rp255p";
        Uri uri = getIntent().getData();
        if (uri!=null) tonePath = uri.getPath();
        if (savedTitle==null) savedTitle = getString(R.string.loading);
        unsettableParam.text = Param.UNSETTABLE;
    	setTitle(savedTitle);
    	if (!loadedEverything) {
    		Resources res = getResources();
	        colorSetting = res.getColor(R.color.setting);
	    	colorSettingOpt = res.getColor(R.color.settingOpt);
	    	colorValue = res.getColor(R.color.value);
	    	colorValueOpt = res.getColor(R.color.valueOpt);
	    	colorSettingBackground = res.getColor(R.color.settingBackground);
	    	colorSettingBackgroundOpt = res.getColor(R.color.settingBackgroundOpt);
	    	colorPedalSetOpt = res.getColor(R.color.pedalsetOpt);
	    	colorPedalSet = res.getColor(R.color.pedalset);
	    	colorPedal = res.getColor(R.color.pedal);
	    	colorPedalOpt = res.getColor(R.color.pedalOpt);
    		optional = Chip.getOptional(this);
    		LoadTask loadTask = new LoadTask();
    		loadTask.execute(tonePath);
    	}
        ListView lvPedals = (ListView) findViewById(R.id.ListViewPedals);
        pedalAdapter = new PedalAdapter(this);
        pedalAdapter.setList(optional?listPedalsOptional:listPedals);
        lvPedals.setAdapter(pedalAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
		return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem menuOptional = menu.findItem(R.id.menu_optional);
    	menuOptional.setTitle(getText(Chip.getOptional(this)?R.string.optionalon:R.string.optionaloff));
    	return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_optional:
        	optional = !Chip.getOptional(this);
        	Chip.setOptional(this, optional);
        	pedalAdapter.setList(optional?listPedalsOptional:listPedals);
        	pedalAdapter.notifyDataSetChanged();
        	if (isHoneycomb) invalidateOptionsMenu();
            return true;
        case R.id.menu_about:
        	startActivity(new Intent(PresetActivity.this, SplashActivity.class).putExtra("cangoback", "y"));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onDestroy() {
    	if (isFinishing()) loadedEverything = false;
    	super.onDestroy();
    }
    /** returns the Param of the knob */
    private static Param getValue(Knob knob) {
    	if (knob.id.equals(-2)) return unsettableParam;
    	if (knob.id.equals(-1)) return blankParam;
    	if (knob.position==null) {
    		if (hashmapParams.get(knob.id)==null) return blankParam; 
    		return hashmapParams.get(knob.id);
    	}
    	if (hashmapParamsWithPosition.get(knob.id + "+" + knob.position)==null) return blankParam;
    	return hashmapParamsWithPosition.get(knob.id + "+" + knob.position);
    }
    private static class PedalAdapter extends BaseAdapter {
    	LayoutInflater mInflater;
		static ViewHolder holder;
		static Integer condition;
		static Knob enabledKnob, typeKnob, currentKnob;
		static Param foundParam;
		static Pedal pedal;
		static int knobCount;
		static List<Pedal> mListPedals = new ArrayList<Pedal>();
    	public PedalAdapter(Context context) {
    		mInflater = LayoutInflater.from(context);
    	}
    	public void setList(List<Pedal> list) {
    		mListPedals = list;
    	}
		public int getCount() {
			return mListPedals.size();
		}
		public Object getItem(int position) {
			return mListPedals.get(position);
		}
		public long getItemId(int position) {
			return 0;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			pedal = mListPedals.get(position);
			if (convertView==null) {
				convertView = mInflater.inflate(R.layout.list_pedal, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.TextViewPedal);
				holder.pedaltype = (TextView) convertView.findViewById(R.id.TextViewPedalType);
				holder.light = (ImageView) convertView.findViewById(R.id.ImageViewPedalSwitch);
				holder.settings = (LinearLayout) convertView.findViewById(R.id.LinearLayoutOptionalSettings);
				holder.values = (LinearLayout) convertView.findViewById(R.id.LinearLayoutOptionalValues);
				holder.setting1 = (TextView) convertView.findViewById(R.id.TextViewSettings1);
				holder.setting2 = (TextView) convertView.findViewById(R.id.TextViewSettings2);
				holder.setting3 = (TextView) convertView.findViewById(R.id.TextViewSettings3);
				holder.setting4 = (TextView) convertView.findViewById(R.id.TextViewSettings4);
				holder.setting5 = (TextView) convertView.findViewById(R.id.TextViewSettings5);
				holder.setting6 = (TextView) convertView.findViewById(R.id.TextViewSettings6);
				holder.value1 = (TextView) convertView.findViewById(R.id.TextViewSettingsValue1);
				holder.value2 = (TextView) convertView.findViewById(R.id.TextViewSettingsValue2);
				holder.value3 = (TextView) convertView.findViewById(R.id.TextViewSettingsValue3);
				holder.value4 = (TextView) convertView.findViewById(R.id.TextViewSettingsValue4);
				holder.value5 = (TextView) convertView.findViewById(R.id.TextViewSettingsValue5);
				holder.value6 = (TextView) convertView.findViewById(R.id.TextViewSettingsValue6);			
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			holder.text.setText(pedal.name);
			enabledKnob = new Knob();
			enabledKnob.id = pedal.enabled;
			typeKnob = new Knob();
			typeKnob.id = pedal.type;
			typeKnob.position = pedal.position;
			foundParam = getValue(typeKnob);
			if (foundParam.value==null) {
				condition = -1;
			} else {
				condition = Integer.parseInt(foundParam.value);
			}
			if (foundParam.text.equals(Param.BLANK)) {
				holder.pedaltype.setText("");
			} else {
				holder.pedaltype.setText(foundParam.text);
			}
			holder.light.setVisibility(new String("On").equals(getValue(enabledKnob).text)?View.VISIBLE:View.INVISIBLE);
			holder.pedaltype.setTextColor(pedal.optional?colorValueOpt:colorValue);
			holder.text.setTextColor(pedal.optional?colorPedalOpt:colorPedal);
			knobCount = pedal.getSize(condition);
			if (knobCount>0) {
				currentKnob = pedal.getPosition(0, condition);
				holder.setting1.setTextColor(currentKnob.optional?colorSettingOpt:colorSetting);
				holder.setting1.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.value1.setTextColor(currentKnob.optional?colorValueOpt:colorValue);
				holder.value1.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.setting1.setText(currentKnob.name);
				holder.value1.setText(getValue(currentKnob).text);
			} else {
				holder.setting1.setTextColor(colorSettingOpt);
				holder.setting1.setBackgroundColor(colorSettingBackgroundOpt);
				holder.value1.setTextColor(colorValueOpt);
				holder.value1.setBackgroundColor(colorSettingBackgroundOpt);
				holder.setting1.setText("");
				holder.value1.setText("");
			}
			if (knobCount>1) {
				currentKnob = pedal.getPosition(1, condition);
				holder.setting2.setTextColor(currentKnob.optional?colorSettingOpt:colorSetting);
				holder.setting2.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.value2.setTextColor(currentKnob.optional?colorValueOpt:colorValue);
				holder.value2.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.setting2.setText(currentKnob.name);
				holder.value2.setText(getValue(currentKnob).text);
			} else {
				holder.setting2.setTextColor(colorSettingOpt);
				holder.setting2.setBackgroundColor(colorSettingBackgroundOpt);
				holder.value2.setTextColor(colorValueOpt);
				holder.value2.setBackgroundColor(colorSettingBackgroundOpt);
				holder.setting2.setText("");
				holder.value2.setText("");
			}
			if (knobCount>2) {
				currentKnob = pedal.getPosition(2, condition);
				holder.setting3.setTextColor(currentKnob.optional?colorSettingOpt:colorSetting);
				holder.setting3.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.value3.setTextColor(currentKnob.optional?colorValueOpt:colorValue);
				holder.value3.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.setting3.setText(currentKnob.name);
				holder.value3.setText(getValue(currentKnob).text);
			} else {
				holder.setting3.setTextColor(colorSettingOpt);
				holder.setting3.setBackgroundColor(colorSettingBackgroundOpt);
				holder.value3.setTextColor(colorValueOpt);
				holder.value3.setBackgroundColor(colorSettingBackgroundOpt);
				holder.setting3.setText("");
				holder.value3.setText("");
			}
			if (knobCount>3) {
				currentKnob = pedal.getPosition(3, condition);
				holder.setting4.setTextColor(currentKnob.optional?colorSettingOpt:colorSetting);
				holder.setting4.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.value4.setTextColor(currentKnob.optional?colorValueOpt:colorValue);
				holder.value4.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.setting4.setText(currentKnob.name);
				holder.value4.setText(getValue(currentKnob).text);
			} else {
				holder.setting4.setTextColor(colorSettingOpt);
				holder.setting4.setBackgroundColor(colorSettingBackgroundOpt);
				holder.value4.setTextColor(colorValueOpt);
				holder.value4.setBackgroundColor(colorSettingBackgroundOpt);
				holder.setting4.setText("");
				holder.value4.setText("");
			}
			if (knobCount>4) {
				currentKnob = pedal.getPosition(4, condition);
				holder.setting5.setTextColor(currentKnob.optional?colorSettingOpt:colorSetting);
				holder.setting5.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.value5.setTextColor(currentKnob.optional?colorValueOpt:colorValue);
				holder.value5.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.setting5.setText(currentKnob.name);
				holder.value5.setText(getValue(currentKnob).text);
			} else {
				holder.setting5.setTextColor(colorSettingOpt);
				holder.setting5.setBackgroundColor(colorSettingBackgroundOpt);
				holder.value5.setTextColor(colorValueOpt);
				holder.value5.setBackgroundColor(colorSettingBackgroundOpt);
				holder.setting5.setText("");
				holder.value5.setText("");
			}
			if (knobCount>5) {
				currentKnob = pedal.getPosition(5, condition);
				holder.setting6.setTextColor(currentKnob.optional?colorSettingOpt:colorSetting);
				holder.setting6.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.value6.setTextColor(currentKnob.optional?colorValueOpt:colorValue);
				holder.value6.setBackgroundColor(currentKnob.optional?colorSettingBackgroundOpt:colorSettingBackground);
				holder.setting6.setText(currentKnob.name);
				holder.value6.setText(getValue(currentKnob).text);
			} else {
				holder.setting6.setTextColor(colorSettingOpt);
				holder.setting6.setBackgroundColor(colorSettingBackgroundOpt);
				holder.value6.setTextColor(colorValueOpt);
				holder.value6.setBackgroundColor(colorSettingBackgroundOpt);
				holder.setting6.setText("");
				holder.value6.setText("");
			}
			holder.settings.setVisibility(knobCount>3?View.VISIBLE:View.GONE);
			holder.values.setVisibility(knobCount>3?View.VISIBLE:View.GONE);
			return convertView;
		}
    }
    private class LoadTask extends AsyncTask<String, Integer, Integer> {
    	String toneLayout = "rp255layout.xml";
    	String tonePath;
		@Override
		protected void onPreExecute() {
	        ListView lvPedals = (ListView) findViewById(R.id.ListViewPedals);
	        if (lvPedals!=null) lvPedals.setVisibility(View.GONE);
	        setTitle(getString(R.string.loading));
		}
		@Override
		protected Integer doInBackground(String... args) {
			listPedals = new ArrayList<Pedal>();
    		listPedalsOptional = new ArrayList<Pedal>();
    		hashmapParams = new HashMap<Integer, Param>();
    		hashmapParamsWithPosition = new HashMap<String, Param>();
	        InputStream is = null;
	        FileInputStream fis = null;
	        try {
	        	if (args!=null) {
        			tonePath = args[0];
	        		if (tonePath!=null) {
	        			if (tonePath.toLowerCase().contains(".rp155p")) toneLayout = "rp155layout.xml";
	        			if (tonePath.toLowerCase().contains(".rp250p")) toneLayout = "rp250layout.xml";
	        			Log.d("tonePath", tonePath.toLowerCase() + " layout: " + toneLayout);
	        		}
	        	}
	        	is = getAssets().open(toneLayout);
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
				doc.getDocumentElement().normalize();
				NodeList knobs, pedals = doc.getElementsByTagName("pedal");
				NamedNodeMap elements, knobElements;
				Pedal pedal, pedalOptional;
				Knob knob;
				for (int i = 0; i < pedals.getLength(); i++) {
					elements = pedals.item(i).getAttributes();
					pedal = new Pedal();
					pedal.name = elements.getNamedItem("name").getNodeValue();
					pedal.type = Integer.parseInt(elements.getNamedItem("type").getNodeValue());
					pedal.enabled = Integer.parseInt(elements.getNamedItem("enabled").getNodeValue());
					if (elements.getNamedItem("pos")!=null) pedal.position = Integer.parseInt(elements.getNamedItem("pos").getNodeValue());
					if (elements.getNamedItem("opt")!=null) pedal.optional = true;
					//copy everything over manually, to avoid the pointer
					pedalOptional = new Pedal();
					pedalOptional.name = pedal.name;
					pedalOptional.type = pedal.type;
					pedalOptional.enabled = pedal.enabled;
					pedalOptional.position = pedal.position;
					pedalOptional.optional = pedal.optional;				
					knobs = pedals.item(i).getChildNodes();
					for (int j = 0; j < knobs.getLength(); j++) {
						if (knobs.item(j).getNodeName().equals("knob")) {
							knobElements = knobs.item(j).getAttributes();
							knob = new Knob();
							for (int k = 0; k<knobElements.getLength(); k++) {
								if (knobElements.item(k).getNodeName().equals("opt")) knob.optional = true;
								if (knobElements.item(k).getNodeName().equals("name")) knob.name = knobElements.item(k).getNodeValue();
								if (knobElements.item(k).getNodeName().equals("id")) knob.id = Integer.parseInt(knobElements.item(k).getNodeValue());
								if (knobElements.item(k).getNodeName().equals("pos")) knob.position = Integer.parseInt(knobElements.item(k).getNodeValue());
								if (knobElements.item(k).getNodeName().equals("condition")) knob.condition = Integer.parseInt(knobElements.item(k).getNodeValue());
								if (knobElements.item(k).getNodeName().equals("slot")) knob.slot = Integer.parseInt(knobElements.item(k).getNodeValue());
							}
							if (!knob.optional) pedal.knobs.add(knob);
							pedalOptional.knobs.add(knob);
						}
						// TODO if opt=y and value is NOT found, \don't show it, we must check condition, and add if matched
					}				
					if (!pedal.optional) listPedals.add(pedal);
					listPedalsOptional.add(pedalOptional);
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        if (is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }	        
	        try {
	        	File toneFile = new File(tonePath);
	        	fis = new FileInputStream(toneFile);
	        	Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fis);
				doc.getDocumentElement().normalize();
				NodeList name = doc.getElementsByTagName("Name");
				savedTitle = name.item(0).getTextContent() + " (" + tonePath.substring(tonePath.lastIndexOf("/") + 1) + ")";
				NodeList params = doc.getElementsByTagName("Param");
				for (int i = 0; i<params.getLength(); i++) {
					NodeList param = params.item(i).getChildNodes();
					Param paramToAdd = new Param();
					for (int j = 0; j<param.getLength(); j++) {
						if (param.item(j).getNodeName().equals("ID")) paramToAdd.id = Integer.parseInt(param.item(j).getTextContent());
						if (param.item(j).getNodeName().equals("Position")) paramToAdd.position = Integer.parseInt(param.item(j).getTextContent());
						if (param.item(j).getNodeName().equals("Value")) paramToAdd.value = param.item(j).getTextContent();
						if (param.item(j).getNodeName().equals("Name")) paramToAdd.name = param.item(j).getTextContent();
						if (param.item(j).getNodeName().equals("Text")) paramToAdd.text = param.item(j).getTextContent();
					}
					paramToAdd.idposition = paramToAdd.id + "+" + paramToAdd.position;
					hashmapParams.put(paramToAdd.id, paramToAdd);
					hashmapParamsWithPosition.put(paramToAdd.idposition, paramToAdd);
				}
				for (Integer key: hashmapParams.keySet()) Log.d("param by key #" + key, hashmapParams.get(key).toString());
				for (String key: hashmapParamsWithPosition.keySet()) Log.d("param by idposition #" + key, hashmapParamsWithPosition.get(key).toString());
	        } catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        if (fis!=null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        
        	Integer condition;
        	Knob typeKnob;
        	Param foundParam;
        	//experimental cacher
	        for (Pedal pedal: listPedals) {
		        typeKnob = new Knob();
		        typeKnob.id = pedal.type;
		        typeKnob.position = pedal.position;
		        foundParam = getValue(typeKnob);
		        if (foundParam.value==null) {
		        	condition = -1;
		        } else {
		        	condition = Integer.parseInt(foundParam.value);
		        }
		        pedal.getSize(condition);
		        for (int i=0; i<6; i++) typeKnob = pedal.getPosition(i, condition);
	        }
	        for (Pedal pedal: listPedalsOptional) {
		        typeKnob = new Knob();
		        typeKnob.id = pedal.type;
		        typeKnob.position = pedal.position;
		        foundParam = getValue(typeKnob);
		        if (foundParam.value==null) {
		        	condition = -1;
		        } else {
		        	condition = Integer.parseInt(foundParam.value);
		        }
		        pedal.getSize(condition);
		        for (int i=0; i<6; i++) typeKnob = pedal.getPosition(i, condition);
	        }
	        loadedEverything = true;
			return 0;
		}
		@Override
		protected void onPostExecute(Integer result) {
			if (!isFinishing()) {
		        pedalAdapter.setList(optional?listPedalsOptional:listPedals);
		        pedalAdapter.notifyDataSetChanged();
		        setTitle(savedTitle);
		        ListView lvPedals = (ListView) findViewById(R.id.ListViewPedals);
		        if (lvPedals!=null) lvPedals.setVisibility(View.VISIBLE);
			}
		}
    }
}