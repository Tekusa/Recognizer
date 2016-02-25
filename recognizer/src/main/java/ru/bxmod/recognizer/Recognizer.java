package ru.bxmod.recognizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by linnik.da on 17.02.2016.
 */

public class Recognizer extends LinearLayout implements RecognitionListener {

    private Context context;
    private View layout;
    private String LOG_TAG = "######";

    private EditText elementEditText;
    private RecognizerButton elementButtonRecord;
    private ImageButton elementButtonClear;
    private ImageButton elementButtonSave;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    // Текущий статус работы
    private String lastStatus = "off";

    // Хендлер для лупера, проверяющего наличие текста в текстовом поле
    private Handler checkHandler;

    // Флаг, нужен для переключения кнопки записи
    private boolean recordStatus = false;

    // Флаг нажатия на кнопку записи. Необходим для контроля типа прекращения записи.
    private boolean isPressed = false;

    // Подписчики на событие Apply
    public ArrayList<RecognizerListener> listeners = new ArrayList<RecognizerListener>();

    public Recognizer(Context context) {
        super(context);
        initializeViews(context);
    }

    public Recognizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setViewAttributes(context, attrs);
        initializeViews(context);
    }

    public Recognizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setViewAttributes(context, attrs);
        initializeViews(context);
    }

    // Организация подписки на событие "Принять"
    public void setApplyListener(RecognizerListener listener){
        listeners.add(listener);
    }

    // Получаем установленные для View в XML разметке атрибуты
    private void setViewAttributes ( Context context, AttributeSet attrs ) {
        TypedArray a = context.getTheme().obtainStyledAttributes( attrs, R.styleable.Recognizer, 0, 0);

        try {
            Log.d("######", "Показ текста:" + String.valueOf( a.getBoolean(R.styleable.Recognizer_recognizerShowText, false) ));
            Log.d("######", "Выравнивание текста:" + String.valueOf( a.getInteger(R.styleable.Recognizer_recognizerLabelPosition, 0) ));
        } finally {
            a.recycle();
        }
    }

    public void startRecognize () {
        startRecognize (lastStatus);
    }
    public void startRecognize ( String status ) {

        lastStatus = status;

        switch( lastStatus ) {
            case "on":
                break;
            case "off":
                elementButtonRecord.setStatus(RecognizerConfig.STATUS_ON);
                speech.startListening(recognizerIntent);
                lastStatus = "on";
                break;
            case "error":
                Handler timerHandler = new Handler();
                timerHandler.postDelayed(new Runnable() {
                    public void run() {
                        if ( lastStatus.equals("error") ) {
                            elementButtonRecord.setStatus(RecognizerConfig.STATUS_ON);
                            speech.startListening(recognizerIntent);
                            lastStatus = "on";
                        }
                    }
                }, 5000);
                elementButtonRecord.setStatus(RecognizerConfig.STATUS_WAIT);
                break;
            default:
                break;
        }
    }

    private void initializeViews(Context context) {

        this.context = context;

        Activity activity = (Activity) context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.recognizer, this);

        elementEditText = (EditText) layout.findViewById(R.id.editText);
        elementButtonRecord = (RecognizerButton) layout.findViewById(R.id.buttonRecord);
        elementButtonClear = (ImageButton) layout.findViewById(R.id.buttonClear);
        elementButtonSave = (ImageButton) layout.findViewById(R.id.buttonSave);

        elementButtonSave.setEnabled(false);
        elementButtonClear.setEnabled(false);

        // Событие клика на кнопке Apply
        elementButtonSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();

                // Отправка результата всем подписчикам
                for (RecognizerListener listener:listeners){
                    listener.onRecognizerApply( elementEditText.getText().toString() );
                }
            }
        });

        speech = SpeechRecognizer.createSpeechRecognizer(activity);
        speech.setRecognitionListener(this);

        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 300);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        elementButtonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recordStatus) {
                    startRecognize("off");
                } else {
                    stop();
                }
                recordStatus = !recordStatus;
            }
        });

        elementButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
                elementEditText.setText("");
            }
        });

        checkHandler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                if ( elementEditText.getText().length() > 0 ) {
                    elementButtonSave.setEnabled(true);
                    elementButtonClear.setEnabled(true);
                } else {
                    elementButtonSave.setEnabled(false);
                    elementButtonClear.setEnabled(false);
                }
                checkHandler.postDelayed(this, 300);
            }
        };
        checkHandler.postDelayed(r, 300);
    }

    public void stop () {
        lastStatus = "off";
        isPressed = true;
        speech.stopListening();
        elementButtonRecord.setStatus(RecognizerConfig.STATUS_OFF);
    }

    /**
     * Очистить поле ввода
     */
    public void clear() {
        elementEditText.setText("");
    }

    /**
     * Получить текст из поля ввода
     */
    public String getText() {
        return elementEditText.getText().toString();
    }

    /**
     * Установить текст в поле ввода
     */
    public void setText( String text ) {
        elementEditText.setText( text );
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String text = "Возникла ошибка";

        if ( matches.size() > 0 ) {
            text = matches.get(0);
        }

        elementEditText.setText( elementEditText.getText() + " " + text);
        elementEditText.setSelection(elementEditText.getText().length());

        if ( !isPressed ) {
            startRecognize("off");
        }
        isPressed = false;
    }

    @Override
    public void onError(int errorCode) {
        if (!lastStatus.equals("off")) {
            String errorMessage = getErrorText(errorCode);
            Toast.makeText((Activity) context, errorMessage, Toast.LENGTH_SHORT).show();

            startRecognize("error");
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle results) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB){
        Log.i(LOG_TAG, "onRmsChanged");
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Ошибка записи аудио";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Ошибка на стороне клиента";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Не достаточно прав";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Ошибка сети";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Таймаут соединения";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "Вы ничего не сказали";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Сервер распознавания занят";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Ошибка на сервере";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "Вы ничего не сказали";
                break;
            default:
                message = "Ничего не понятно, повторите еще";
                break;
        }
        return message;
    }


}
