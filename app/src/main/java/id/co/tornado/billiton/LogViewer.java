package id.co.tornado.billiton;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import id.co.tornado.billiton.handler.OnSwipeTouchListener;

public class LogViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);
        try {
            Process process = Runtime.getRuntime().exec("logcat -d *:I ");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            final TextView tv = (TextView)findViewById(R.id.textView1);
            tv.setMovementMethod(new ScrollingMovementMethod());
            tv.setText(log.toString());
            Button a = (Button) findViewById(R.id.buttonA);
            a.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(LogViewer.this, DatabaseViewer.class);
                    startActivity(intent);
                }
            });
//            tv.setOnTouchListener(new OnSwipeTouchListener(this) {
//                @Override
//                public void onSwipeLeft() {
//                    Intent intent = new Intent(LogViewer.this, DatabaseViewer.class);
//                    startActivity(intent);
//                }
//
//                @Override
//                public void onSwipeTop() {
//                    tv.scrollBy(0, tv.getScrollY()-20);
//                }
//
//                @Override
//                public void onSwipeBottom() {
//                    tv.scrollBy(0, tv.getScrollY()+20);
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
