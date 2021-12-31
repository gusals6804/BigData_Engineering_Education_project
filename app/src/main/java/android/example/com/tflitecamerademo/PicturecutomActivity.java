package android.example.com.tflitecamerademo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.tflitecamerademo.R;

import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class PicturecutomActivity extends Activity {

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_picturecutom);

        Button btnClose =  findViewById(R.id.btnClose);
        Button btnListen = findViewById(R.id.btnListen);
        ImageView imageViewResult = findViewById(R.id.imageViewResult);
        TextView textViewResult = findViewById(R.id.textViewResult);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        Bitmap picture = intent.getParcelableExtra("picture");

        imageViewResult.setImageBitmap(picture);
        textViewResult.setText(name);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR){
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.7f);
                tts.speak(name, TextToSpeech.QUEUE_FLUSH,null);
            }
        });

    }
}
