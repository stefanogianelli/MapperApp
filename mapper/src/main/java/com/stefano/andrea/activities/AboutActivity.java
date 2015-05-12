package com.stefano.andrea.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Calendar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versione_app = (TextView) findViewById(R.id.versione_app);
        TextView anno_inc = (TextView) findViewById(R.id.anno_inc);


        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionNumber = pinfo.versionCode;
        String versionName = pinfo.versionName;


        versione_app.setText("Versione "+versionName);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        anno_inc.setText("@ "+year+" MappApp inc.");
    }

}
