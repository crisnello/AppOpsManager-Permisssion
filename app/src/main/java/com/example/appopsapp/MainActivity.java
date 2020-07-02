package com.example.appopsapp;

import java.util.ArrayList;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.MODE_DEFAULT;
import static android.app.AppOpsManager.MODE_ERRORED;
import static android.app.AppOpsManager.MODE_IGNORED;
import static android.app.AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW;

public class MainActivity extends AppCompatActivity implements OnOpChangedListener {

    private ArrayList<String> data = new ArrayList<>();
    private ArrayAdapter mAdapter;

    private AppOpsManager mAppOpsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppOpsManager = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        mAppOpsManager.startWatchingMode(OPSTR_SYSTEM_ALERT_WINDOW, getPackageName(), this);

        findViewById(R.id.action).setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                data.add("Permission requested");
                data.add("checkOp() result = " + getCurrentMode());
                data.add("Opening settings...");
                mAdapter.notifyDataSetChanged();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);
        ListView lv = (ListView) findViewById(R.id.log);
        lv.setAdapter(mAdapter);
        data.add("App started");
        data.add("checkOp() result = " + getCurrentMode());
        mAdapter.notifyDataSetChanged();

    }

    @Override public void onOpChanged(String op, String packageName) {
        if (OPSTR_SYSTEM_ALERT_WINDOW.equals(op) && getPackageName().equals(packageName)) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    data.add("AppOpsListenerCalled");
                    data.add("checkOp() result = " + getCurrentMode());
                    mAdapter.notifyDataSetChanged();
                }
            });

            if (getCurrentModeConst() == MODE_ALLOWED) {
                mAppOpsManager.stopWatchingMode(this);

                final Intent intent = new Intent(this, this.getClass());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    private String getCurrentMode() {
        switch (getCurrentModeConst()) {
            case MODE_ALLOWED:
                return "MODE_ALLOWED";
            case MODE_IGNORED:
                return "MODE_IGNORED";
            case MODE_ERRORED:
                return "MODE_ERRORED";
            case MODE_DEFAULT:
                return "MODE_DEFAULT";
            default:
                return "unknown";
        }
    }

    private int getCurrentModeConst() {
        return mAppOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, Process.myUid(), getPackageName());
    }
}
