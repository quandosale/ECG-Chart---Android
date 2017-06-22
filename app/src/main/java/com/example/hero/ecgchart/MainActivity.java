package com.example.hero.ecgchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ECGChart mECGSweepChart;

    Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mECGSweepChart = (ECGChart) findViewById(R.id.ecg_sweep_chart);
        new Thread() {
            public void run() {
                int counter = 0;
                while (counter++ < 1000) {
                    try {
                        rand.nextInt(1250);
                        final double offset = 0.1;
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                int inputSize = 240;
                                int[] ecgArray = new int[inputSize];
                                double angle = 0;
                                double interval = Math.PI / inputSize * 2;
                                int randHeight = rand.nextInt(1250) % 1250;
                                for(int i = 0; i < inputSize; i ++) {
                                    ecgArray[i] = (int)(Math.sin(angle) * randHeight + 1250);
                                    angle += interval;
                                }
                                mECGSweepChart.addEcgData(ecgArray);
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        final Button button = (Button)findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mECGSweepChart.toggleFullscreen();
            }
        });
    }
}
