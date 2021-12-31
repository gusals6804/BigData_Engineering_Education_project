package android.example.com.tflitecamerademo;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SendImage {
    private static SendImage sInstance;
    private RequestQueue requestQueue;
    private  static Context mCtx;

    private  SendImage(Context context){
        mCtx = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue()
    {
        if(requestQueue==null)
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        return requestQueue;
    }

    public static synchronized SendImage getsInstance(Context context)
    {
        if(sInstance==null){
            sInstance = new SendImage(context);
        }
        return sInstance;
    }
    public<T> void addToRequestQue(Request<T> request)
    {
        getRequestQueue().add(request);
    }
}
