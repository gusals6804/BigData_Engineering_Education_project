package android.example.com.Login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.tflitecamerademo.R;


public class userInfoActivity extends AppCompatActivity {

    Button btnLogout;
    static TextView tv_userid;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        btnLogout = (Button)findViewById(R.id.btnLogout);


        tv_userid = (TextView) findViewById(R.id.tv_userid);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        tv_userid.setText(userID);



    }

}

