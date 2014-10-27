package com.samsung.gear.gsonsapproviderdemo;

import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.samsung.gear.gsonsapproviderdemo.data.HelloMessage;
import com.samsung.gear.gsonsapproviderservice.GsonSapProvider;
import com.samsung.gear.gsonsapproviderservice.GsonSapProvider.GsonSapProviderBinder;

public class MainActivity extends Activity {

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((GsonSapProviderBinder) service).getService();
        }
    };

    private GsonSapProvider mService;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent service = new Intent(this, GsonSapSampleService.class);
        bindService(service, mConnection, BIND_AUTO_CREATE);

        mTextView = (TextView) findViewById(R.id.message);
        findViewById(R.id.send).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mService) {
                    try {
                        mService.send(new HelloMessage(mTextView.getText().toString()));
                    } catch (IOException e) {
                        Log.e("GsonSapSample", e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
