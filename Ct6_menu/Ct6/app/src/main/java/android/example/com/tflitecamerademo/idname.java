package android.example.com.tflitecamerademo;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class idname extends StringRequest {
    final static private String UPLOAD_URL = "http://hihit22.cafe24.com/blobdemo.php";
    private Map<String, String> parameters;

    public idname(String userid, String name, String image, Response.Listener<String> listener) {
        super(Method.POST, UPLOAD_URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userId",userid);
        parameters.put("name",name);
        parameters.put("image",image);
    }
    @Override
    public Map<String, String> getParams() {
        return parameters;
    }
}
