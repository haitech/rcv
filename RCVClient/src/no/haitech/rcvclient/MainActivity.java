/*
 * Copyright (C) 2013 Thomas Le
 * 
 * This file is part of RCVClient.
 *
 * RCVClient is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RCVClient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public license
 * along with RCVClient. If not, see <http://www.gnu.org/licenses/>.
 */
package no.haitech.rcvclient;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Main Activity.
 * Handling the login screen.
 * 
 * @author Thomas Le
 */
public class MainActivity extends Activity {
    private final String LOGIN = "no.haitech.rcvclient.LOGIN";
    private final String USERNAME = "no.haitech.rcvclient.USERNAME";
    private final String PASSWORD = "no.haitech.rcvclient.PASSWORD";
    private final String REMEMBER = "no.haitech.rcvclient.REMEMBER";
    
    // UI
    private CheckBox cbRememberMe;
    private EditText etUsername;
    private EditText etPassword;
    
    // Intent
    private Intent iVehicle;
    
    // Variables
    private String sUsername;
    private String sPassword;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Makes the window fullscreen, with titlebar.
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        // Views
        etUsername = (EditText) findViewById(R.id.eUsername);
        etPassword = (EditText) findViewById(R.id.ePassword);
        cbRememberMe = (CheckBox) findViewById(R.id.cbRememberMe);
        
        // Preferences
        SharedPreferences spLogin = getSharedPreferences(LOGIN, 0);
        boolean rememberMe = spLogin.getBoolean(REMEMBER, false);        
        // Check if user have preferences
        if(rememberMe) {
            etUsername.setText(spLogin.getString(USERNAME, null));
            etPassword.setText(spLogin.getString(USERNAME, null));
            cbRememberMe.setChecked(true);
        }
        
    }

    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    
    /**
     * Called when connect button is pushed.
     * Opens a Intent object to start an Activity
     * 
     * @see VehicleActivity
     * @param view
     */
    public void onConnect(View view) {
        sUsername = etUsername.getText().toString();
        sPassword = etPassword.getText().toString();
        
        if(cbRememberMe.isChecked()) {
            SharedPreferences spLogin = getSharedPreferences(LOGIN, 0);
            SharedPreferences.Editor spEditor = spLogin.edit();
            spEditor.putString(USERNAME, sUsername);
            spEditor.putString(PASSWORD, sPassword);
            spEditor.putBoolean(REMEMBER, cbRememberMe.isChecked());
            spEditor.commit();
        }
        
        iVehicle = new Intent(this, VehicleActivity.class);
        
        startActivity(iVehicle);
        finish();
    }

}
