package com.example.hero.ecgchart;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by hero on 6/22/2017.
 */

public class ECGChart extends View {
    static final int SWEEP_MODE = 0;
    static final int FLOW_MODE = 1;
    private int mLineColor;
    private int mWindowSize;
    private final int ONEWINDOW = 240;
    private LinkedBlockingDeque<Integer> mInputBuf;
    private Vector<Integer> mDrawingBuf;

    private Paint mPaint;
    private Paint mMaskBarPaint;
    private int mDrawPosition;

    private Activity mActivity;

    private int mGraphMode = 1;

    public ECGChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (Activity)context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ECGChart,
                0, 0);

        try {
            mLineColor = a.getColor(R.styleable.ECGChart_lineColor, Color.WHITE);
            mGraphMode = a.getColor(R.styleable.ECGChart_graphMode, SWEEP_MODE);
            mWindowSize = a.getColor(R.styleable.ECGChart_windowSize, ONEWINDOW * 3);

            mInputBuf = new LinkedBlockingDeque<>();
            mDrawingBuf = new Vector<>();

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(mLineColor);
            mMaskBarPaint = new Paint();
            mMaskBarPaint.setColor(Color.rgb(0x33,0x33,0x33));
            mMaskBarPaint.setStyle(Paint.Style.FILL);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        for(int i = 0 ; i < mWindowSize; i++)
            mDrawingBuf.add(1250);

        int redrawInterval = 50;
        final int redrawPoints = ONEWINDOW / (1000/redrawInterval);
        TimerTask drawEmitter = new TimerTask() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mInputBuf.size() < ONEWINDOW)
                            return;
                        if(mGraphMode == SWEEP_MODE) {
                            for(int i = 0 ; i < redrawPoints; i++) {
                                int val = mInputBuf.pollFirst();
                                mDrawingBuf.remove(mDrawPosition);
                                mDrawingBuf.add(mDrawPosition ++, val);
                                if(mDrawPosition >= mWindowSize) mDrawPosition = 0;
                            }
                        }
                        else {
                            for(int i = 0 ; i < redrawPoints; i++) {
                                int val = mInputBuf.pollFirst();
                                mDrawingBuf.remove(0);
                                mDrawingBuf.add(val);
                            }
                        }

                        invalidate();
                        Log.d("inputBufSize", mInputBuf.size() + "");
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(drawEmitter, 0, redrawInterval);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = this.getWidth();
        float height = this.getHeight();

        if(mDrawingBuf.size() < mWindowSize)
            return;

        float mapRatio = (float)width / mWindowSize;
        int start = mDrawingBuf.get(0);
        for(int i = 1 ; i < mWindowSize; i ++) {
            int end = mDrawingBuf.get(i);
            canvas.drawLine(i * mapRatio, start / 2500.f * height, (i + 1) * mapRatio, end / 2500.f * height , mPaint);
            start = end;
        }
        if(mGraphMode == SWEEP_MODE)
            canvas.drawRect((mDrawPosition-5) * mapRatio, 0, (mDrawPosition + 5) * mapRatio, height, mMaskBarPaint);
    }
    public void addEcgData(int[] data) {
        checkBufOverflow();
        for(int i = 0 ; i < data.length; i++) {
            mInputBuf.addLast(data[i]);
        }
    }
    private void checkBufOverflow() {
        if(mInputBuf.size() > 2000)
            mInputBuf.clear();
    }
}
