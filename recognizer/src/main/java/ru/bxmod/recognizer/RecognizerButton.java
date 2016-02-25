package ru.bxmod.recognizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by linnik.da on 20.02.2016.
 */
public class RecognizerButton extends ImageButton {

    // Используется в onDraw
    private Paint paint;

    // Используется в onDraw для рассчета размеров текста
    private Rect bounds;

    private int status;

    private long startTime, waitTime;
    private Time controlTimer;

    // Хендлер - обновление кнопки
    Handler handler;

    public RecognizerButton(final Context context) {
        super(context);
        init();
    }

    public RecognizerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecognizerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init () {

        status = RecognizerConfig.STATUS_OFF;

        controlTimer = new Time();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);

        bounds = new Rect();

        handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                invalidate();
                handler.postDelayed(this, 300);
            }
        };
        handler.postDelayed(r, 300);
    }

    public void setStatus( int status ) {

        switch( status ) {
            case RecognizerConfig.STATUS_ON:
                this.status = RecognizerConfig.STATUS_ON;
                controlTimer.setToNow();
                startTime = controlTimer.toMillis(false);
                setBackgroundResource(R.drawable.center_button_focused);
                break;
            case RecognizerConfig.STATUS_WAIT:
                this.status = RecognizerConfig.STATUS_WAIT;
                controlTimer.setToNow();
                waitTime = controlTimer.toMillis(false);
                setBackgroundResource(R.drawable.center_button_disabled);
                break;
            default:
                this.status = RecognizerConfig.STATUS_OFF;
                setBackgroundResource(R.drawable.center_button_default);
                break;
        }
    }

    // TODO пробросить эти параметры от родительского View

    // Нижний отступ микрофона от текста
    private int micMarginBottom = 5;
    // Верхний отступ микрофона от края кнопки
    private int micMarginTop = 10;
    // Нижний отступ текста от края кнопки
    private int textMarginBottom = 10;
    // Текст статуса при выключенном распознавании
    private String statusOffText = "Записать";
    // Текст статуса в режиме перезапуска из-за ошибки
    private String statusWaitText = "ждите %d сек";

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO Рисуем волну

        Drawable d;
        String buttonText;
        switch( status ) {
            case RecognizerConfig.STATUS_ON:
                buttonText = getTime();
                d = getResources().getDrawable(R.drawable.ic_microphone);
                break;
            case RecognizerConfig.STATUS_WAIT:
                controlTimer.setToNow();
                int seconds = (int) 5 - Math.round( (controlTimer.toMillis(false) - waitTime) / 1000 );
                buttonText = String.format(statusWaitText, seconds);
                d = getResources().getDrawable(R.drawable.ic_wait);
                break;
            default:
                buttonText = statusOffText;
                d = getResources().getDrawable(R.drawable.ic_microphone);
                break;
        }

        // Вычисляем размеры прямоугольника для рисования текста
        paint.getTextBounds(buttonText, 0, buttonText.length(), bounds);
        // Вычисляем позицию текста по-горизонтали
        int textXPos = (int) ((canvas.getWidth() / 2) - ( ( paint.measureText(buttonText, 0, buttonText.length()) )  / 2));
        // И рисуем текст
        canvas.drawText(buttonText, textXPos, canvas.getHeight() - textMarginBottom, paint);

        // Рисуем на кнопке микрофончик
        int micMarginLeft;
        int micSize;
        // Вычисляем размеры микрофона
        micSize = canvas.getHeight() - (micMarginTop + textMarginBottom + micMarginBottom + bounds.height());
        // Вычисляем позицию для рисования
        micMarginLeft = Math.round( canvas.getWidth() / 2 ) - Math.round( micSize / 2 );
        // И рисуем микрофон
        d.setBounds(micMarginLeft, micMarginTop, micSize + micMarginLeft, micSize + micMarginTop);
        d.draw(canvas);
    }

    private String getTime () {
        controlTimer.setToNow();

        int min = 0;
        long sec = (controlTimer.toMillis(false) - startTime) / 1000;

        min = (int) Math.ceil(sec / 60);
        sec = sec - ( min * 60 );

        return String.format("%02d", min) + ":" + String.format("%02d", sec);
    }
}
