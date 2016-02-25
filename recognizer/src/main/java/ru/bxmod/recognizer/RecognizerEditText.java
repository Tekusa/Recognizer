package ru.bxmod.recognizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import java.util.Random;

/**
 * Created by BXMOD on 18.02.2016.
 */
public class RecognizerEditText extends EditText {

    private int recordColor;

    private int notRecordingColor;

    public RecognizerEditText(final Context context) {
        super(context);
        init();
    }

    public RecognizerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecognizerEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void refresh ( float value ) {
        angle = value * 10;
        this.invalidate();
    }

    private float angle = 180;
    Paint paint;
    Random g;
    float X, Y;

    private void init () {
        g = new Random();
        paint = new Paint( Paint.ANTI_ALIAS_FLAG );
    }

    @Override
    protected void onDraw (Canvas c) {
        //float i;

        paint.setColor (Color.WHITE);
        c.drawPaint (paint);

        paint.setAntiAlias (true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //paint.setStyle (Paint.Style.STROKE);
        paint.setColor (Color.BLACK);
        paint.setStrokeWidth(3);

        float koef = angle / 100;
        if ( koef < 0 ) koef = 0;

        Log.d("######", "Коэф: " + koef);

        /*for ( float i = 0; i < getMeasuredWidth(); i = i + 1) {
            c.drawLine (
                    i,
                    (getMeasuredHeight() - getMeasuredHeight() * koef) + (float)Math.sin(i/180.0*Math.PI),
                    (i + 1),
                    (getMeasuredHeight() - getMeasuredHeight() * koef) + (float)Math.sin((i + 10)/180.0*Math.PI),
                    paint
            );
        }*/

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        path.moveTo(0, (getMeasuredHeight() - getMeasuredHeight() * koef));

        for ( float i = 0; i < getMeasuredWidth(); i = i + 1) {
            path.lineTo(i, (getMeasuredHeight() - getMeasuredHeight() * koef) + (float)Math.sin((i + 100)/180.0*Math.PI));
        }

        path.lineTo(getMeasuredWidth(), getMeasuredHeight());
        path.lineTo(0, getMeasuredHeight());
        path.lineTo(0, (getMeasuredHeight() - getMeasuredHeight() * koef));
        path.close();

        c.drawPath(path, paint);

        super.onDraw (c);
    }
}
