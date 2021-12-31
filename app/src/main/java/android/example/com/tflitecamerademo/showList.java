package android.example.com.tflitecamerademo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.tflitecamerademo.R;

public class showList extends LinearLayout {

    TextView load_no;
    TextView load_ID;
    ImageView load_image;


    public showList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public showList(Context context) {
        super(context);
        init(context);

    }


    public void setID (String data){
        load_ID.setText(data);            //번호


    }

    public void setNo (String data){
        load_no.setText(data);            //번호


    }


    public void setBlob (Bitmap data){
        //image

        load_image.setImageBitmap(data);

    }





    @SuppressLint("NewApi")
    public void init(Context context){
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.list_image,this,true);



        load_no=(TextView)findViewById(R.id.load_no);

        load_image=(ImageView) findViewById(R.id.load_image);



    }

}
