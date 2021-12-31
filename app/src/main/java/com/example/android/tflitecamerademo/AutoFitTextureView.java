/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.android.tflitecamerademo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/** A {@link TextureView} that can be adjusted to a specified aspect ratio. */
/** 지정된 종횡비로 조정할 수 있는 TextureView */
public class AutoFitTextureView extends TextureView {

  private int mRatioWidth = 0;
  private int mRatioHeight = 0;

  public AutoFitTextureView(Context context) {
    this(context, null);
  }

  public AutoFitTextureView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  /**
   * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
   * calculated from the parameters. Note that the actual sizes of parameters don't matter, that is,
   * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
   *
   * 이 뷰의 종회비를 설정합니다. 뷰의 크기는 매개 변수에서 계산 된 비율에 따라 측정됩니다.
   * 매개 변수의 실제 크기는 중요하지 않습니다. 즉, setAspectRatio (2,3) 및 setAspectRatio(4,6)를
   * 호출하면 동일한 결과가 생성됩니다.
   *
   * @param width Relative horizontal size
   * @param height Relative vertical size
   */
  public void setAspectRatio(int width, int height) {
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("Size cannot be negative.");
    }
    mRatioWidth = width;
    mRatioHeight = height;
    requestLayout();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //onMeasure
    int width = MeasureSpec.getSize(widthMeasureSpec);
    //MeasureSpec : onMeasure에서나, 외부에서 그려지기 전에 크기를 구할 경우에 MeasureSpec를 사용합니다.
    int height = MeasureSpec.getSize(heightMeasureSpec);
    if (0 == mRatioWidth || 0 == mRatioHeight) {
      setMeasuredDimension(width, height);
      //setMeasuredDimension : 이 메소드는 onMeasure 측정 된 너비와 측전 된 높이를 지정하기 위해 호출되어야합니다.
      //그렇게 하지 않으면 측정시 예외가 트리거 됩니다.
    } else {
      if (width < height * mRatioWidth / mRatioHeight) {
        setMeasuredDimension(width, height);
      } else {
        setMeasuredDimension(width , height);
      }
    }
  }
}
