package com.example.android.cardgame;

import android.app.Activity;
import android.content.Intent;
import android.example.com.Login.LoginActivity;
import android.example.com.tflitecamerademo.MainActivity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.android.tflitecamerademo.R;

public class CardgameWrongAnswer extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.activity_cardgame_wrong_answer);


        Handler handler = new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);

                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0,1000);
    }
}
