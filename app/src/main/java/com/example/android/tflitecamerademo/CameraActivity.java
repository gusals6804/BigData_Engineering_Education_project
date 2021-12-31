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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/** Main {@code Activity} class for the Camera app. */
public class CameraActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);

    Intent intent = getIntent();
    String userid = intent.getStringExtra("userID");


    /** saveInstanceState == null 인 경우에는 Fragment 또는 Activity가 처음 실행되면 null 값이 주어진다. */
    if (null == savedInstanceState) {
      /** Fragment로 디자인을 하기 위해서 Camera2BasicFragment.newInstance() 로 Fragment 를 넘기기로 한다는 뜻이다. */
      getFragmentManager()
          .beginTransaction()
          .replace(R.id.container, Camera2BasicFragment.newInstance(userid))
          .commit();

    }


  }
}
