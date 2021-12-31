package com.example.android.puzzlegame;

import android.content.Intent;
import android.example.com.tflitecamerademo.MainActivity;
import android.example.com.tflitecamerademo.loadMysql;
import android.example.com.tflitecamerademo.loadMysql2;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.tflitecamerademo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import static android.speech.tts.TextToSpeech.ERROR;

public class ImageActivity extends AppCompatActivity {

    String[] icon_word; //단어 끊어서 저장할 배열 선언
    Bitmap mergedImag;
    ArrayList<AlphabetActivity> pieces;
    TextToSpeech tts;
    static public int list_cnt;
    static public int count;
    static TextView tv_userid;
    static Bitmap[] getBlob;
    static String[] getNo;
    static String[] getID;
    static String[] aa;
    static int a;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        tv_userid = (TextView) findViewById(R.id.tv_userid);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        tv_userid.setText(userID);

        loadMysql2 getting = new loadMysql2();
        loadMysql2.active = true;
        getting.start();

        //음성인식
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR){
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });


//------------Back ---------------------------------------------------------------------------------
        Button backButton = findViewById(R.id.BackButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//------1.사과를 단어로 가져오기----------------------------------------------------------------------


        //이미지 리소스 가져와서 배열에 넣기
        String pkgName = getPackageName();
        /*String[] icon_Image = {"alphabet","egg","apple","banana","trafficlight","computer","qwerasdfzxcvbn"};*///이미지이름과 맞게 배열 적어주기/추후 메인액티비로 연결예정

        //String replaceString = icon_Image[2].replaceAll(" ",%20); //이미지 파일 이름 공백없애주기 지금은 안된다.
        Log.d("11111", "사과 사진가져옴" + 11111);

//------2.사과 사진 띄우기----------------------------------------------------------------------------

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Random r = new Random();
                a = r.nextInt(getNo.length);
                //배열로 이미지 리소스 찾기-사진 String 타입이어야 한다
                int ImageId = getResources().getIdentifier(getNo[a], "drawable", pkgName);

                //동적으로 이미지 뷰 생성 -사진!
                LinearLayout layout_image = (LinearLayout) findViewById(R.id.layout_image);//레이아웃 연결
                ImageView imageView_image = new ImageView(ImageActivity.this);//이미지뷰 연결
                imageView_image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));//이미지뷰 크기
                imageView_image.setImageBitmap(getBlob[a]);//사진 리소스 넣어주는 곳
                layout_image.addView(imageView_image);//이미지뷰(imageView_Image)를 레이아웃에(layout_image) 생성
                Log.d("11111", "사진띄움" + 2222);

//------3.사과 단어 사진 가져오기 (한개로 합쳐서)-------------------------------------------------------
                //비트맵 , charAt()

                //1. apple끊어서 [a,p,p,l,e]로 icon_word 에 저장
                for(int i=0; i<1; i++) {//문자열 apple 출력 - 한번 만들어야되서 1

                    icon_word =  new String[getNo[a].length()];//낱말을 담을 공간 생성

                    for(int j=0; j<icon_word.length; j++) {//a,p,p,l,e = 문자 출력 - 낱말 수 만큼
                        icon_word[j] = Character.toString(getNo[a].charAt(j));//낱말 수만큼 공간을 만들어야되서 charAt으로 쪼개서 다시 String에 넣어준다
                        Log.d("11111","낱말 :"+icon_word[j]);
                    }
                }

                //char확인
                for(int i =0; i<icon_word.length; i++){
                    Log.d("11111","낱말들어갔는지 확인 :"+icon_word[i]);
                }

                //2.비트맵 배열안에 단어 이미지 넣어주기
                Bitmap[] bitmap = new Bitmap[icon_word.length];//낱말이미지들을 보관할 비트맵 배열 선언
                Log.d("11111", "단어길이가 들어 갔나" + bitmap.length);

                //낱말 수 만큼 이미지 택배차 배열생성
                int[] icon_wordId = new int[icon_word.length];

                //이미지 Bitmap에 넣어주기
                for (int i = 0; i < icon_word.length; i++) {//낱말 수 만큼 길이 지정

                    //[a,p,p,l,e]를 하나하나 이미지 찾아서
                    icon_wordId[i] = getResources().getIdentifier(icon_word[i], "drawable", pkgName);

                    //들어갔는지 확인
                    Log.d("11111", "이미지배열 주소 맞나" + icon_wordId[i]);
                }

                //낱말에 해당하는 이미지 비트맵에 저장
                for(int i =0; i< icon_wordId.length; i++){
                    bitmap[i] = BitmapFactory.decodeResource(getResources(), icon_wordId[i]);
                }
                Log.d("11111", "단어 수 최종확인" + bitmap.length);

                //3.낱말이미지를 합침
                mergedImag = mergeMultiple(bitmap);//낱말 합칠때 필요한 함수 사용 - 6번 참조

                //4.낱말이미지를 보여주기 이미지뷰 사용


                final RelativeLayout layout_puzzle = findViewById(R.id.layout_puzzle);
                final ImageView alphabet_image_puzzle = findViewById(R.id.alphabet_image_puzzle);//XML 이미지뷰 연결

                alphabet_image_puzzle.setImageBitmap(mergedImag);//낱말 이미지 보여줌

//-------4.낱말사진 잘라서 추출---------------------------------------------------------------------
                alphabet_image_puzzle.post(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("11111", "Runnable에 들어옴");
                        pieces = splitImage();
                        //터치리스너 추가 코드----1-----
                        TouchListener touchListener = new TouchListener(ImageActivity.this);
                        //조각의 순서를 섞는다.
                        Collections.shuffle(pieces);
                        //----------------------------
                        for(AlphabetActivity piece : pieces){
                            Log.d("11111", "Runnable for문에 들어옴");
                            piece.setOnTouchListener(touchListener);
                            layout_puzzle.addView(piece);
                            //화면 하단에서 위치를 랜덤하게 표출

                            RelativeLayout.LayoutParams lparams = (RelativeLayout.LayoutParams) piece.getLayoutParams();
                            lparams.leftMargin = new Random().nextInt(layout_puzzle.getWidth()-piece.pieceWidth);
                            if(icon_word.length < 4){
                                lparams.topMargin = layout_puzzle.getHeight()-piece.pieceHeight;//랜덤을 넣으면 에러가 나기 때문에 분리해서 넣음
                            }else{
                                lparams.topMargin =  new Random ().nextInt(layout_puzzle.getHeight()-piece.pieceHeight);

                            }
                            piece.setLayoutParams(lparams);



                        }
                    }
                });
            }
        }, 1000);
       /* a = Integer.parseInt(tv_userNum.getText().toString());
        //배열로 이미지 리소스 찾기-사진 String 타입이어야 한다
        int ImageId = getResources().getIdentifier(getNo[a], "drawable", pkgName);

        //동적으로 이미지 뷰 생성 -사진!
        LinearLayout layout_image = (LinearLayout) findViewById(R.id.layout_image);//레이아웃 연결
        ImageView imageView_image = new ImageView(ImageActivity.this);//이미지뷰 연결
        imageView_image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));//이미지뷰 크기
        imageView_image.setImageBitmap(getBlob[a]);//사진 리소스 넣어주는 곳
        layout_image.addView(imageView_image);//이미지뷰(imageView_Image)를 레이아웃에(layout_image) 생성
        Log.d("11111", "사진띄움" + 2222);

//------3.사과 단어 사진 가져오기 (한개로 합쳐서)-------------------------------------------------------
        //비트맵 , charAt()

        //1. apple끊어서 [a,p,p,l,e]로 icon_word 에 저장
        for(int i=0; i<1; i++) {//문자열 apple 출력 - 한번 만들어야되서 1

            icon_word =  new String[getNo[a].length()];//낱말을 담을 공간 생성

            for(int j=0; j<icon_word.length; j++) {//a,p,p,l,e = 문자 출력 - 낱말 수 만큼
                icon_word[j] = Character.toString(getNo[a].charAt(j));//낱말 수만큼 공간을 만들어야되서 charAt으로 쪼개서 다시 String에 넣어준다
                Log.d("11111","낱말 :"+icon_word[j]);
            }
        }

        //char확인
        for(int i =0; i<icon_word.length; i++){
            Log.d("11111","낱말들어갔는지 확인 :"+icon_word[i]);
        }

        //2.비트맵 배열안에 단어 이미지 넣어주기
        Bitmap[] bitmap = new Bitmap[icon_word.length];//낱말이미지들을 보관할 비트맵 배열 선언
        Log.d("11111", "단어길이가 들어 갔나" + bitmap.length);

        //낱말 수 만큼 이미지 택배차 배열생성
        int[] icon_wordId = new int[icon_word.length];

        //이미지 Bitmap에 넣어주기
        for (int i = 0; i < icon_word.length; i++) {//낱말 수 만큼 길이 지정

            //[a,p,p,l,e]를 하나하나 이미지 찾아서
            icon_wordId[i] = getResources().getIdentifier(icon_word[i], "drawable", pkgName);

            //들어갔는지 확인
            Log.d("11111", "이미지배열 주소 맞나" + icon_wordId[i]);
        }

        //낱말에 해당하는 이미지 비트맵에 저장
        for(int i =0; i< icon_wordId.length; i++){
            bitmap[i] = BitmapFactory.decodeResource(getResources(), icon_wordId[i]);
        }
        Log.d("11111", "단어 수 최종확인" + bitmap.length);

        //3.낱말이미지를 합침
        mergedImag = mergeMultiple(bitmap);//낱말 합칠때 필요한 함수 사용 - 6번 참조

        //4.낱말이미지를 보여주기 이미지뷰 사용


        final RelativeLayout layout_puzzle = findViewById(R.id.layout_puzzle);
        final ImageView alphabet_image_puzzle = findViewById(R.id.alphabet_image_puzzle);//XML 이미지뷰 연결

        alphabet_image_puzzle.setImageBitmap(mergedImag);//낱말 이미지 보여줌

//-------4.낱말사진 잘라서 추출---------------------------------------------------------------------
        alphabet_image_puzzle.post(new Runnable() {
            @Override
            public void run() {

                Log.d("11111", "Runnable에 들어옴");
                pieces = splitImage();
                //터치리스너 추가 코드----1-----
                TouchListener touchListener = new TouchListener(ImageActivity.this);
                //조각의 순서를 섞는다.
                Collections.shuffle(pieces);
                //----------------------------
                for(AlphabetActivity piece : pieces){
                    Log.d("11111", "Runnable for문에 들어옴");
                    piece.setOnTouchListener(touchListener);
                    layout_puzzle.addView(piece);
                    //화면 하단에서 위치를 랜덤하게 표출

                    RelativeLayout.LayoutParams lparams = (RelativeLayout.LayoutParams) piece.getLayoutParams();
                    lparams.leftMargin = new Random().nextInt(layout_puzzle.getWidth()-piece.pieceWidth);
                    if(icon_word.length < 4){
                        lparams.topMargin = layout_puzzle.getHeight()-piece.pieceHeight;//랜덤을 넣으면 에러가 나기 때문에 분리해서 넣음
                    }else{
                        lparams.topMargin =  new Random ().nextInt(layout_puzzle.getHeight()-piece.pieceHeight);

                    }
                    piece.setLayoutParams(lparams);



                }
            }
        });*/
    }

    static public void load_image(String result) {

        if (result.contains("false")) {
            //Toast.makeText(mContext, "이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
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
        //pd.cancel();
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

    //==================단어 사진 자르기(함수 2 - 1)==========================================================
    private  ArrayList<AlphabetActivity> splitImage(){
        int piecesNumber = icon_word.length;//조각 수
        int rows ;
        if(icon_word.length < 4){ //단어갯수가 4개미만일때
            rows = 2;
        }else {
            rows = 4;
        }
        int cols = icon_word.length;//세로수
        ImageView alphabet_image_puzzle = findViewById(R.id.alphabet_image_puzzle);
        ArrayList<AlphabetActivity> pieces = new ArrayList<>(piecesNumber);


        //----- getBitmapPositionInsideImageView 가 나타남에 따라 추가된 코드----소스 이미지의 크기가 조정된 비트맵 가져오기
        int[] dimensions = getBitmapPositionInsideImageView(alphabet_image_puzzle); //dimensions = 치수
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int croppedImageWidth = scaledBitmapWidth - 3 * Math.abs(scaledBitmapLeft);
        int croppedImageHeight = scaledBitmapHeight - 3 * Math.abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mergedImag, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, Math.abs(scaledBitmapLeft), Math.abs(scaledBitmapTop), croppedImageWidth, croppedImageHeight);

        //-------------------------------------

        //조각의 높이 계산
        int pieceWidth  = croppedImageWidth / cols;//cols =세로
        int pieceHight  = croppedImageHeight / rows;//rows =가로
        Log.d("11111", "조각배열 앞" );

        //비트 맵 조각을 만들어 결과 배열에 추가 / 각 비트맵 조각을 만들어 결과 배열에 추가
        int yCoord = 0;
        for (int row = 0; row < 1; row++){
            Log.d("11111", "조각배열 for문 안" );
            int xCoord = 0;
            for (int col = 0; col < cols; col++){
                Log.d("11111", "조각배열 for문 2번째 안"+xCoord+"///"+pieceWidth );
                //-추가코드
                Bitmap pieceBitmap = Bitmap.createBitmap(croppedBitmap,xCoord,yCoord,pieceWidth,pieceHight);
                AlphabetActivity piece = new AlphabetActivity(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);

                piece.xCoord = xCoord + alphabet_image_puzzle.getLeft();
                piece.yCoord = yCoord + alphabet_image_puzzle.getTop();
                piece.pieceWidth = pieceWidth;
                piece.pieceHeight = pieceHight;
                pieces.add(piece);
                //--
                xCoord += pieceWidth;
            }
            Log.d("11111", "조각배열 for문 2번째 나옴" );
            yCoord += pieceHight;
        }

        return pieces;
    }
    //=============낱말조각 크기와 위치(함수 2 -1 ) ====================================================
    private int[] getBitmapPositionInsideImageView(ImageView imageView){
        int[] ret  = new int[4];

        if(imageView == null || imageView.getDrawable() == null){
            return ret;
        }

        //이미지 크기 가져오기
        //이미지 행렬 값을 가져와서 배열에 배치하십시오.???????????
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        //상수를 사용하여 축척 값을 추출합니다.
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        //실제 치수 계산

        final int actW = Math.round(origW*scaleX);
        final int actH = Math.round(origH*scaleY);

        ret[2] = actW;
        ret[3] = actH;

        //이미지 위치 가져오기
        //이미지가 ImageView중앙에 있다고 가정
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int)(imgViewH - actH)/actH;
        int left = (int)(imgViewW - actW)/actW;

        ret[0] = left;
        ret[1] = top;

        return  ret;
    }

    //------종료하면 돌아가기----------------------------------------------------------------------------
    public  void  checkGameOver(){
        if(isGameOver()){
            Log.d("11111", "완전끝남" );
            tts.setPitch(1.0f);
            tts.setSpeechRate(0.7f);
            tts.speak(getNo[a], TextToSpeech.QUEUE_FLUSH,null);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    recreate();
                }
            }, 1500);
        }
    }

    public  boolean isGameOver(){
        for (AlphabetActivity piece : pieces){
            if(piece.canMove){
                Log.d("11111", "끝남" );
                return  false;
            }
        }
        return true;
    }


//==================================================================================================
//========단어 사진 합치는데 필요한 (함수 1 )==========================================================

    public Bitmap mergeMultiple(Bitmap[] parts) {//이미지 보여주기


        Bitmap result = Bitmap.createBitmap(parts[0].getWidth() * icon_word.length, parts[0].getHeight() * icon_word.length, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();

        for (int y = 0; y < parts.length; y++) {
            canvas.drawBitmap(parts[y], parts[y].getWidth() * (y % icon_word.length), parts[y].getHeight() * (y / icon_word.length), paint);

        }
        return result;

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
