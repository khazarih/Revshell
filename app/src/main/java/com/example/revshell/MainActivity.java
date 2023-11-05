package com.example.revshell;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        startService(serviceIntent);
        finish();
    }
    public void startForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        startService(serviceIntent);
    }
}