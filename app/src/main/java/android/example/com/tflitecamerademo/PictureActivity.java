package android.example.com.tflitecamerademo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.tflitecamerademo.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class PictureActivity extends AppCompatActivity {

    static TextView tv_userid;
    private GridView bloblist;
    static listView list;
    static public int list_cnt;
    static public int list_knt;
    static public int count;
    static Context mContext;
    static ProgressDialog pd;
    static Bitmap[] getBlob;
    static String[] getNo;
    static String[] getID;
    static String[] aa;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        bloblist = (GridView) findViewById(R.id.bloblist);
        tv_userid = (TextView) findViewById(R.id.tv_userid);
        mContext = this;
        list = new listView(this);
        bloblist.setAdapter(list);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        tv_userid.setText(userID);

        reflash_list();

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bloblist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = getNo[position];
                Bitmap picture = getBlob[position];
                Intent intent1 = new Intent(PictureActivity.this, PicturecutomActivity.class);
                intent1.putExtra("name", name);
                intent1.putExtra("picture",picture);
                startActivity(intent1);

            }
        });
    }

    static public void load_image(String result) {

        if (result.contains("false")) {
            Toast.makeText(mContext, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
        } else {
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
                list.setNo(getNo);
                list.setBlob(getBlob);
                list.setID(getID);
                list.notifyDataSetChanged();

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
        pd.cancel();
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

    void reflash_list() {
        pd = new ProgressDialog(this);
        pd.setMessage("불러오는중입니다. 잠시만 기다리세요.");
        pd.show();
        loadMysql3 getting = new loadMysql3();
        loadMysql3.active = true;
        getting.start();

    }
}
