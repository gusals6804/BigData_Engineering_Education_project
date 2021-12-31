package android.example.com.Login;

import android.app.Activity;
import android.example.com.tflitecamerademo.MainActivity;
import android.example.com.tflitecamerademo.MenuActivity;
import android.support.v7.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.android.tflitecamerademo.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity implements View.OnClickListener{

    private AlertDialog dialog;
    EditText idText, passwordText;
    Button loginbutton, btnClose, registerbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginbutton = (Button)findViewById(R.id.loginbutton);
        btnClose = findViewById(R.id.btnClose);
        registerbutton = findViewById(R.id.registerButton);
        registerbutton.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        loginbutton.setOnClickListener(this);

        idText = (EditText) findViewById(R.id.idText);
        passwordText = (EditText) findViewById(R.id.passwordText);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginbutton : {

                final String userID = idText.getText().toString();
                final String userPassword = passwordText.getText().toString();
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if(success) {
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                                dialog = builder.setMessage("로그인에 성공했습니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();

                                if(idText.getText().toString().equals("hihit22") && passwordText.getText().toString().equals("hihit33")){
                                    Intent intent = new Intent(LoginActivity.this, ManagerActivity.class);

                                    intent.putExtra("userID",userID);
                                    intent.putExtra("userPassword",userPassword);
                                    LoginActivity.this.startActivity(intent);
                                    finish();
                                }
                                else {
                                    Intent intent1 = new Intent(LoginActivity.this, MenuActivity.class);
                                    intent1.putExtra("userID",userID);

                                    LoginActivity.this.startActivity(intent1);
                                    finish();
                                }

                            }
                            else{
                                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this);
                                dialog = builder.setMessage("계정을 다시 확인하세요")
                                        .setNegativeButton("다시 시도", null)
                                        .create();
                                dialog.show();
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest= new LoginRequest(userID,userPassword,responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);
                break;
            }
            case R.id.registerButton : {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
                break;
            }
            case R.id.btnClose : {
                finish();
                break;
            }
        }
    }
    protected  void onStop(){
        super.onStop();
        if(dialog !=null)
        {
            dialog.dismiss();
            dialog=null;
        }
    }
}
