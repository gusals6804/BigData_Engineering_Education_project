package com.example.android.tflitecamerademo;

import android.app.Activity;
import android.content.Intent;
import android.example.com.tflitecamerademo.SendImage;
import android.example.com.tflitecamerademo.idname;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.speech.tts.TextToSpeech.ERROR;

public class CustomActivity extends Activity implements  View.OnClickListener{

    private String UploadUrl = "http://hihit22.cafe24.com/gusalsupload.php";

    Button btnCancel, btnListen;
    ImageView imageViewResult;
    TextView textViewResult, textViewKorea, tvuserid;
    TextToSpeech tts;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_custom);

        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewKorea = (TextView) findViewById(R.id.textViewKorea);
        tvuserid = (TextView) findViewById(R.id.tvuserid);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        //물체의 단어 띄우기
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String userid = intent.getStringExtra("userID");
        textViewResult.setText(name);
        tvuserid.setText(userid);
        Log.d("50번",userid);
        btnCancel.setOnClickListener(this);


        try {
            String fileName = "/Android/data/android.example.com.tflitecamerademo/files/pic.jpg";
            String path = Environment.getExternalStorageDirectory()+fileName;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 16;
            Bitmap bm = BitmapFactory.decodeFile(path, options);

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(),matrix,true);

            imageViewResult.setImageBitmap(resizedBitmap);
        }catch (Exception e){
            e.printStackTrace();
        }

        bitmap = ((BitmapDrawable) imageViewResult.getDrawable()).getBitmap();

        //음성인식
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR){
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        //단어번역
        if(textViewResult.getText().toString().length()==0){
            textViewResult.requestFocus();
            return;
        }
        NaverTranslateTask asyncTask = new NaverTranslateTask();
        String sText = textViewResult.getText().toString();
        asyncTask.execute(sText);


        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnListen = (Button)findViewById(R.id.btnListen);

        //
        btnListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.setPitch(1.0f);
                tts.setSpeechRate(0.7f);
                tts.speak(name, TextToSpeech.QUEUE_FLUSH,null);
            }
        });

    }
    //네이버 API를 이용하여 단어 번역
    public class NaverTranslateTask extends AsyncTask<String, Void, String>{
        public String resultText;

        String clientId = "X7kd9xXdViVwqe8nTNiu";
        String clientSecret = "7DbDBbpnhz";

        String sourceLang = "en";
        String targetLang = "ko";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {


            String sourceText = strings[0];

            try {
                //String text = URLEncoder.encode("만나서 반갑습니다.", "UTF-8");
                String text = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/language/translate";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source="+sourceLang+"&target="+targetLang+"&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                //System.out.println(response.toString());
                return response.toString();

            } catch (Exception e) {
                //System.out.println(e);
                Log.d("error", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(s.toString())
                    .getAsJsonObject().get("message")
                    .getAsJsonObject().get("result");

            TranslatedItem items = gson.fromJson(rootObj.toString(),TranslatedItem.class);

            textViewKorea.setText(items.getTranslatedText());
        }
        private class TranslatedItem{
            String translatedText;

            public String getTranslatedText(){
                return translatedText;
            }
        }
    }

    //사진 데이터 디비 저장

    private void idname1() {
        String userID = tvuserid.getText().toString();
        String name = textViewResult.getText().toString();
        String image = imageToString(bitmap);
        Log.d("77777",name);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
//                    boolean success = jsonResponse.getBoolean("success");
//                    if(success){
//                        finish();
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        idname uploadRequest = new idname(userID, name, image, responseListener);
        RequestQueue queue = Volley.newRequestQueue(CustomActivity.this);
        queue.add(uploadRequest);
    }

    /*private void uploadImage(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,UploadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String Response = jsonObject.getString("response");
                            Toast.makeText(CustomActivity.this,Response,Toast.LENGTH_LONG).show();
                            *//*imageView.setImageResource(0);
                            imageView.setVisibility(View.GONE);
                            NAME.setText("");
                            NAME.setVisibility(View.GONE);*//*
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("name",textViewResult.getText().toString().trim());
                params.put("image",imageToString(bitmap));
                return params;
            }
        };
        SendImage.getsInstance(CustomActivity.this).addToRequestQue(stringRequest);
        idname1();

    }*/
    private  String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);

    }

    @Override
    public void onClick(View v) {
        if(v == btnCancel){
            switch (v.getId()){
                case R.id.btnCancel :
                    //uploadImage();
                    idname1();
                    finish();
                    break;
            }

        }
    }
}
