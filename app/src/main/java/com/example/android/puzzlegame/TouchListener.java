package com.example.android.puzzlegame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.android.tflitecamerademo.R;

//주변의 이미지 조각 드래그

//추가 = 제자리 고정& 가까울경우 canmove
public class TouchListener implements View.OnTouchListener {

    //OnTouchListener : 화면을 터치 했을때 발생하는 이벤트
    private float xDelta;
    private float yDelta;
    private ImageActivity activity;

    public TouchListener(ImageActivity activity){
        this.activity = activity;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //터치 이벤트가 뷰에 전달되었을때 좌표줌
        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();
        //----추가
        final double tolerance = Math.sqrt(Math.pow(view.getWidth(), 2) + Math.pow(view.getHeight(), 2)) / 10;

        AlphabetActivity piece = (AlphabetActivity) view;
        if (!piece.canMove) {
            return true;
        }
        //-----

        //갑자기 릴레이션 레이아웃
        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xDelta = x - lParams.leftMargin;
                yDelta = y - lParams.topMargin;
                //--추가 -마퉈진
                piece.bringToFront();
                //---
                break;
            case MotionEvent.ACTION_MOVE:
                lParams.leftMargin = (int) (x - xDelta);
                lParams.topMargin = (int) ( y  -yDelta);
                view.setLayoutParams(lParams);
                break;
            //--추가
            case MotionEvent.ACTION_UP:
                int xDiff = Math.abs(piece.xCoord - lParams.leftMargin);
                int yDiff = Math.abs(piece.yCoord - lParams.topMargin);
                if (xDiff <= tolerance && yDiff <= tolerance) {
                    lParams.leftMargin = piece.xCoord;
                    lParams.topMargin = piece.yCoord;
                    piece.setLayoutParams(lParams);
                    piece.canMove = false;
                    sendViewToBack(piece);
                    //--추가코드2 - 게임 종료
                    activity.checkGameOver();
                    //--
                }
                break;
            //---
        }

        return true;
    }
    //--추가

    public void sendViewToBack(final View child) {
        final ViewGroup parent = (ViewGroup)child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }
    //----

}
