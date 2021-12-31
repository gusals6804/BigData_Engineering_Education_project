package android.example.com.tflitecamerademo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.android.cardgame.CardGameActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class loadMysql extends Thread{
    public static boolean active=false;
    Handler mHandler;
    String url=null;

    public loadMysql(){ //이미지 추가
        url="http://hihit22.cafe24.com/select.php";
        mHandler= new Handler(Looper.getMainLooper());
        Log.e("url",url);


    }
    @Override
    public void run() {
        super.run();
        if(active){

            URL url;

            StringBuilder sb = new StringBuilder();
            try {
                url = new URL("http://hihit22.cafe24.com/select.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));


                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();
                Log.d("77777",""+conn.getResponseCode());

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    sb = new StringBuilder();
                    String response;
                    while ((response = br.readLine()) != null){
                        sb.append(response);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            show(sb.toString());
        }



    }

    void show(final String result){
        mHandler.post(new Runnable(){

            @Override
            public void run() {
                CardGameActivity.load_image(result);

            }
        });

    }
}
