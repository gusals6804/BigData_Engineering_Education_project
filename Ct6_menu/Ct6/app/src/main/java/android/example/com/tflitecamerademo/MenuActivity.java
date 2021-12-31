package android.example.com.tflitecamerademo;

import android.content.Intent;
import android.example.com.Login.userInfoActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.cardgame.CardGameActivity;
import com.example.android.puzzlegame.ImageActivity;
import com.example.android.tflitecamerademo.Camera2BasicFragment;
import com.example.android.tflitecamerademo.CameraActivity;
import com.example.android.tflitecamerademo.R;
import com.flatfisher.dialogflowchatbotsample.ChatbotActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class MenuActivity extends AppCompatActivity
                implements View.OnClickListener {

    static TextView tv_userid;
    Button btnCamera, btnChatbot, btnAlphabetPuzzle, btnEnglishPuzzle, btnCardGame;
    static String userid;
    static public int list_cnt;
    static public int list_knt;
    static public int count;
    static Bitmap[] getBlob;
    static String[] getNo;
    static String[] getID;
    static String[] aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnCamera = (Button)findViewById(R.id.btnCamera);
        btnChatbot = (Button)findViewById(R.id.btnChatbot);
        btnAlphabetPuzzle = (Button)findViewById(R.id.btnAlphabetPuzzle);
        btnEnglishPuzzle = (Button)findViewById(R.id.btnEnglishPuzzle);
        btnCardGame = (Button)findViewById(R.id.btnCardGame);
        tv_userid = (TextView) findViewById(R.id.tv_userid);

        loadMysql getting = new loadMysql();
        loadMysql.active = true;
        getting.start();

        Intent intent = getIntent();
        userid = intent.getStringExtra("userID");

        //tv_userid.setText(userid);


        btnCamera.setOnClickListener(this);
        btnChatbot.setOnClickListener(this);
        btnEnglishPuzzle.setOnClickListener(this);
        btnCardGame.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCamera :
                Intent intent = new Intent(MenuActivity.this, CameraActivity.class);
                intent.putExtra("userID",userid);
                startActivity(intent);
                break;
            case R.id.btnChatbot :
                Intent intent1 = new Intent(MenuActivity.this, ChatbotActivity.class);
                intent1.putExtra("userID",userid);
                startActivity(intent1);
                break;
            case R.id.btnAlphabetPuzzle :
                break;
            case R.id.btnEnglishPuzzle :
                Intent intent3 = new Intent(MenuActivity.this, ImageActivity.class);
                intent3.putExtra("userID",userid);
                startActivity(intent3);
                break;
            case R.id.btnCardGame :
                Intent intent4 = new Intent(MenuActivity.this, CardGameActivity.class);
                /*intent4.putExtra("userName",getNo);
                intent4.putExtra("userIamge",getBlob);*/
                intent4.putExtra("userID",userid);
                startActivity(intent4);
                break;
            case R.id.btnUserInfo :
                Intent intent5 = new Intent(MenuActivity.this, userInfoActivity.class);
                intent5.putExtra("userID",userid);
                startActivity(intent5);
                break;
            case R.id.btnLogout :
                finish();
                break;

        }
    }
}
