package team31.sofraguc;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.view.View.OnClickListener;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends ListActivity {
    Button sendButton;
    EditText textField;
    String userID = null;
    String userName = null;
    String userMsg = null;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = (Button) findViewById(R.id.send);
        textField = (EditText) findViewById(R.id.msg);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        setListAdapter(adapter);
        HerokuAppAPI.get("/welcome", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    userID = response.getString("uuid");
                    HerokuAppAPI.initAuthorizationHeader(userID);
                    adapter.add("SofraGUC: " + response.getString("message"));
                } catch (org.json.JSONException e) {
                    adapter.add(e.getMessage());
                }
            }
        });


        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userName == null) {
                    userName = textField.getText().toString().split(" ")[0];
                }
                userMsg = textField.getText().toString();
                adapter.add(userName + ": " + userMsg);
                String message = "{ \"message\" : \"" + textField.getText().toString() + "\" }";
                StringEntity messageEntity = null;
                try {
                    messageEntity = new StringEntity(message);
                    messageEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                } catch (java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                HerokuAppAPI.post(null, "/chat", messageEntity, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int StatusCode, Header[] headers, JSONObject response) {
                        try {
                            adapter.add("SofraGUC: " + response.getString("message"));
                        } catch (org.json.JSONException e) {
                            adapter.add(e.getMessage());

                        }
                    }
                    @Override
                    public void onFailure(int StatusCode, Header[] headers, String response, Throwable e) {
                        adapter.add("SofraGUC[ERROR]: " + response);
                    }
                });
                textField.getText().clear();
            }
        });
    }


}

class HerokuAppAPI {
    private static final String BASE_URL = "http://evening-bayou-86412.herokuapp.com";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static boolean userInitialized = false;

    public static boolean initAuthorizationHeader(String userID) {
        if (!userInitialized) {
            client.addHeader("Authorization", userID);
//            client.addHeader("Content-Type", "application/json");
            userInitialized = true;
            return true;
        } else {
            return false;
        }
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, StringEntity paramEntity, AsyncHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), paramEntity, "application/json",responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
