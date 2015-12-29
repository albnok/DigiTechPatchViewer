package com.glaringnotebook.digitechpatchviewer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
public class SplashActivity extends Activity {
	boolean canGoUp = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        boolean isHoneycomb = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        if (isHoneycomb) {
        	setTheme(android.R.style.Theme_Holo);
        	Intent intent = getIntent();
        	if (intent!=null) {
        		if (intent.getStringExtra("cangoback")!=null) {
        			canGoUp = true;
        			getActionBar().setDisplayHomeAsUpEnabled(true);
        		}
        	}
        }
        Button buttonEmail = (Button) findViewById(R.id.ButtonEmail);
        Button buttonWebsite = (Button) findViewById(R.id.ButtonWebsite);
        Button buttonWebsiteLaunch = (Button) findViewById(R.id.ButtonWebsiteLaunch);
        registerForContextMenu(buttonWebsiteLaunch);
        buttonEmail.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"albnok@hotmail.com"});  
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
				startActivity(Intent.createChooser(emailIntent, getString(R.string.email))); 	
			}
		});
        buttonWebsite.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			    Intent i = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.websiteurl)));
			    startActivity(i);  
			}
		});
        buttonWebsiteLaunch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				v.showContextMenu();
			}
		});
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_website, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.menu_rp155website:
			    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.websiterp155url))));  
	            return true;
	        case R.id.menu_rp255website:
			    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.websiterp255url))));  
	            return true;
	        case R.id.menu_rp255website2:
			    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.websiterp255url2))));  
	            return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (canGoUp) finish();
    	return true;
    }
}