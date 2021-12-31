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
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import org.tensorflow.lite.Interpreter;

/**
 * Classifies images with Tensorflow Lite.
 * Tensorflow Lite로 이미지 분류
 */
public abstract class ImageClassifier {
  // Display preferences
  // 표기 환경 설정
  private static final float GOOD_PROB_THRESHOLD = 0.3f;
  private static final int SMALL_COLOR = 0xffddaa88;

  /** Tag for the {@link Log}. */
  /** Log 태그 */
  private static final String TAG = "TfLiteCameraDemo";

  /** Number of results to show in the UI. */
  /** UI에 표시 할 결과의 수 */
  private static final int RESULTS_TO_SHOW = 1;

  /** Dimensions of inputs. */
  /** input 값의 크기 */
  private static final int DIM_BATCH_SIZE = 1;

  private static final int DIM_PIXEL_SIZE = 3;

  /** Preallocated buffers for storing image data in. */
  /** 이미지 데이터를 저장하기 위해 미리 할당 된 버퍼 */
  private int[] intValues = new int[getImageSizeX() * getImageSizeY()];

  /** Options for configuring the Interpreter. */
  /** Interpreter 구성 옵션 */
  private final Interpreter.Options tfliteOptions = new Interpreter.Options();

  /** The loaded TensorFlow Lite model. */
  /** 로드된 TensorFlow Lite 모델 */
  /** MappedByteBuffer : 파일의 메모리 맵된 영역을 내용으로하는 다이렉트 바이트 버퍼 입니다. */
  private MappedByteBuffer tfliteModel;

  /** An instance of the driver class to run model inference with Tensorflow Lite. */
  /** Tensorflow Lite로 모델 추론을 실행하는 드라이버 클래스의 인스턴스 */
  protected Interpreter tflite;

  /** Labels corresponding to the output of the vision model. */
  /** 비전 모델의 출력에 해당하는 레이블 */
  private List<String> labelList;

  /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
  /** Tensorflow Lite에 입력으로 제공 될 이미지 데이터를 보유하는 ByteBuffer */
  protected ByteBuffer imgData = null;

  /** multi-stage low pass filter * */
  /** 다단 저역 통과 필터 */
  private float[][] filterLabelProbArray = null;

  private static final int FILTER_STAGES = 3;
  private static final float FILTER_FACTOR = 0.4f;

  /** PriorityQueue : 우선순위 큐 /  안의 리스트의 내용을 우선순위를 따져서 저장해준다. */
  private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
      new PriorityQueue<>(
          RESULTS_TO_SHOW,
          new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
              return (o1.getValue()).compareTo(o2.getValue());
            }
          });

  /** Initializes an {@code ImageClassifier}. */
  /** ImageClassifier를 초기화 합니다. */
  ImageClassifier(Activity activity) throws IOException {
    tfliteModel = loadModelFile(activity);
    tflite = new Interpreter(tfliteModel, tfliteOptions);
    labelList = loadLabelList(activity);
    imgData =
        ByteBuffer.allocateDirect(
            DIM_BATCH_SIZE
                * getImageSizeX()
                * getImageSizeY()
                * DIM_PIXEL_SIZE
                * getNumBytesPerChannel());
    //allocateDirect : 직접 새로운 바이트 버퍼를 할당합니다.
    //새로운 버퍼의 위치는 제로가 되어, 그 한계는 용량이 되며, 마크는 정의되지 않고, 각 요소는 제로로 초기화 됩니다.
    //그것이 가지고 있는지의 여부 backing array 는 불특정이다.

    imgData.order(ByteOrder.nativeOrder());
    //이 버퍼의 byte 순서를 취득합니다. 바이트 순서는 다중 바이트 값을 읽거나 쓸 때 이 바이트 버퍼의 View인
    //버퍼를 만들때 사용됩니다. 새로 생성된 바이트 버퍼의 순서는 항상 BIG_ENDIAN 입니다.
    //byteOrder.nativeOrder : 기본 플랫폼의 기본 바이트 순서를 검색합니다.
    //이 메소드는 성능에 민감한 Java code 가 하드웨어와 동일한 바이트 순서로 직접 버퍼를 할당 할 수 있습니다.
    //native code  라이브러리는 종종 그러한 버퍼가 사용될때 효율적입니다.

    filterLabelProbArray = new float[FILTER_STAGES][getNumLabels()];
    //filterLabelProArray : 다단 저역 통과 필터
    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
  }

  /** Classifies a frame from the preview stream. */
  /** 미리보기 스트림에서 프레임을 분류합니다. */
  void classifyFrame(Bitmap bitmap, SpannableStringBuilder builder) {
    printTopKLabels(builder);
    //결과로 UI에 표시 될 최상위 -K 라벨을 인쇄합니다

    if (tflite == null) {
      //tflite : Tensorflow Lite로 모델 추론을 실행하는 드라이버 클래스의 인스턴스
      Log.e(TAG, "Image classifier has not been initialized; Skipped.");
      builder.append(new SpannableString("Uninitialized Classifier."));
    }
    convertBitmapToByteBuffer(bitmap);
    //이미지 데이터를 ByteBuffer에 기입합니다.
    // Here's where the magic happens!!!
    // 마법의 발생 장소는 다음과 같습니다.
    long startTime = SystemClock.uptimeMillis();
    runInference();
    long endTime = SystemClock.uptimeMillis();
    Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime));

    // Smooth the results across frames.
    // 결과 프레임 전체에서 부드럽게 처리하십시오.
    applyFilter();

    // Print the results.
    // 결고 인쇄
    /*long duration = endTime - startTime;
    SpannableString span = new SpannableString(duration + " ms");
    span.setSpan(new ForegroundColorSpan(android.graphics.Color.LTGRAY), 0, span.length(), 0);
    builder.append(span);*/
  }

  /** 결과 프레임 전체에서 부드럽게 처리하는 방법 */
  void applyFilter() {
    int numLabels = getNumLabels();

    // Low pass filter `labelProbArray` into the first stage of the filter.
    // 필터의 첫 단게로 low pass filter 'labelProbArray'
    for (int j = 0; j < numLabels; ++j) {
      filterLabelProbArray[0][j] +=
          FILTER_FACTOR * (getProbability(j) - filterLabelProbArray[0][j]);
      //float FILTER_FACTOR = 0.4f
    }
    // Low pass filter each stage into the next.
    //Low Pass Filter 는 각 단계를 다음 단계로 진행합니다.
    for (int i = 1; i < FILTER_STAGES; ++i) {
      for (int j = 0; j < numLabels; ++j) {
        filterLabelProbArray[i][j] +=
            FILTER_FACTOR * (filterLabelProbArray[i - 1][j] - filterLabelProbArray[i][j]);
      }
    }

    // Copy the last stage filter output back to `labelProbArray`.
    //마지막 스테이지 필터 출력을 `labelProbArray` 에 복사하십시오.
    for (int j = 0; j < numLabels; ++j) {
      setProbability(j, filterLabelProbArray[FILTER_STAGES - 1][j]);
    }
  }

  /** Tensorflow Lite로 모델 추론을 실행을 종료하고 새로운 Interpreter 을 생성한다. */
  private void recreateInterpreter() {
    if (tflite != null) {
      tflite.close();
      /** interpreter 은 프로그램을 일부분씩 기계어로 번환하여 실행한다. */
      tflite = new Interpreter(tfliteModel, tfliteOptions);
    }
  }

  /** Tensorflow Lite의 구성옵션을 사용 시작 / 중지 할수 있고, Interpreter를 다시 만듭니다.*/
  public void setUseNNAPI(Boolean nnapi) {
    tfliteOptions.setUseNNAPI(nnapi);
    recreateInterpreter();
  }

  /** Tensorflow Lite 의 멀티 스레딩 환경에서 스레드 수를 설정합니다, 그리고 Interpreter을 다시 만듭니다.*/
  public void setNumThreads(int numThreads) {
    tfliteOptions.setNumThreads(numThreads);
    recreateInterpreter();
  }

  /** Closes tflite to release resources. */
  /** 리소스를 해제하기 위해 tflite를 닫습니다. */
  public void close() {
    tflite.close();
    tflite = null;
    tfliteModel = null;
  }

  /** Reads label list from Assets. */
  /** Assets에서 레이블 목록을 읽습니다. */

  private List<String> loadLabelList(Activity activity) throws IOException {
    List<String> labelList = new ArrayList<String>();
    //List로 배열을 선언한다.
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(activity.getAssets().open(getLabelPath())));
    //BufferReader : 문자, 배열 및 행렬을 효율적으로 읽을 수 있도록 문자를 버퍼링하여 문자 입력 스트림에서 텍스트를 넣습니다.
    String line;
    while ((line = reader.readLine()) != null) {
      labelList.add(line);
    }
    //reader.readLine() 에서 중복된 값이 없을 경우에는 labelList.add(line) 을 하여 추가하여준다.
    reader.close();
    return labelList;
  }

  /** Memory-map the model file in Assets. */
  private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
    AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
    //AssetFileDescriptor : AssetManager에 있는 항목의 파일 설명자. 이것은 파일에서 해당 항ㅁㄱ의 데이터의 오프셋과
    //길이 뿐만 아니라 데잍를 읽을 수 있는데 사용 할 수 있는 자신 만의 열린 FileDescriptor를 제공합니다.
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();

    String sr = " "+fileChannel;
    Log.d("14번",sr);

    //FileChannel : 파일 읽기, 쓰기, 매핑 및 조작을 위한 채널
    long startOffset = fileDescriptor.getStartOffset();
    //getStartOffset() : 범위 요소 내에서 부분 범위의 시작 위치를 가져옵니다.
    //Integer - Text 요소의 경우 : 범위의 시작 전의 문자 수 / 다른 요소의 경우 : -1
    long declaredLength = fileDescriptor.getDeclaredLength();
    //AssetFileDescriptor가 구축 될 때 선언 된 실제 바이트 수를 반환합니다.
    //UNKNOWN_LENGTH 길이가 선언되지 않은 경우, 의미 데이터는 파일의 끝에 읽어야합니다.ㄴ
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    //map : 이 채널의 파일의 영역을 직접 메모리에 매핑합니다.
    //map(FileChannel.MapMode mode, long position, long size)
    //여기서 READ_ONLY : 읽기 전용( 결과 버퍼를 수정하려고 시도하면 ReadOnlyBufferException 이 발생합니다.
  }

  /** Writes Image data into a {@code ByteBuffer}. */
  /** 이미지 데이터를 ByteBuffer에 기입합니다. */
  private void convertBitmapToByteBuffer(Bitmap bitmap) {
    //ByteBufer imgData = null;
    if (imgData == null) {
      return;
    }
    imgData.rewind();
    //imageData의 파일 위치를 조정한다.

    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Convert the image to floating point.
    // 이미지를 부동 소수점으로 변환
    int pixel = 0;
    long startTime = SystemClock.uptimeMillis();
    //SystemClock.uptimeMillis() : 시스템이 부팅 된 후 밀리 초 단위로 계산됩니다.
    //이 클럭은 시스템이 딥 슬립 모드로 들어갈때 멈추지만 클럭 조정, 유휴 또는 다른 절전 메커니즘의 영향을
    //받지 않습니다.
    for (int i = 0; i < getImageSizeX(); ++i) {
      for (int j = 0; j < getImageSizeY(); ++j) {
        final int val = intValues[pixel++];
        addPixelValue(val);
      }
    }
    long endTime = SystemClock.uptimeMillis();
    //시간이 얼마나 걸렸는데 endTime - startTime 해서 보여준다.(ByteBuffer가 걸린시간)
    Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
  }

  /** Prints top-K labels, to be shown in UI as the results. */
  /** 결과로 UI에 표시 될 최상위 -K 라벨을 인쇄합니다. */

  /** 결과를 표시하는 중요한 코드 */

  private void printTopKLabels(SpannableStringBuilder builder) {
    for (int i = 0; i < getNumLabels(); ++i) {
      //getNumLables() : NumLables 객체를 가져온다.
      sortedLabels.add(
      //우선큐의 객체를 sortedLabels로 잡아주었다.
          new AbstractMap.SimpleEntry<>(labelList.get(i), getNormalizedProbability(i)));
          //AbstractMap.SimpleEntry : 이 클래스는 사용자 정의 맵 구현을 빌드하는 프로세스를 용이하게 합니다.
          //지정된 키로부터 지정된 값에 매핑을 나타내는 엔트리를 작성합니다.
      if (sortedLabels.size() > RESULTS_TO_SHOW) {
        sortedLabels.poll();
        //큐의 선두를 취득해 삭제합니다. 큐가 하늘의 경우는 그것을 돌려줍니다.
      }
    }

    final int size = sortedLabels.size();
    for (int i = 0; i < size; i++) {
      Map.Entry<String, Float> label = sortedLabels.poll();
      SpannableString span =
          new SpannableString(String.format("%s", label.getKey()));
        /*Log.d("11 번", label.getKey());*/
      int color;
      // Make it white when probability larger than threshold.
      // 확률이 임계 값보다 클때 흰색으로 만듭니다.
      if (label.getValue() > GOOD_PROB_THRESHOLD) {
        //flat GOOD_PROB_THRESHOLD = 0.3f;
        color = android.graphics.Color.WHITE;
      } else {
        color = SMALL_COLOR;
        //int SMALL_COLOR = 0xffddaa88;
      }
      // Make first item bigger.
      // 첫번째 항목을 더 크데 만들기
      if (i == size - 1) {
        float sizeScale = (i == size - 1) ? 1.25f : 0.8f;
        //삼항연상 만약  i랑 size-1이 맞다면 1.25f 값을 다르면 0.8f의 값을 나타내어라
        //만약 i = size -1 일때 1.25배 크게 특스트를 나태내고 아니면 0.8배 작게 나타내어라
        span.setSpan(new RelativeSizeSpan(sizeScale), 0, span.length(), 0);
        //RelativeSizeSpan(sizeScale) : 특정비율로 첨부된 텍스트의 크기를 균등하게 조정합니다.
      }
      span.setSpan(new ForegroundColorSpan(color), 0, span.length(), 0);
      //ForegroundColorSpan(color) : 스팬이 첨부 된 텍스트의 색상을 변경합니다.
      builder.insert(0, span);
    }
  }

  /**
   * Get the name of the model file stored in Assets.
   * Assests에 저장된 모델 파일의 이름을 가지고 옵니다.
   * @return
   */
  protected abstract String getModelPath();

  /**
   * Get the name of the label file stored in Assets.
   * Assets에 저장된 레이블 파일의 이름을 가져옵니다.
   * @return
   */
  protected abstract String getLabelPath();

  /**
   * Get the image size along the x axis.
   * x 축을 따라 이미지 크기를 가져옵니다
   * @return
   */
  protected abstract int getImageSizeX();

  /**
   * Get the image size along the y axis.
   * y 축을 따라 이미지 크기를 가져옵니다.
   * @return
   */
  protected abstract int getImageSizeY();

  /**
   * Get the number of bytes that is used to store a single color channel value.
   * 단일 색상 채널 값을 저장하는 데 사용되는 바이트 수를 가져옵니다.
   * @return
   */
  protected abstract int getNumBytesPerChannel();

  /**
   * Add pixelValue to byteBuffer.
   * byteValue에 pixelValue를 추가하십시오.
   * @param pixelValue
   */
  protected abstract void addPixelValue(int pixelValue);

  /**
   * Read the probability value for the specified label This is either the original value as it was
   * read from the net's output or the updated value after the filter was applied.
   * 지정된 라벨의 확률 값 읽기, 이것은 네트 출력에서 읽은 원래 값이거나 필터를 적용한 후 업데이트 된 값입니다.
   *
   * @param labelIndex
   * @return
   */
  protected abstract float getProbability(int labelIndex);

  /**
   * Set the probability value for the specified label.
   * 지정된 레이블에 대한 확률 값을 설정하십시오
   * @param labelIndex
   * @param value
   */
  protected abstract void setProbability(int labelIndex, Number value);

  /**
   * Get the normalized probability value for the specified label. This is the final value as it
   * will be shown to the user.
   * 지정된 레이블에 대한 정규화 된 확률 값을 가져옵니다. 이 값은 사용자에게 표시 될 최종 값입니다.
   * @return
   */
  protected abstract float getNormalizedProbability(int labelIndex);

  /**
   * Run inference using the prepared input in {@link #imgData}. Afterwards, the result will be
   * provided by getProbability().
   * 준비된 입력을 사용하여 유추를 실행합니다. 그 후, 결과는 getProbablilty()에 의해 제공됩니다.
   *
   * <p>This additional method is necessary, because we don't have a common base for different
   * primitive data types.
   * 다른 기본 데이터 유형에 대한 공통 기본을 가지고 있지 않기 때문에 추가 방법이 필요합니다.
   */
  protected abstract void runInference();

  /**
   * Get the total number of labels.
   * 총 레이블 수 가져오기
   * @return
   */
  protected int getNumLabels() {
    return labelList.size();
  }
}
