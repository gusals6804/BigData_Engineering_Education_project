package com.flatfisher.dialogflowchatbotsample;

import android.content.Intent;
import android.example.com.tflitecamerademo.MainActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

import static android.speech.tts.TextToSpeech.ERROR;

import com.example.android.tflitecamerademo.R;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.ResponseMessage;
import ai.api.model.Result;
import ai.api.model.Status;

import static android.speech.tts.TextToSpeech.ERROR;

public class ChatbotActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = MainActivity.class.getName();
    private Gson gson = GsonFactory.getGson();
    private AIDataService aiDataService;
    private ChatView chatView;
    private User myAccount;
    private User droidKaigiBot;
    private TextToSpeech tts;

    public Intent i;
    SpeechRecognizer mRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        // ???????????? ui ???????????? ??????
        initChatView();

        //////////////// ???????????? /////////////////////////////////////////////
        //Intent
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        //?????? ???????????? ??????   kr-KR ????????? ?????????
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        //Recognizer ??????
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        Button button = findViewById(R.id.button1);
        //???????????? ??????
        Button button2 = findViewById(R.id.button2);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????? ?????? ????????? ???????????? ??????
                //????????? ?????????????????? button ??? ??????????????? ??????
                mRecognizer.startListening(i);
            }
        });

        /////////////// ????????? ???????????? ///////////////////////////////////
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    // ????????? ????????????.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        //Language, Dialogflow Client access token
        // dialogflow ?????? ??????
        final LanguageConfig config = new LanguageConfig("ko", "a9a18089c8fc4e3298e6017dac1c0d32");
        initService(config);
    }

    // ?????? ??????
    private RecognitionListener listener;
                {
                    listener = new RecognitionListener() {

                        @Override
                        public void onRmsChanged(float rmsdB) {
                            // TODO Auto-generated method stub
                        }

                        //??????????????? ??????????????? ????????? ???????????? ??????
                        //????????? ???????????? ????????? HelloWorld!!????????? ???????????? ?????????
                        @Override
                        public void onResults(Bundle results) {
                            String key = "";
                            key = SpeechRecognizer.RESULTS_RECOGNITION;
                            ArrayList<String> mResult = results.getStringArrayList(key);
                            String[] rs = new String[mResult.size()];
                            mResult.toArray(rs);

                            chatView.setInputText("" + rs[0]);
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // TODO Auto-generated method stub

            }

            //??????????????? ????????? ???????????? ???
            @Override
            public void onError(int error) {
                if (error == mRecognizer.ERROR_NETWORK_TIMEOUT) {
                    Toast.makeText(getApplicationContext(), "???????????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_NETWORK) {
                    Toast.makeText(getApplicationContext(), "???????????? ??????", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_AUDIO) {
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_SERVER) {
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_CLIENT) {
                    Toast.makeText(getApplicationContext(), "??????????????? ??????", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_SPEECH_TIMEOUT) {
                    Toast.makeText(getApplicationContext(), "?????? ????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_NO_MATCH) {
                    Toast.makeText(getApplicationContext(), "????????? ????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                } else if (error == mRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Toast.makeText(getApplicationContext(), "??????????????? ??????", Toast.LENGTH_SHORT).show();
                }

                // TODO Auto-generated method stub

            }

            @Override
            public void onEndOfSpeech() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onBeginningOfSpeech() {
                // TODO Auto-generated method stub

            }
        };
    }
    ////////////////////////////////////////////////////////////////////////////////////////////

    // ????????? ?????????
    @Override
    public void onClick(View v) {

                final Message message = new Message.Builder()
                        .setUser(myAccount)
                        .setRightMessage(true)
                        .setMessageText(chatView.getInputText())
                        .hideIcon(true)
                        .build();
                //Set to chat view
                chatView.send(message);
                sendRequest(chatView.getInputText());
                //Reset edit text
                chatView.setInputText("");
        }


    /*
     * AIRequest should have query OR event
     */

    // ????????? ????????????
    private void sendRequest(String text) {
        Log.d(TAG, text);
        final String queryString = String.valueOf(text);
        final String eventString = null;
        final String contextString = null;

        if (TextUtils.isEmpty(queryString) && TextUtils.isEmpty(eventString)) {
            onError(new AIError(getString(R.string.non_empty_query)));
            return;
        }

        new AiTask().execute(queryString, eventString, contextString);
    }

    public class AiTask extends AsyncTask<String, Void, AIResponse> {
        private AIError aiError;

        @Override
        protected AIResponse doInBackground(final String... params) {
            final AIRequest request = new AIRequest();
            String query = params[0];
            String event = params[1];
            String context = params[2];

            if (!TextUtils.isEmpty(query)) {
                request.setQuery(query);
            }

            if (!TextUtils.isEmpty(event)) {
                request.setEvent(new AIEvent(event));
            }

            RequestExtras requestExtras = null;
            if (!TextUtils.isEmpty(context)) {
                final List<AIContext> contexts = Collections.singletonList(new AIContext(context));
                requestExtras = new RequestExtras(contexts, null);
            }

            try {
                return aiDataService.request(request, requestExtras);
            } catch (final AIServiceException e) {
                aiError = new AIError(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final AIResponse response) {
            if (response != null) {
                onResult(response);
            } else {
                onError(aiError);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////

    // ????????? ??????????????? ??????
    private void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Variables
                gson.toJson(response);
                final Status status = response.getStatus();
                final Result result = response.getResult();
                final String speech = result.getFulfillment().getSpeech();
                final Metadata metadata = result.getMetadata();
                final HashMap<String, JsonElement> params = result.getParameters();
                final String gsonData = gson.toJson(response.getResult().getFulfillment());
                final List<ResponseMessage> responseMessages = result.getFulfillment().getMessages();
                for (ResponseMessage message :
                        responseMessages) {
                    Class<?> class1 = message.getClass();
                    Log.i(TAG, String.format("%s", class1.getCanonicalName()));
                }

                //Log.i(TAG, String.valueOf(messges));
                // Logging
                Log.d(TAG, "onResult");
                Log.i(TAG, "Received success response");
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());
                Log.i(TAG, "Action: " + result.getAction());
                Log.i(TAG, "Speech: " + speech);

                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s",
                                entry.getKey(), entry.getValue().toString()));
                    }
                }

                if (speech == null) {
                    try {
                        JSONObject json = new JSONObject(gsonData);
                        JSONArray rows = json.getJSONArray("messages");
                        String a[] = new String[rows.length()];
                        for (int i = 0; i < rows.length(); i++) {
                            JSONObject row = rows.getJSONObject(i);
                            Log.d("2222", "" + row.toString());
                            JSONArray speechArray = row.getJSONArray("speech");
                            Log.d("2222", "" + speechArray.toString());
                            a[i] = speechArray.toString();
                            Log.d("3333", a[i]);

                            tts.speak(a[i], TextToSpeech.QUEUE_ADD, null); // ????????? ????????????

                            for (int j = 0; j < speechArray.length(); j++) {
                                String speechs = speechArray.get(j).toString();
                                Log.i(TAG, speechs);
                                Log.d("1111", "" + rows.length());
                                final Message receivedMessage = new Message.Builder()
                                        .setUser(droidKaigiBot)
                                        .setRightMessage(false)
                                        .setMessageText(speechs)
                                        .build();
                                chatView.receive(receivedMessage);
                                Log.d("1111", "" + receivedMessage);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                //Update view to bot says
                if (speech != null) {
                    final Message receivedMessage = new Message.Builder()
                            .setUser(droidKaigiBot)
                            .setRightMessage(false)
                            .setMessageText(speech)
                            .build();

                    chatView.receive(receivedMessage);
                    tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null); // ????????? ????????????
                }

            }
        });
    }

    /// TTS ????????? ??????
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS ????????? ??????????????? ????????? ???????????? ??????????????? ????????????.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    private void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, error.toString());
            }
        });
    }

    // ?????? ?????? ??????
    private void initChatView() {
        int myId = 0;
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_user);
        String myName = "Fish";
        myAccount = new User(myId, myName, icon);

        int botId = 1;
        String botName = "DroidKaigi";
        droidKaigiBot = new User(botId, botName, icon);


        chatView = (ChatView) findViewById(R.id.chat_view);
        chatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.green500));
        chatView.setLeftBubbleColor(Color.WHITE);
        chatView.setBackgroundColor(ContextCompat.getColor(this, R.color.blueGray500));
        chatView.setSendButtonColor(ContextCompat.getColor(this, R.color.lightBlue500));
        chatView.setSendIcon(R.drawable.ic_action_send);
        chatView.setRightMessageTextColor(Color.WHITE);
        chatView.setLeftMessageTextColor(Color.BLACK);
        chatView.setUsernameTextColor(Color.WHITE);
        chatView.setSendTimeTextColor(Color.WHITE);
        chatView.setDateSeparatorColor(Color.WHITE);
        chatView.setInputTextHint("new message...");
        chatView.setMessageMarginTop(5);
        chatView.setMessageMarginBottom(5);
        chatView.setOnClickSendButtonListener(this);
    }

    private void initService(final LanguageConfig languageConfig) {
        final AIConfiguration.SupportedLanguages lang =
                AIConfiguration.SupportedLanguages.fromLanguageTag(languageConfig.getLanguageCode());
        final AIConfiguration config = new AIConfiguration(languageConfig.getAccessToken(),
                lang,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
    }
}
