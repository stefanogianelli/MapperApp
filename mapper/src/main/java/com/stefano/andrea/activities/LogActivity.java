package com.stefano.andrea.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogActivity extends AppCompatActivity {

    private static final String TAG = "LogActivity";

    private static final String FOLDER = "Mapper";
    private static final String FILENAME = "mapper_log.txt";

    private TextView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        mLogView = (TextView) findViewById(R.id.log_textview);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.log_scrollview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.log_activity_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String separator = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("E")) {
                    appendColoredText(mLogView, line, Color.RED);
                } else if (line.startsWith("W")) {
                    appendColoredText(mLogView, line, Color.BLUE);
                } else {
                    appendColoredText(mLogView, line, Color.BLACK);
                }
                mLogView.append(separator);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException during log reading " + e.getMessage());
        }
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send_log) {
            writeLogFile();
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("application/image");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"stefano.gianelli@outlook.com"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Mapper] Bug report");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Invio log");
            String path = Environment.getExternalStorageDirectory() + "/" + FOLDER + "/" + FILENAME;
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
            startActivity(Intent.createChooser(emailIntent, "Invia mail..."));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    private void writeLogFile () {
        File path = new File(Environment.getExternalStorageDirectory(), "/" + FOLDER);
        if (!path.exists()) {
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        }
        try {
            FileOutputStream overWrite = new FileOutputStream(path + "/" + FILENAME, false);
            overWrite.write(mLogView.getText().toString().getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (IOException e) {
            Log.e(TAG, "Errore durante la creazione del file di log: " + e.getMessage());
        }
    }
}
