package android.example.com.Login;

import android.app.Activity;
import android.content.Intent;
import android.example.com.tflitecamerademo.MenuActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.tflitecamerademo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ManagerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        Button uploadBt  = (Button)findViewById(R.id.uploadbt);
        uploadBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent uploadIntent = new Intent(ManagerActivity.this, MenuActivity.class);
                ManagerActivity.this.startActivity(uploadIntent);
            }
        });

        TextView idText = (TextView) findViewById(R.id.idText);
        TextView passwordText = (TextView) findViewById(R.id.passwordText);
        TextView welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        Button managementbutton = (Button) findViewById(R.id.managemenButton);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userPassword = intent.getStringExtra("userPassword");
        String message = "환영합니다." + userID + "님!";

        idText.setText(userID);
        passwordText.setText(userPassword);
        welcomeMessage.setText(message);
        if (userID.equals("hihit22") && userPassword.equals("hihit33")) {
            idText.setText("안녕 난 관리자란다!");
            welcomeMessage.setText("난 관리자!");
            passwordText.setText("응 관리자 비번~");
        } else {
            idText.setText(userID);
            passwordText.setText(userPassword);
            welcomeMessage.setText(message);
        }
        if (!userID.equals("hihit22")) {
            managementbutton.setVisibility(View.GONE);
        }
        managementbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new BackgroundTask().execute();
            }
        });
    }
    class BackgroundTask extends AsyncTask<Void,Void,String>
    {
        String target;

        @Override
        protected void onPreExecute() {
            target = "http://hihit22.cafe24.com/List.php";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder stringBuilder= new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(temp+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return  stringBuilder.toString().trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public void onProgressUpdate(Void...valuser){
            super.onProgressUpdate(valuser);
        }
        @Override
        public void onPostExecute(String result){
            Intent intent = new Intent(ManagerActivity.this , ManagementActivity.class);
            intent.putExtra("userList",result);
            ManagerActivity.this.startActivity(intent);
        }
    }
}
