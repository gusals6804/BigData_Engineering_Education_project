package com.example.android.cardgame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.example.com.tflitecamerademo.loadMysql;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.tflitecamerademo.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static android.speech.tts.TextToSpeech.ERROR;

public class CardGameActivity extends AppCompatActivity implements View.OnClickListener{

    static ImageView iv_image;
    static TextView tv_name,tv_tts;
    static TextView tv_userid;
    Button btnTalk, btnCancel, btnListen;
    static public int list_cnt;
    static public int list_knt;
    static public int count;
    TextToSpeech tts;

    public Intent i;
    SpeechRecognizer mRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_game);

        iv_image = (ImageView) findViewById(R.id.iv_image);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_tts = (TextView) findViewById(R.id.tv_tts);
        btnTalk = (Button) findViewById(R.id.btnTalk);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnListen = (Button) findViewById(R.id.btnListen);
        tv_userid = (TextView) findViewById(R.id.tv_userid);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        tv_userid.setText(userID);

        loadMysql getting = new loadMysql();
        loadMysql.active = true;
        getting.start();

        /*Drawable drawable = getResources().getDrawable(R.drawable.a);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        iv_image.setImageBitmap(bitmap);*/
        /*tv_name.setText("cat");*/

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        //음성 인식언어 설정   kr-KR 이니까 한국어
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR){
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        btnCancel.setOnClickListener(this);
        btnTalk.setOnClickListener(this);
        btnListen.setOnClickListener(this);



    }

    static public void load_image(String result) {

        if (result.contains("false")) {
            //Toast.makeText(mContext, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Bitmap[] getBlob;
            String[] getNo;
            String[] getID;
            String[] aa;
            try {
                JSONArray jArray = new JSONArray(result);

                list_cnt = jArray.length();

                Log.e("array_count try", list_cnt + "");
                getNo = new String[list_cnt];
                getBlob = new Bitmap[list_cnt];
                getID = new String[list_cnt];
                aa = new String[list_cnt];
                count = 0;

                for (int i = 0; i < list_cnt; i++) {
                    JSONObject jsonObject = jArray.getJSONObject(i);
                    aa[i] = jsonObject.getString("userID");
                    if (aa[i].equals(tv_userid.getText().toString())) {
                        getNo[count] = jsonObject.getString("name");
                        getBlob[count] = StringToBitMap(jsonObject.getString("data"));
                        getID[count] = jsonObject.getString("userID");
                        Log.e("load_image", jsonObject.get("data").toString());
                        count++;
                    }
                }

                list_knt = count;
                Random rdn = new Random();
                int count1 = rdn.nextInt(getNo.length);
                tv_name.setText(getNo[count1]);
                iv_image.setImageBitmap(getBlob[count1]);

                //list.notifyDataSetChanged();

            } catch (Exception e) {

                String temp = e.toString();

                while (temp.length() > 0) {
                    if (temp.length() > 4000) {
                        Log.e("imageLog", temp.substring(0, 4000));
                        temp = temp.substring(4000);
                    } else {
                        Log.e("imageLog", temp);
                        break;
                    }
                }
            }
        }
        //pd.cancel();
    }

    public static Bitmap StringToBitMap(String image) {
        Log.e("StringToBitMap", "StringToBitMap");
        try {
            byte[] encodeByte = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            Log.e("StringToBitMap", "good");
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTalk :
                mRecognizer.startListening(i);
                break;
            case R.id.btnListen :
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.7f);
                tts.speak(tv_name.getText().toString(), TextToSpeech.QUEUE_FLUSH,null);
                break;
            case R.id.btnCancel :
                finish();
                break;
        }
    }
    // 음성 인식
    private RecognitionListener listener;
    {
        listener = new RecognitionListener() {

            @Override
            public void onRmsChanged(float rmsdB) {
                // TODO Auto-generated method stub
            }

            //음성인식이 성공했을때 결과를 이용하는 함수
            //지금은 음성인식 결과가 HelloWorld!!부분에 들어가게 코딩됨
            @Override
            public void onResults(Bundle results) {
                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResult = results.getStringArrayList(key);
                String[] rs = new String[mResult.size()];
                mResult.toArray(rs);

                tv_tts.setText(rs[0]);

                if(tv_tts.getText().toString().equals(tv_name.getText().toString())){
                    Intent intent = new Intent(CardGameActivity.this,CardgameAnswer.class);
                    tv_tts.setText("");
                    startActivity(intent);
                    Handler handler = new Handler(){
                        public void handleMessage(Message msg){
                            super.handleMessage(msg);
                            recreate();
                        }
                    };
                    handler.sendEmptyMessageDelayed(0,1000);

                }
                else{
                    Intent intent1 = new Intent(CardGameActivity.this, CardgameWrongAnswer.class);
                    tv_tts.setText("");
                    startActivity(intent1);
                    Handler handler = new Handler(){
                        public void handleMessage(Message msg){
                            super.handleMessage(msg);
                            recreate();
                        }
                    };
                    handler.sendEmptyMessageDelayed(0,1000);

                }

            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // TODO Auto-generated method stub

            }

            //음성인식에 오류가 발생했을 때
            @Override
            public void onError(int error) {
                if (error == mRecognizer.ERROR_NETWORK_TIMEOUT) {
                    Toast.makeText(getApplicationContext(), "네트워크 타임아웃 에러", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_NETWORK) {
                    Toast.makeText(getApplicationContext(), "네트워크 에러", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_AUDIO) {
                    Toast.makeText(getApplicationContext(), "녹음 에러", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_SERVER) {
                    Toast.makeText(getApplicationContext(), "서버 에러", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_CLIENT) {
                    Toast.makeText(getApplicationContext(), "클라이언트 에러", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_SPEECH_TIMEOUT) {
                    Toast.makeText(getApplicationContext(), "아무 음성도 듣지 못함", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_NO_MATCH) {
                    Toast.makeText(getApplicationContext(), "적당한 결과를 찾지 못함", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Toast.makeText(getApplicationContext(), "인스턴스가 바쁨", Toast.LENGTH_SHORT).show();
                }

                // TODO Auto-generated method stub

            }

            @Override
            public void onEndOfSpeech() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onBeginningOfSpeech() {
                // TODO Auto-generated method stub

            }
        };
    }
}
