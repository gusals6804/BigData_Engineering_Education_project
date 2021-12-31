package com.example.android.puzzlegame;

import android.content.Context;

public class AlphabetActivity extends android.support.v7.widget.AppCompatImageView {

    public int xCoord;
    public int yCoord;
    public int pieceWidth;
    public int pieceHeight;
    public boolean canMove = true;

    public AlphabetActivity(Context context) {
        super(context);
    }
}
