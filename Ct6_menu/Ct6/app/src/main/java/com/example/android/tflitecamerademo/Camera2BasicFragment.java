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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.example.com.tflitecamerademo.MenuActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Basic fragments for the Camera. */
/** 카메라의 기본 조각 */
public class Camera2BasicFragment extends Fragment
    implements View.OnClickListener, FragmentCompat.OnRequestPermissionsResultCallback {
  //FragmentCompat.OnRequestPermissionsResultCallback : 이 인터페이스는 권한 요청에 대한 결과를 수신하기위한 계약입니다.

    private String mMyData;

  /** Tag for the {@link Log}. */
  /** Log 태그 */
  private static final String TAG = "TfLiteCameraDemo";

  private static final String FRAGMENT_DIALOG = "dialog";

  private static final String HANDLE_THREAD_NAME = "CameraBackground";

  private String userid;

  private static final int PERMISSIONS_REQUEST_CODE = 1;
  private static final int STATE_PREVIEW = 0;
  private static final int STATE_WAITING_LOCK = 1;
  private static final int STATE_WAITING_PRECAPTURE = 2;
  private static final int STATE_WAITING_NON_PRECAPTURE = 3;
  private static final int STATE_PICTURE_TAKEN = 4;
  ////////////////////////////////////////////////////
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

  static{
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }
  ////////////////////////////////////////////////////

  private int mState = STATE_PREVIEW;

  private final Object lock = new Object();
  private boolean runClassifier = false;
  private boolean checkedPermissions = false;
  private TextView textView, tvId;
  private ToggleButton toggle;
  private Button btnDetectObject, btnCameraCancel;
  private NumberPicker np;
  private ImageClassifier classifier;
  private int sensorOrientation;

  /** Max preview width that is guaranteed by Camera2 API */
  /** Camera2 API로 보장되는 최대 미리보기 폭 */
  private static final int MAX_PREVIEW_WIDTH = 1920;

  /** Max preview height that is guaranteed by Camera2 API */
  /** Camera2 API로 보장되는 최대 미리보기 높이 */
  private static final int MAX_PREVIEW_HEIGHT = 1080;

  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a {@link
   * TextureView}.
   * TextureView.SurfaceTextureListener 은  TextureView의 라이프 사이클 이벤트를 처리합니다.
   */
  private final TextureView.SurfaceTextureListener surfaceTextureListener =
      new TextureView.SurfaceTextureListener() {
          //TextureView.SurfaceTextureListener : 이 청취자는 텍스트 뷰에 관련 지을 수 있었던 표면 텍스처가
          // 이용 가능하게 되었을 때에 통지를 받는데 사용 할 수 있습니다.

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
          openCamera(width, height);
        }
        //onSurfaceTextureAvailable : TextureView SurfaceTexture를 사용할 준비가 되면 호출함.

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
          configureTransform(width, height);
        }
        //지정된 객체 SurfaceTexture이 파손 되려고 할때 호출된다.
        //true 가 돌려주어 졌을 경우, 이 Methods의 호출 후에 표면 텍스트 내에서 렌더링은 일어나지 않는다.
        //false가 반환되면 클라이언트는 호출되어야한다. SurfaceTexture.release()
        //대부분 응용 프로그램은 true를 반환해야댄다.

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
          return true;
        }
        //onSurfaceTextureSizeChanged : 버퍼 사이즈가 변경 되었을 때에 불려갑니다.

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {}
      };
        //onSurfaceTextureUpdated : 지정된 객체 SurfaceTexture 이 갱신되면 호출됩니다.
                                //SurfaceTexture.updateTextImage()

  /** ID of the current {@link CameraDevice}. */
  /** 현재의 CameraDevice 의 ID입니다. */
  private String cameraId;

  /** An {@link AutoFitTextureView} for camera preview. */
  /** 카메라 미리보기를 위한 AutoFitTextureView */
  private AutoFitTextureView textureView;

  /** A {@link CameraCaptureSession } for camera preview. */
  /** 카메라 미리보기를 위한 CameraCaptureSession  */
  private CameraCaptureSession captureSession;

  /** A reference to the opened {@link CameraDevice}. */
  /** 열린 CameraDevice */
  private CameraDevice cameraDevice;

  /** The {@link android.util.Size} of camera preview. */
  /** 카메라 미기보기의 android.util.Size */
  private Size previewSize;

  /** {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state. */
  /** CameraDevice.StateCallback 은 CameraDevice가 상태를 변경할 때 호출 됩니다. */
  //CameraDevice.StateCallback : 카메라 장치의 상태에 대한 업데이트를 받기 위한 콜백 객체입니다.
  private final CameraDevice.StateCallback stateCallback =
      new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice currentCameraDevice) {
          // This method is called when the camera is opened.  We start camera preview here.
          cameraOpenCloseLock.release();
          cameraDevice = currentCameraDevice;
          createCameraPreviewSession();
        }
        //onOpened : 카메라 장치가 열리면 호출되는 메서드 입니다.
        //이 시점에서 카메라 장치는 사용할 준비가 되었으면 CameraDevice.createCaptureSession(SessionConfiguration)
        //첫 번째 캡처 세션을 설정하기 위해 호출 할 수 있습니다.

        @Override
        public void onDisconnected(@NonNull CameraDevice currentCameraDevice) {
          cameraOpenCloseLock.release();
          currentCameraDevice.close();
          cameraDevice = null;
        }
        //onDisconnected : 카메라 장치를 더 이상 사용할 수 없을 때 호출 되는 메서드 입니다.
        //이 콜백은 onOpened(CameraDevice) 카메라를 열지 못했을 때 호출 될 수 있다.

        @Override
        public void onError(@NonNull CameraDevice currentCameraDevice, int error) {
          cameraOpenCloseLock.release();
          currentCameraDevice.close();
    cameraDevice = null;
    Activity activity = getActivity();
    if (null != activity) {
      activity.finish();
    }
  }
  //onError : 카메자 장치에 심각한 오류가 발생 했을때 호출 되는 메서드입니다.
  //이 콜백은 onOpened(CameraDevice) 카메라를 열지 못했을 때 호출 될 수 있습니다.
  //이는 카메라 장치나 카메라 서비스가 어떤 식으로든 실패 했음을 나타냅니다.
  //향후 이 CameraDevice의 메소드를 호출 하려고 하면 CameraAccessException, CAMERA_ERROR 이유가 throw 됩니다.

  //이 오류가 수신 된후에도 호출 완료 또는 캡처 스트림 완료 콜백이 호출 될 수 있습니다.
  //CameraDevice.close() 이런 일이 발생하면 카메라를 청소해야합니다.
  //복구에 대한 추가 시도는 오류 코드에 따라 다릅니다.
};

/** An additional thread for running tasks that shouldn't block the UI. */
/** UI를 차단해서는 안되는 작업을 실행 하기 위한 추가 스레드 입니다.*/
private HandlerThread backgroundThread;
//순차적 실행 및 메시지 큐의 통제가 필요한 많은 백그라운드 실행 사용 사례에 적용할 수 있다.

/** A {@link Handler} for running tasks in the background. */
/** 백그라운드에서 텍스트를 싱행하기 위한 Handler */
private Handler backgroundHandler;

/** An {@link ImageReader} that handles image capture. */
/** 이미지 캡처를 처리하는 ImageReader*/
private ImageReader imageReader;
//렌더링 된 이미지 데이터에 직접 응용 프로그램 액세스를 할 수 있음.
//생산속도와 동일한 비율로 이미지를 가져오거나 릴리스하지 않으면 결국 이미지 소스가 이미지를 멈추게하거나
//이미지를 드롭하여 표면에 렌더링 하려고 합니다.

/** {@link CaptureRequest.Builder} for the camera preview */
/** 카메라 미리보기를 위한 CaptureRequst.Builder*/
private CaptureRequest.Builder previewRequestBuilder;
//캡처 요청을 위한 빌더
//더 인스턴스를 얻으려면 CameraDevice.createCaptureRequest(int) 요청 필드에 정의된 템플리트 중
//하나로 초기화하는 메소드를 사용하십시오.

/** {@link CaptureRequest} generated by {@link #previewRequestBuilder} */
/** previewRequestBuilder에 의해 생성된 CaptureRequest */
private CaptureRequest previewRequest;
//카메라 장치에서 단일 이미지를 캡처하는데 필요한 설정 및 출력의 불변 패키지 캡처 하드웨어(센서, 렌즈, 플래시),
//처리 파이프 라인, 제어 알고리즘 및 출력 버퍼에 대한 구성을 포함합니다.
//또한 이 캡처를 위해 이미지 데이터를 보낼 대상 서비스 목록을 포함합니다.

/** A {@link Semaphore} to prevent the app from exiting before closing the camera. */
/** 카메라를 닫기 전에 앱이 종료되지 않도록 하는 Semaphore */
private Semaphore cameraOpenCloseLock = new Semaphore(1);

private boolean mFlashSupported;

private File mFile;

private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
        = new ImageReader.OnImageAvailableListener() {

@Override
public void onImageAvailable(ImageReader reader) {
    Log.d("6번","ImageSaver");
        backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }
        //acquireNextImage() : ImageReader 의 큐로부터 다음의 Image 를 취득합니다.

        };

/** A {@link CameraCaptureSession.CaptureCallback} that handles events related to capture. */
/** 캡처와 관련된 이벤트를 처리하는 CameraCaptureSession.CaptureCallBack */
private CameraCaptureSession.CaptureCallback captureCallback =
        new CameraCaptureSession.CaptureCallback() {
//CameraCaptureSession.CaptureCallback : CaptureRequest 카메라 장치에 제출된 진행 상황을 추가하기위한 콜백 객체입니다.
//이 콜백은 요청이 캡처 시작을 시도 할 때 및 캡처가 완료 될 때 호출됩니다.
//이미지를 캡처하는 중 오류가 발생하는 경우 완료 메소드 대신 오류 메소드가 시도됩니다.

private void process(CaptureResult result) {
        switch (mState) {
        case STATE_PREVIEW: {
        // We have nothing to do when the camera preview is working normally.
        // 카메라 미리보기가 정상적으로 작동할 때 수행 할 작업이 없습니다.
        break;
        }
        case STATE_WAITING_LOCK: {
        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
        if (afState == null) {
            Log.d("7번","captureStill");
        captureStillPicture();
        } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
        // CONTROL_AE_STATE can be null on some devices
        //CONTROL_AE_STATE 는 일부 기기에서 null 을 이르킬 수 있습니다.
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (aeState == null ||
        aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
        mState = STATE_PICTURE_TAKEN;
        Log.d("9번","9번");
        captureStillPicture();
        } else {
        runPrecaptureSequence();
        }
        }
        break;
        }
        case STATE_WAITING_PRECAPTURE: {
        // CONTROL_AE_STATE can be null on some devices
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (aeState == null ||
        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
        mState = STATE_WAITING_NON_PRECAPTURE;
        }
        break;
        }
        case STATE_WAITING_NON_PRECAPTURE: {
        // CONTROL_AE_STATE can be null on some devices
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
        mState = STATE_PICTURE_TAKEN;
        captureStillPicture();
        Log.d("8번","8번");
        }
        break;
        }
        }
        }
@Override
public void onCaptureProgressed(
@NonNull CameraCaptureSession session,
@NonNull CaptureRequest request,
@NonNull CaptureResult partialResult) {
        process(partialResult);
        }
//onCaptureProgressed : 이 메소드는 이미지 캡쳐가 부분적으로 진행될때 호출됩니다.
//이미지 캡처의 일부 결과를 사용할 수 있습니다.

@Override
public void onCaptureCompleted(
@NonNull CameraCaptureSession session,
@NonNull CaptureRequest request,
@NonNull TotalCaptureResult result) {
        process(result);
        }
        //onCaptureCompleted : 이 메소드는 이미지 캡처가 완료되고, 모든 결과 데이터를 사용할 수 있을때 호출됩니다.
        //이 콜백은 마지막 이후에 항상 실행됩니다. 즉, 완성된 결과가 나오면 부분결과가 전달되지 않습니다.
        };

/**
 * Shows a {@link Toast} on the UI thread for the classification results.
 * 분류 결과에 대한 UI 스레드에 {@link Toast} 를 표시합니다.
 *
 * @param text The message to show
 * @param text 표시하는 메세지
 */
private void showToast(String s) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str1 = new SpannableString(s);
    builder.append(str1);
    showToast(builder);
  }

  private void showToast(SpannableStringBuilder builder) {
    final Activity activity = getActivity();
    if (activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          textView.setText(builder,TextView.BufferType.SPANNABLE);
        }
      });
    }
  }
  //runOnUiThread : 특정 동작을 UI 스레드에서 동작하도록 합니다. 만약 현재 스레드가 UI 스레드이면 그 동작은 즉시 수행됩니다.
  //하지만 현재 스레드가 UI 스레드가 아니면, 필요한 동작을 UI 스레드의 이벤트 큐로 전달한다.

  /**
   * Resizes image. 이미지 크기조정
   *
   * Attempting to use too large a preview size could  exceed the camera bus' bandwidth limitation,
   * 너무 큰 미리보기 크기를 사용하면 카메라 버스의 대역폭 제한을 초과 할 수 있습니다.
   * resulting in gorgeous previews but the storage of garbage capture data.
   * 화려한 미리보기가 가능 하지만 garbage 캡처 데이터가 저장됩니다.
   *
   * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that is
   * 카메라가 지원하는 크기의 적어도 각각의 텍스처 보기 크기만큼 큰 가장 작은것을 선택하십시오.
   *
   * at least as large as the respective texture view size, and that is at most as large as the
   *  각 최대 크기만큼 크고 지정된 종횡비가 지정된 값과 일치하는 크기입니다.
   *
   * respective max size, and whose aspect ratio matches with the specified value. If such size
   * doesn't exist, choose the largest one that is at most as large as the respective max size, and
   * whose aspect ratio matches with the specified value.
   *  크기가 존재하지 않으면 가각의 최대 크기만큼 크고 지정된 값과 일치하는 종횡비를 가진 가장 큰것을 선택하십시오.
   *
   * @param choices The list of sizes that the camera supports for the intended output class
   *                카메라가 원하는 출력 클래스에 대해 지원하는 크기 목록
   * @param textureViewWidth The width of the texture view relative to sensor coordinate
   *                        센서 좌표에 상대적인 텍스처 보기의 너비
   * @param textureViewHeight The height of the texture view relative to sensor coordinate
   *                          센서 좌표에 상대적인 텍스처 보기의 높이
   * @param maxWidth The maximum width that can be chosen
   *                 선택할 수 있는 최대 너비
   * @param maxHeight The maximum height that can be chosen
   *                  선택할 수 있는 최대 높이
   * @param aspectRatio The aspect ratio
   *                    종횡비
   * @return The optimal {@code Size}, or an arbitrary one if none were big enough
   * 최적의 크기( Size ), 크기가 충분하지 않은 임의의 크기 (Size)
   */
  private static Size chooseOptimalSize(
      Size[] choices,
      int textureViewWidth,
      int textureViewHeight,
      int maxWidth,
      int maxHeight,
      Size aspectRatio) {

    // Collect the supported resolutions that are at least as big as the preview Surface
    // 적어도 미리보기 Surface만큼 큰 지원 해상도를 수집하십시오.
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    // 미리보기 표면보다 작은 지원 해상도를 수집하십시오.
    List<Size> notBigEnough = new ArrayList<>();
    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();
    for (Size option : choices) {
      if (option.getWidth() <= maxWidth
          && option.getHeight() <= maxHeight
          && option.getHeight() == option.getWidth() * h / w) {
        if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
          bigEnough.add(option);
        } else {
          notBigEnough.add(option);
        }
      }
    }

    // Pick the smallest of those big enough. If there is no one big enough, pick the
    // largest of those not big enough.
    // 큰 것 중 가장 작은 것을 선택하십시오. 아무도 큰 사람이 없다면, 큰사람이 없는 사람 중 가장 큰 사람을 선택하십시오.
    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new CompareSizesByArea());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new CompareSizesByArea());
    } else {
      Log.e(TAG, "Couldn't find any suitable preview size");
      return choices[0];
    }
  }

  public static Camera2BasicFragment newInstance(String userID) {
      Log.d("52번",userID);
      Camera2BasicFragment fragment = new Camera2BasicFragment();
      Bundle args = new Bundle();
      args.putString("userID", userID);
      fragment.setArguments(args);
      return fragment;
  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userid = getArguments().getString("userID");
            Log.d("52번",userid);
        }
    }

  /** Layout the preview and buttons. */
  /** 미리보기 및 단추 레이아웃*/
  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
  }

  /** Connect the buttons to their event handler. */
  /** 버튼을 이벤트 핸들러에 연결하십시오. */
  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    textView = (TextView) view.findViewById(R.id.text);
    toggle = (ToggleButton) view.findViewById(R.id.button);
    view.findViewById(R.id.btnDetectObject).setOnClickListener(this);
    view.findViewById(R.id.btnCameraCancel).setOnClickListener(this);

  }

  /** Load the model and labels. */
  /** 모델 및 레이블로드 */
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mFile = new File(getActivity().getExternalFilesDir(null),"pic.jpg");
    try {
      // create either a new ImageClassifierQuantizedMobileNet or
      // 새로운 ImageClssifierQuantizeMovileNet 또는 ImageClassifierFloatInception을 생성하십시오.

      classifier = new ImageClassifierQuantizedMobileNet(getActivity());
    } catch (IOException e) {
      Log.e(TAG, "Failed to initialize an image classifier.", e);
    }
    startBackgroundThread();
  }
  /**
   * onResume 호출 : 사용자가 일시중지됨 상태에서 액티비티를 재개하면, 시스템은 onResume() 메소드 호출
   *                  액티비티가 처음 생성되는 경우를 포함하여 액티비티가 전면에 표시 될 때 마다 시스템이
   *                  이 메소드를 호출한다는 것을 유의합니다.
   */
  @Override
  public void onResume() {
    super.onResume();
    startBackgroundThread();

    // When the screen is turned off and turned back on, the SurfaceTexture is already
    // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
    // a camera and start preview from here (otherwise, we wait until the surface is ready in
    // the SurfaceTextureListener).

    // 화면이 꺼지고 다시 켜지면 SurfaceTexture는 이미 사용 가능하며, "onSurfaceTextureAvailable" 은 호출되지 않습니다.
    // 이 경우 카메라를 열고 여기에서 미리보기를 시작할 수 있습니다.
    // (그렇지 않으면 Surface가 SurfaceTextureListener 에서 준비 될 때까지 기다립니다.

    if (textureView.isAvailable()) {
      openCamera(textureView.getWidth(), textureView.getHeight());
    } else {
      textureView.setSurfaceTextureListener(surfaceTextureListener);
    }
  }

  /**
   * onPause 호출 : 사용자가 액티비티를 더나 곧 정지상태로 전환 될 것임을 나타냅니다.
   *                일반적으로 다음 작업을 수행할 때 onPause() 콜백을 사용해야합니다.
   * */
  @Override
  public void onPause() {
    closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  /**
   * onDestroy 호출 :  Activity가 종료 될때에는 onDestroy 함수가 콜백된다.
   */
  @Override
  public void onDestroy() {
    classifier.close();
    super.onDestroy();
  }

  /**
   * Sets up member variables related to camera.
   * 카메라와 관련된 멤버 변수를 설정합니다.
   *
   * @param width The width of available size for camera preview
   *              카메라 미리보기에 사용 할 수 있는 크기의 너비
   * @param height The height of available size for camera preview
   *               카메라 미리보기에 사용 할 수 있는 크기의 높이
   */
  private void setUpCameraOutputs(int width, int height) {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    //CameraManager : 특성화, 연결 및 감지를 위한 시스템 서비스 관리자
    //getSystemService : 핸들을 이름으로 시스템 레벨서비스에 리턴하십시오.
    try {
      for (String cameraId : manager.getCameraIdList()) {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        // 이 샘플에서는 전면 카메라를 사용하지 않습니다.

        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        //LENS_FACING : 카메자 장치 화면을 기준으로 향하는 방향입니다.
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //SCALER_STREAM_CONFIGURATION_MAP : 이 카메라 장치가 지원하는 사용 가능한 스트림 구성,
        //또한, 각 형식 / 크기 조합에 대한 최소 프레임 지속 시간 및 정지 시간을 포함합니다.
        if (map == null) {
          continue;
        }

        // For still image captures, we use the largest available size.
        // 스틸 이미지 캡처의 경우 가장 큰 가용 크기를 사용합니다.

        Size largest =
            Collections.max(
                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
        imageReader =
            ImageReader.newInstance(
                largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/ 2);
        imageReader.setOnImageAvailableListener(mOnImageAvailableListener, backgroundHandler);

        // Find out if we need to swap dimension to get the preview size relative to sensor
        // coordinate.
        // 센서 좌표를 기분으로 미리보기 크기를 가져 오려면 치수를 바꿔야하는지 확인 하십시오.

        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        // noinspection ConstantConditions
        //
        /* Orientation of the camera sensor */
        // 카메라 센서의 방향

        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        boolean swappedDimensions = false;
        switch (displayRotation) {
          case Surface.ROTATION_0:
          case Surface.ROTATION_180:
            if (sensorOrientation == 90 || sensorOrientation == 270) {
              swappedDimensions = true;
            }
            break;
          case Surface.ROTATION_90:
          case Surface.ROTATION_270:
            if (sensorOrientation == 0 || sensorOrientation == 180) {
              swappedDimensions = true;
            }
            break;
          default:
            Log.e(TAG, "Display rotation is invalid: " + displayRotation);
        }

        Point displaySize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        if (swappedDimensions) {
          rotatedPreviewWidth = height;
          rotatedPreviewHeight = width;
          maxPreviewWidth = displaySize.y;
          maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
          maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
          maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }

        previewSize =
            chooseOptimalSize(
                map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth,
                rotatedPreviewHeight,
                maxPreviewWidth,
                maxPreviewHeight,
                largest);

        // We fit the aspect ratio of TextureView to the size of preview we picked.
        // TextureView의 종횡비를 우리가 선택한 미리보기의 크기에 맞춥니다.


        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
          textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
        } else {
          textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
        }

        Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        mFlashSupported = available == null ? false : available;

        this.cameraId = cameraId;
        return;
      }
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to access Camera", e);
    } catch (NullPointerException e) {
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      // 현재 Camera2 API가 사용되지만 이 코드가 실행되는 장치에서 지원되지 않으면 NPE가 발생합니다.

      ErrorDialog.newInstance(getString(R.string.camera_error))
          .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }
  }

  private String[] getRequiredPermissions() {
    Activity activity = getActivity();
    try {
      PackageInfo info =
          activity
              .getPackageManager()
              .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
      ///PackageInfo : 패키지의 내용에 대한 전체정보. 이것은 AndroidManifest.xml에서 수집 한 모든 정보에 해당합니다.
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  /** Opens the camera specified by {@link Camera2BasicFragment#cameraId}. */
  /** Camera2BasicFragment #cameraId 에 지정된 카메라를 엽니다. */

  private void openCamera(int width, int height) {
    if (!checkedPermissions && !allPermissionsGranted()) {
      FragmentCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
      //FragmentCompat : Fragment 이전 버전과 호환되는 기능에 액세스 할 수 잇는 도우미
      //requestPermissions : 이 응용 프로그램에 권한을 요청합니다.
      return;
    } else {
      checkedPermissions = true;
    }
    setUpCameraOutputs(width, height);
    configureTransform(width, height);
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }
      manager.openCamera(cameraId, stateCallback, backgroundHandler);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to open Camera", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (ContextCompat.checkSelfPermission(getActivity(), permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /** Closes the current {@link CameraDevice}. */
  /** 현재의 CameraDevice 를 닫습니다. */

  private void closeCamera() {
    try {
      cameraOpenCloseLock.acquire();
      if (null != captureSession) {
        captureSession.close();
        captureSession = null;
      }
      if (null != cameraDevice) {
        cameraDevice.close();
        cameraDevice = null;
      }
      if (null != imageReader) {
        imageReader.close();
        imageReader = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      cameraOpenCloseLock.release();
    }
  }

  /** Starts a background thread and its {@link Handler}. */
  /** 백그라운드 Thread와 그 핸들러를 가동한다. */
  private void startBackgroundThread() {
    backgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
    //HandlerThread : 루퍼가 있는 새 스레드를 시작하기위한 편리한 클래스입니다.
    //그러면 루퍼를 사용하여 핸들러 클래스를 만들 수 있습니다. start()는 여전히 호출되어야합니다.
    backgroundThread.start();
    backgroundHandler = new Handler(backgroundThread.getLooper());
    synchronized (lock) {
      runClassifier = true;
    }
    backgroundHandler.post(periodicClassify);
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  }

  /** Stops the background thread and its {@link Handler}. */
  /** 백그라운드 Thread와 그 핸들러를 정지시킨다. */
  private void stopBackgroundThread() {
    backgroundThread.quitSafely();
    //quitSafely : 핸들러 스레드의 루퍼를 안전하게 종료합니다.
    try {
      backgroundThread.join();
      backgroundThread = null;
      backgroundHandler = null;
      synchronized (lock) {
        runClassifier = false;
      }
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted when stopping background thread", e);
    }
  }

  /** Takes photos and classify them periodically. */
  /** 사진을 찍어 주기적으로 분류합니다. */
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private Runnable periodicClassify =
      new Runnable() {
        @Override
        public void run() {
          synchronized (lock) {
            if (runClassifier) {
              classifyFrame();

            }
          }
                                                     backgroundHandler.post(periodicClassify);
        }
      };

  /** Creates a new {@link CameraCaptureSession} for camera preview. */
  /** 카메라 미리보기를 위한 새로운 CameraCaptureSession 을 만듭니다. */
  private void createCameraPreviewSession() {
    try {
      SurfaceTexture texture = textureView.getSurfaceTexture();
      assert texture != null;

      // We configure the size of default buffer to be the size of camera preview we want.
      //기본 버퍼의 크기를 원하는 카메라 미리보기 크기로 구성합니다.
      texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
      //setDefaltBufferSize : 이미지 버퍼의 기본 크기를 설정합니다.

      // This is the output Surface we need to start preview.
      // 이것은 미리보기를 시작하는 데 필요한 표면입니다.
      Surface surface = new Surface(texture);

      // We set up a CaptureRequest.Builder with the output Surface.
      // 우리는 Surface 를 출력하는 CaptureRequestBuilder 를 설정했습니다.
      /**
       * CameraDevice.Capture
       *
       * */
      previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      previewRequestBuilder.addTarget(surface);

      // Here, we create a CameraCaptureSession for camera preview.
      // 여기서는 카메라 미리보기를 위한 CameraCaptureSession을 만듭니다.

      cameraDevice.createCaptureSession(
          Arrays.asList(surface, imageReader.getSurface()),
          new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // The camera is already closed
              //카메라가 이미 닫혔습니다.
              if (null == cameraDevice) {
                return;
              }

              // When the session is ready, we start displaying the preview.
              // 세션이 준비되면 미리보기가 표시됩니다.
              captureSession = cameraCaptureSession;
              try {
                // Auto focus should be continuous for camera preview.
                // 자동 초점은 카메라 미리보기를 위해 연속적이어야합니다.
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Finally, we start displaying the camera preview.
                // 마지막으로 카메라 미리보기를 시작합니다.

                previewRequest = previewRequestBuilder.build();
                captureSession.setRepeatingRequest(
                    previewRequest, captureCallback, backgroundHandler);
                //setRepeatingRequest : 이 캡처 세션을 통해 이미지 캡처를 끝없이 반복적으로 요청하십시오.
              } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to set up config to capture Camera", e);
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              showToast("Failed");
            }
          },
          null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Failed to preview Camera", e);
    }
  }



  /**
   * Configures the necessary {@link android.graphics.Matrix} transformation to `textureView`. This
   * method should be called after the camera preview size is determined in setUpCameraOutputs and
   * also the size of `textureView` is fixed.
   *
   * 'textureView' 에 필요한 android.graphics.Matrix 변환을 설정합니다.
   *  이 메소드는 setUpCameraOutputs 에서 카메라 미리보기 크기가 결정되고, 'textView' 의 크기가 고정 된 후에 호출해야합니다.
   *
   * @param viewWidth The width of `textureView`
   * @param viewHeight The height of `textureView`
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == textureView || null == previewSize || null == activity) {
      return;
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
    //RectF : 사각형에 대해 4개의 부동 좌표를 유지합니다. 사각형은 네 모서리의 좌표로 표시됩니다.
    //이 필드는 직접 액세스 할 수 있습니다. 사각형의 너미와 높이를 가져오려면 width()와 height()를 사용하십니오.
    //주 : 대부분의 메소드는 좌표가 올바르게 정렬되었는지 확인하지 않습니다.
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale =
          Math.max(
              (float) viewHeight / previewSize.getHeight(),
              (float) viewWidth / previewSize.getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180, centerX, centerY);
    }
    textureView.setTransform(matrix);
  }

  /**
   * Initiate a still image capture
   * 정지 이미지 캡쳐 시작.
   * */
  private void takePicture(){
      Toast.makeText(getActivity(),"잠시만 기다려주세요", Toast.LENGTH_LONG).show();
      lockFocus();
  }

  /**
   * Lock the focus as the first step for a still image capture.
   * 정지 이미지 캡처를위한 첫 번째 단계로 포커스 고정
   * */

  private void lockFocus(){
      Log.d("1번","포커스고정");
      try {
        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,CameraMetadata.CONTROL_AF_TRIGGER_START);
        mState = STATE_WAITING_LOCK;
        captureSession.capture(previewRequestBuilder.build(),captureCallback,backgroundHandler);
    }
    catch (CameraAccessException e){
      e.printStackTrace();
    }
  }

  /**
   * Run the precapture sequence for capturing a still image. This method should be called when
   * we get a response in {@link #captureCallback} from {@link #lockFocus()}.
   * 정지 영상 캡처를 위해 사전 캡처 시퀀스를 실행하십시오. 이 메서드는 {@link #captureCallback}에서 {@link #lockFocus ()}의 응답을 얻을 때 호출해야합니다.
   */
  private void runPrecaptureSequence() {
      Log.d("2번","시퀀스 실행");
      try {
      // This is how to tell the camera to trigger.
      previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
              CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
      // Tell #mCaptureCallback to wait for the precapture sequence to be set.
      mState = STATE_WAITING_PRECAPTURE;
      captureSession.capture(previewRequestBuilder.build(), captureCallback,
              backgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }
  /**
   * Capture a still picture. This method should be called when we get a response in
   * {@link #captureCallback} from both {@link #lockFocus()}.
   * 정지 영상을 캡처하십시오. 이 메서드는 {@link #captureCallback}에서 {@link #lockFocus ()}의 응답을받을 때 호출해야합니다.
   */
  private void captureStillPicture(){
      Log.d("3번","정지영상 캡쳐");
    try {
        final Activity activity = getActivity();
        if(null ==activity || null == cameraDevice){
          return;
        }
        Log.d("3.1번","3.1");
        //  This is the CaptureRequest.Builder that we use to take a picture
        // 이것은 우리가 사진 찍는 데 사용하는 CaptureRequest.Builder입니다.
        final CaptureRequest.Builder captureBuilder =
                cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());
        Log.d("3.2번","3.2");
      // Use the same AE and AF modes as the preview.
      //미리보기와 동일한 AE 및 AF 모드 사용
      captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
              CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
      setAutoFlash(captureBuilder);
        Log.d("3.3번","3.3");
      int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,getOrientation(rotation));

        Log.d("3.4번","3.4");

      CameraCaptureSession.CaptureCallback CaptureCallback
              = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            Log.d("3.7.1번","3.7.1번");

          /*showToast("Saved: " + mFile);*/
          Log.d(TAG, mFile.toString());
          unlockFocus();
        }
      };

        Log.d("3.5번","3.5");
      captureSession.stopRepeating();
        Log.d("3.6번","3.6");
      captureSession.abortCaptures();
        Log.d("3.7번","3.7");
      captureSession.capture(captureBuilder.build(), CaptureCallback, backgroundHandler);
        Log.d("3.8번","3.8");
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }

  }

  private int getOrientation(int rotation){
    return (ORIENTATIONS.get(rotation) + sensorOrientation + 270)%360;
  }


  /**
   * Unlock the focus. This method should be called when still image capture sequence is
   * finished.
   * 초점을 잠금 해제합니다. 정지 영상 캡처 시퀀스가 ​​완료되면이 메서드를 호출해야합니다
   */
  private void unlockFocus() {
      Log.d("4번","포커스 해제");
    try {
      // Reset the auto-focus trigger
      previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
              CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
      captureSession.capture(previewRequestBuilder.build(), captureCallback,
              backgroundHandler);
      // After this, the camera will go back to the normal state of preview.
      mState = STATE_PREVIEW;
      captureSession.setRepeatingRequest(previewRequest, captureCallback,
              backgroundHandler);



        Intent intent = new Intent(getActivity(),CustomActivity.class);
        s = textView.getText().toString();
        Log.d("51번",userid);
        intent.putExtra("name",s);
        intent.putExtra("userID",userid);

        startActivity(intent);

    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  String s;
  @Override
  public void onClick(View view) {
    switch (view.getId()) {
        case R.id.btnDetectObject : {
            takePicture();
            break;
        }
        case R.id.btnCameraCancel : {
            getActivity().finish();
            /*Intent intent = new Intent(getActivity(),MenuActivity.class);
            startActivity(intent);*/

            break;
        }
    }
  }



  private void setAutoFlash(CaptureRequest.Builder requestBuilder){
    if(mFlashSupported){
        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    }
  }

  //in CustomDialog cannot be applied

  /** Classifies a frame from the preview stream. */
  /** 미리보기 스트림에서 프레임을 분류합니다. */

  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private void classifyFrame() {
    if (classifier == null || getActivity() == null || cameraDevice == null) {
      /*showToast("Uninitialized Classifier or invalid context.");*/
      return;
    }
    SpannableStringBuilder textToShow = new SpannableStringBuilder();
    Bitmap bitmap = textureView.getBitmap(classifier.getImageSizeX(), classifier.getImageSizeY());
    String br = " "+bitmap;
   /* Log.d("15번",br);*/

    classifier.classifyFrame(bitmap, textToShow);
    bitmap.recycle();
    showToast(textToShow);
    /*Log.d("16번"," "+textToShow.toString());*/



  }

  /**
   * Saves a JPEG {@link Image} into the specified {@link File}.
   * JPEG {@link Image}를 지정된 {@link File}에 저장합니다.
   */


  private static class ImageSaver implements Runnable{
    private final Image mImage;
    private final File mFile;

    ImageSaver(Image image, File file){
              mImage = image;
              mFile = file;
          }
          @Override
          public void run() {
        Log.d("5번","이미지 저장");
              ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
              byte[] bytes = new byte[buffer.remaining()];
              buffer.get(bytes);
              FileOutputStream output = null;
              try {
                  output = new FileOutputStream(mFile);
                  output.write(bytes);
              } catch (IOException e) {
                  e.printStackTrace();
      } finally {
        mImage.close();
        if (null != output) {
          try {
            output.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  /** Compares two {@code Size}s based on their areas. */
  /** 크기에 근거하는 2개의 Size를 비교합니다. */
  private static class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      // 곱셈이 오버 플로우 하지 않도록 여기에 캐스트합니다.
      return Long.signum(
          (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
    }
  }


  /** Shows an error message dialog. */
  /** 오류 메시지 대호상자를 표시합니다. */
  public static class ErrorDialog extends DialogFragment {


    private static final String ARG_MESSAGE = "message";

    public static ErrorDialog newInstance(String message) {
      ErrorDialog dialog = new ErrorDialog();
      Bundle args = new Bundle();
      args.putString(ARG_MESSAGE, message);
      dialog.setArguments(args);
      return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Activity activity = getActivity();
      return new AlertDialog.Builder(activity)
          .setMessage(getArguments().getString(ARG_MESSAGE))
          .setPositiveButton(
              android.R.string.ok,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  activity.finish();
                }
              })
          .create();
    }
  }
}
