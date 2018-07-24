package com.date0619.carcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import static com.date0619.carcontrol.RockerView.DirectionMode.DIRECTION_4_ROTATE_45;

public class modeActivity extends AppCompatActivity {

    private String remoteDeviceInfo;
    private Context context;
    private TextView btText,text_Btn;
    private Button ButtonLink;
    private String remoteMacAddress;
    private BTChatService mChatService;
    private BluetoothAdapter btAdapter;
    private String directionCmd, songCmd;
    private ConstraintLayout viewMain,viewButton,viewSensor,viewSpeech,viewRocker;
    private Button Buttonbtn,Sensorbtn,Speechbtn,Rockerbtn;
    private ImageButton buttonTOP, buttonDown, buttonLeft, buttonRight, buttonStop;
    private static final String GO_FORWARD = "f";
    private static final String GO_BACKWARD = "b";
    private static final String TURN_LEFT = "l";
    private static final String TURN_RIGHT = "r";
    private static final String CAR_STOP = "p";
    private static final String Song_1 = "1";
    private static final String Song_2 = "2";
    private static final String Song_3 = "3";
    private static final String Song_4 = "4";
    private static final String Song_OFF = "0";
    private static final String TAG = "Car";
    private int mode;
    private final int ButtonMode = 1;
    private final int SensorMode = 2;
    private final int ControlMode = 3;
    private final int SpeechMode = 4;
    private final int RockerMode = 5;
    private ImageView speechUp, speechDown, speechLeft, speechRight,speechStop;
    private SensorManager sensorManager;
    private modeActivity.MyAccListen listener;
    private ImageView imageView_up, imageView_down, imageView_left, imageView_right,imageView_stop;
    private Intent rcintent;
    private SpeechRecognizer recognizer;
    final Handler handler = new Handler();
    private TextToSpeech tts;
    private RockerView mRockerView;
    private TextView mTvShake;
    private TextView mTvAngle;
    private TextView mTvLevel;
    private TextView mTvModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        Intent intent = getIntent();
        remoteDeviceInfo = intent.getStringExtra("btData");

        context = this;

        btText = (TextView) findViewById(R.id.Text);
        btText.setText(remoteDeviceInfo);
        ButtonLink = (Button) findViewById(R.id.Link_Btn);

        ButtonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteDeviceInfo != null) {
                    remoteMacAddress = remoteDeviceInfo.substring(remoteDeviceInfo.length() - 17);
                    BluetoothDevice device = btAdapter.getRemoteDevice(remoteMacAddress);
                    mChatService.connect(device);

                } else {
                    Toast.makeText(context, "No paired BT device.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        LayoutInflater inflater =  getLayoutInflater();

        final View button = inflater.inflate(R.layout.button, null);//展開第1個子畫面視窗
        final View sensor1 = inflater.inflate(R.layout.sensor, null);//展開第2個子畫面視窗
        final View speech = inflater.inflate(R.layout.speech, null);//展開第3個子畫面視窗
        final View rocker = inflater.inflate(R.layout.rocker, null);//展開第4個子畫面視窗

        viewMain=(ConstraintLayout)findViewById(R.id.view_main);
        viewButton=(ConstraintLayout)button.findViewById(R.id.viewButton);
        viewSensor=(ConstraintLayout)sensor1.findViewById(R.id.viewSensor);
        viewSpeech=(ConstraintLayout)speech.findViewById(R.id.viewSpeech);
        viewRocker=(ConstraintLayout)rocker.findViewById(R.id.viewRocker);

        Buttonbtn=(Button)findViewById(R.id.buttonButton);
        Sensorbtn=(Button)findViewById(R.id.buttonSensor);
        Speechbtn=(Button)findViewById(R.id.buttonSpeech);
        Rockerbtn=(Button)findViewById(R.id.buttonRocker);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();

        mChatService = new BTChatService(context, mHandler);

        Buttonbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle("Button mode");
                directionCmd = CAR_STOP;
                sendCMD(directionCmd);
                if (mode==SpeechMode){
                    recognizer.stopListening();
                    recognizer.destroy();
                }else if (mode==SensorMode){
                    sensorManager.unregisterListener(listener);
                }else if (mode==RockerMode){
                    if (tts != null) {
                        tts.stop();
                    }
                }

                viewMain.removeAllViews();
                viewMain.addView(viewButton);
                Buttonbtn.getBackground().setColorFilter(0xFF7F7F7F,android.graphics.PorterDuff.Mode.MULTIPLY);
                Sensorbtn.getBackground().setColorFilter(null);
                Speechbtn.getBackground().setColorFilter(null);
                Rockerbtn.getBackground().setColorFilter(null);
                Buttonbtn.setEnabled(false);
                Sensorbtn.setEnabled(true);
                Speechbtn.setEnabled(true);
                Rockerbtn.setEnabled(true);

                buttonTOP = (ImageButton) findViewById(R.id.imageButtonTop);
                buttonDown = (ImageButton) findViewById(R.id.imageButtonDown);
                buttonLeft = (ImageButton) findViewById(R.id.imageButtonLeft);
                buttonRight = (ImageButton) findViewById(R.id.imageButtonRight);
                buttonStop = (ImageButton) findViewById(R.id.imageButtonStop);
                text_Btn = (TextView) findViewById(R.id.textView_Btn);

                buttonTOP.setOnClickListener(new ButtonClick());
                buttonDown.setOnClickListener(new ButtonClick());
                buttonLeft.setOnClickListener(new ButtonClick());
                buttonRight.setOnClickListener(new ButtonClick());
                buttonStop.setOnClickListener(new ButtonClick());
                text_Btn.setText("");
            }
        });

        Buttonbtn.callOnClick();

        Sensorbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directionCmd = CAR_STOP;
                sendCMD(directionCmd);
                if (mode==SpeechMode){
                    recognizer.stopListening();
                    recognizer.destroy();
                }else if (mode==RockerMode){
                    if (tts != null) {
                        tts.stop();
                    }
                }

                viewMain.removeAllViews();
                viewMain.addView(viewSensor);
                Buttonbtn.getBackground().setColorFilter(null);
                Sensorbtn.getBackground().setColorFilter(0xFF7F7F7F,android.graphics.PorterDuff.Mode.MULTIPLY);
                Speechbtn.getBackground().setColorFilter(null);
                Rockerbtn.getBackground().setColorFilter(null);
                Buttonbtn.setEnabled(true);
                Sensorbtn.setEnabled(false);
                Speechbtn.setEnabled(true);
                Rockerbtn.setEnabled(true);

                mode = SensorMode;

                //speech mode
                setTitle("Speech mode");

                //sensor mode
                setTitle("Sensor mode");

                imageView_up = (ImageView) findViewById(R.id.imageViewTop);
                imageView_down = (ImageView) findViewById(R.id.imageViewDown);
                imageView_left = (ImageView) findViewById(R.id.imageViewLeft);
                imageView_right = (ImageView) findViewById(R.id.imageViewRight);
                imageView_stop = (ImageView) findViewById(R.id.imageViewStop);

                imageView_up.setVisibility(View.INVISIBLE);     //讓imageView不要顯示
                imageView_down.setVisibility(View.INVISIBLE);
                imageView_left.setVisibility(View.INVISIBLE);
                imageView_right.setVisibility(View.INVISIBLE);
                imageView_stop.setVisibility(View.INVISIBLE);

                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                listener = new MyAccListen();
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);

            }
        });

        Speechbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directionCmd = CAR_STOP;
                sendCMD(directionCmd);
                if (mode==SensorMode){
                    sensorManager.unregisterListener(listener);
                }else if (mode==RockerMode){
                    if (tts != null) {
                        tts.stop();
                    }
                }
                viewMain.removeAllViews();
                viewMain.addView(viewSpeech);
                Buttonbtn.getBackground().setColorFilter(null);
                Sensorbtn.getBackground().setColorFilter(null);
                Speechbtn.getBackground().setColorFilter(0xFF7F7F7F,android.graphics.PorterDuff.Mode.MULTIPLY);
                Rockerbtn.getBackground().setColorFilter(null);
                Buttonbtn.setEnabled(true);
                Sensorbtn.setEnabled(true);
                Speechbtn.setEnabled(false);
                Rockerbtn.setEnabled(true);

                mode = SpeechMode;

                //speech mode
                setTitle("Speech mode");

                recognizer = SpeechRecognizer.createSpeechRecognizer(context);
                recognizer.setRecognitionListener(new MyRecognizerListener());
                rcintent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                rcintent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                recognizer.startListening(rcintent);

                speechDown = (ImageView) findViewById(R.id.SpeechDown);
                speechUp = (ImageView) findViewById(R.id.SpeechTop);
                speechLeft = (ImageView) findViewById(R.id.SpeechLeft);
                speechRight = (ImageView) findViewById(R.id.SpeechRight);
                speechStop = (ImageView) findViewById(R.id.SpeechStop);

                speechDown.setVisibility(View.INVISIBLE);     //讓imageView不要顯示
                speechUp.setVisibility(View.INVISIBLE);
                speechLeft.setVisibility(View.INVISIBLE);
                speechRight.setVisibility(View.INVISIBLE);
                speechStop.setVisibility(View.INVISIBLE);
            }
        });

        Rockerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directionCmd = CAR_STOP;
                sendCMD(directionCmd);
                if (mode==SpeechMode){
                    recognizer.stopListening();
                    recognizer.destroy();
                }else if (mode==SensorMode){
                    sensorManager.unregisterListener(listener);
                }
                viewMain.removeAllViews();
                viewMain.addView(viewRocker);
                Buttonbtn.getBackground().setColorFilter(null);
                Sensorbtn.getBackground().setColorFilter(null);
                Speechbtn.getBackground().setColorFilter(null);
                Rockerbtn.getBackground().setColorFilter(0xFF7F7F7F,android.graphics.PorterDuff.Mode.MULTIPLY);
                Buttonbtn.setEnabled(true);
                Sensorbtn.setEnabled(true);
                Speechbtn.setEnabled(true);
                Rockerbtn.setEnabled(false);

                mode = RockerMode;

                mRockerView = (RockerView) findViewById(R.id.my_rocker);
                mTvAngle = (TextView) findViewById(R.id.tv_now_angle);
                mTvLevel = (TextView) findViewById(R.id.tv_now_level);
                mTvModel = (TextView) findViewById(R.id.tv_now_model);
                mTvShake = (TextView) findViewById(R.id.tv_now_shake);

                createLanguageTTS();
                setTitle("Joystick mode");

                //mTvModel.setText("當前模式：方向有改變時回調；4個方向");
                mRockerView.setOnShakeListener(DIRECTION_4_ROTATE_45, new RockerView.OnShakeListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void direction(RockerView.Direction direction) {
                        tts.setLanguage(Locale.CHINESE);
                        if (direction == RockerView.Direction.DIRECTION_CENTER){
                            mTvShake.setText("停止");
                            tts.speak(mTvShake.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);     //念出Text內容
                            directionCmd = CAR_STOP;
                            sendCMD(directionCmd);
                        }else if (direction == RockerView.Direction.DIRECTION_DOWN){
                            mTvShake.setText("後退");
                            tts.speak(mTvShake.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);     //念出Text內容
                            directionCmd = GO_BACKWARD;
                            sendCMD(directionCmd);
                        }else if (direction == RockerView.Direction.DIRECTION_LEFT){
                            mTvShake.setText("左轉");
                            tts.speak(mTvShake.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);     //念出Text內容
                            directionCmd = TURN_LEFT;
                            sendCMD(directionCmd);
                        }else if (direction == RockerView.Direction.DIRECTION_UP){
                            mTvShake.setText("前進");
                            tts.speak(mTvShake.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);     //念出Text內容
                            directionCmd = GO_FORWARD;
                            sendCMD(directionCmd);
                        }else if (direction == RockerView.Direction.DIRECTION_RIGHT){
                            mTvShake.setText("右轉");
                            tts.speak(mTvShake.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);     //念出Text內容
                            directionCmd = TURN_RIGHT;
                            sendCMD(directionCmd);
                        }
                    }

                    @Override
                    public void onFinish() {
                    }
                });
                mRockerView.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void angle(double angle) {
                        //mTvAngle.setText("當前角度："+angle);
                    }

                    @Override
                    public void onFinish() {

                    }
                });

                mRockerView.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
                    @Override
                    public void onDistanceLevel(int level) {
                        //mTvLevel.setText("當前距離級別："+level);
                    }
                });


            }
        });

    }

    private class ButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageButtonTop:
//                    Toast.makeText(context, "Go forward.", Toast.LENGTH_SHORT).show();
                    directionCmd = GO_FORWARD;
                    sendCMD(directionCmd);
                    text_Btn.setText("前進");
                    break;

                case R.id.imageButtonDown:
//                    Toast.makeText(context, "Go backward.", Toast.LENGTH_SHORT).show();
                    directionCmd = GO_BACKWARD;
                    sendCMD(directionCmd);
                    text_Btn.setText("後退");
                    break;

                case R.id.imageButtonLeft:
//                    Toast.makeText(context, "Turn left.", Toast.LENGTH_SHORT).show();
                    directionCmd = TURN_LEFT;
                    sendCMD(directionCmd);
                    text_Btn.setText("左轉");
                    break;

                case R.id.imageButtonRight:
//                    Toast.makeText(context, "Turn right.", Toast.LENGTH_SHORT).show();
                    directionCmd = TURN_RIGHT;
                    sendCMD(directionCmd);
                    text_Btn.setText("右轉");
                    break;

                case R.id.imageButtonStop:
//                    Toast.makeText(context, "Car stop.", Toast.LENGTH_SHORT).show();
                    directionCmd = CAR_STOP;
                    sendCMD(directionCmd);
                    text_Btn.setText("停止");
                    break;
            }
        }
    }


    // Sends a Command to remote BT device.
    private void sendCMD(String message) {
        // Check that we're actually connected before trying anything
        int mState = mChatService.getState();
        Log.d(TAG, "btstate in sendMessage =" + mState);
        if (mState != BTChatService.STATE_CONNECTED) {
            Log.d(TAG, "btstate =" + mState);
            // Toast.makeText(context, "Bluetooth device is not connected. ", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mChatService.BTWrite(send);
            }
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    //There is no message queue leak problem
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDevice = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(context, "Connected to " + mConnectedDevice, Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_TOAST:
                    Toast.makeText(context, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_ServerMode:
                    // Toast.makeText(context,"Enter Server accept state.",Toast.LENGTH_SHORT).show();   //display on TextView
                    break;

                case Constants.MESSAGE_ClientMode:
                    //  Toast.makeText(context,"Enter Client connect state.",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.car_exit:
                finish();
                break;

            case R.id.song1:
                songCmd = Song_1;
                sendCMD(songCmd);
                break;

            case R.id.song2:
                songCmd = Song_2;
                sendCMD(songCmd);
                break;

            case R.id.song3:
                songCmd = Song_3;
                sendCMD(songCmd);
                break;

            case R.id.song4:
                songCmd = Song_4;
                sendCMD(songCmd);
                break;

            case R.id.song0:
                songCmd = Song_OFF;
                sendCMD(songCmd);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyAccListen implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            float X_value = event.values[0];
            float Y_value = event.values[1];
            float Z_value = event.values[2];

            if (X_value < -3.0) {
                imageView_up.setVisibility(View.INVISIBLE);
                imageView_down.setVisibility(View.INVISIBLE);
                imageView_left.setVisibility(View.INVISIBLE);
                imageView_right.setVisibility(View.VISIBLE);
                imageView_stop.setVisibility(View.INVISIBLE);
                directionCmd = TURN_RIGHT;
                sendCMD(directionCmd);
            } else if (X_value > 3.0) {
                imageView_up.setVisibility(View.INVISIBLE);
                imageView_down.setVisibility(View.INVISIBLE);
                imageView_left.setVisibility(View.VISIBLE);
                imageView_right.setVisibility(View.INVISIBLE);
                imageView_stop.setVisibility(View.INVISIBLE);
                directionCmd = TURN_LEFT;
                sendCMD(directionCmd);
            } else {
                if (Y_value < -3.0) {
                    imageView_up.setVisibility(View.VISIBLE);
                    imageView_down.setVisibility(View.INVISIBLE);
                    imageView_left.setVisibility(View.INVISIBLE);
                    imageView_right.setVisibility(View.INVISIBLE);
                    imageView_stop.setVisibility(View.INVISIBLE);
                    directionCmd = GO_FORWARD;
                    sendCMD(directionCmd);
                } else if (Y_value > 3.0) {
                    imageView_up.setVisibility(View.INVISIBLE);
                    imageView_down.setVisibility(View.VISIBLE);
                    imageView_left.setVisibility(View.INVISIBLE);
                    imageView_right.setVisibility(View.INVISIBLE);
                    imageView_stop.setVisibility(View.INVISIBLE);
                    directionCmd = GO_BACKWARD;
                    sendCMD(directionCmd);
                } else {
                    imageView_up.setVisibility(View.INVISIBLE);
                    imageView_down.setVisibility(View.INVISIBLE);
                    imageView_left.setVisibility(View.INVISIBLE);
                    imageView_right.setVisibility(View.INVISIBLE);
                    imageView_stop.setVisibility(View.VISIBLE);
                    directionCmd = CAR_STOP;
                    sendCMD(directionCmd);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class MyRecognizerListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
            //用handler製造delay效果
            //在上面宣告final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    //結束至onResults的時間差不多0.5秒左右，所以需延遲0.5秒以上再執行recognizer.startListening(intent);才可執行
                    //參考資料http://rach-chen.logdown.com/posts/829008-android-endless-conversations-listening-in-silence-he
                    Log.d("RECOGNIZER", "done");
                    recognizer.startListening(rcintent);  //使語音辨識不斷執行
                }
            }, 1000);  //延遲1秒
        }

        @Override
        public void onError(int error) {
            Log.d("RECOGNIZER", "Error Code: " + error);
        }

        @Override
        public void onResults(Bundle results) {
            List resList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            StringBuffer sb = new StringBuffer();
            for (Object res : resList) {
                sb.append(res + "\n");
            }
            boolean status1 = sb.toString().contains("前");
            if (status1) {
                directionCmd = GO_FORWARD;
                sendCMD(directionCmd);
                speechDown.setVisibility(View.INVISIBLE);
                speechUp.setVisibility(View.VISIBLE);
                speechLeft.setVisibility(View.INVISIBLE);
                speechRight.setVisibility(View.INVISIBLE);
                speechStop.setVisibility(View.INVISIBLE);
            }
            boolean status2 = sb.toString().contains("後");
            if (status2) {
                directionCmd = GO_BACKWARD;
                sendCMD(directionCmd);
                speechDown.setVisibility(View.VISIBLE);
                speechUp.setVisibility(View.INVISIBLE);
                speechLeft.setVisibility(View.INVISIBLE);
                speechRight.setVisibility(View.INVISIBLE);
                speechStop.setVisibility(View.INVISIBLE);
            }
            boolean status3 = sb.toString().contains("左");
            if (status3) {
                directionCmd = TURN_LEFT;
                sendCMD(directionCmd);
                speechDown.setVisibility(View.INVISIBLE);
                speechUp.setVisibility(View.INVISIBLE);
                speechLeft.setVisibility(View.VISIBLE);
                speechRight.setVisibility(View.INVISIBLE);
                speechStop.setVisibility(View.INVISIBLE);
            }
            boolean status4 = sb.toString().contains("右");
            if (status4) {
                directionCmd = TURN_RIGHT;
                sendCMD(directionCmd);
                speechDown.setVisibility(View.INVISIBLE);
                speechUp.setVisibility(View.INVISIBLE);
                speechLeft.setVisibility(View.INVISIBLE);
                speechRight.setVisibility(View.VISIBLE);
                speechStop.setVisibility(View.INVISIBLE);
            }
            boolean status5 = sb.toString().contains("停");
            if (status5) {
                directionCmd = CAR_STOP;
                sendCMD(directionCmd);
                speechDown.setVisibility(View.INVISIBLE);
                speechUp.setVisibility(View.INVISIBLE);
                speechLeft.setVisibility(View.INVISIBLE);
                speechRight.setVisibility(View.INVISIBLE);
                speechStop.setVisibility(View.VISIBLE);
            }
            Log.d("RECOGNIZER", "onResults: " + sb.toString());
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }

    private void createLanguageTTS() {

        if (tts == null) {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int arg0) {
                    // TTS 初始化成功
                    if (arg0 == TextToSpeech.SUCCESS) {
                        // 目前指定的【語系+國家】TTS, 已下載離線語音檔, 可以離線發音
                        if (tts.isLanguageAvailable(Locale.CHINESE) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                            tts.setLanguage(Locale.CHINESE);    //中文
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        directionCmd = CAR_STOP;
        sendCMD(directionCmd);

        if (mode==SpeechMode){
            recognizer.stopListening();
            recognizer.destroy();
        }else if (mode== SensorMode){
            sensorManager.unregisterListener(listener);
        }else if (mode==RockerMode){
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
        }
        if (mChatService!=null) {
            mChatService.stop();
            mChatService = null;
        }
        super.onDestroy();
    }

}
