package android.example.com.Login;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DeleteRequest extends StringRequest {
    final static private  String URL = "http://hihit22.cafe24.com/Delete.php";
    private Map<String,String> parameters;

    public  DeleteRequest(String userID, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userID", userID);
    }

    @Override
    public Map<String, String>getParams(){
        return parameters;
    }
}
