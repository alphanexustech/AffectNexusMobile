package io.alphanexus.affectnexusmobile;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OptinActivity extends AppCompatActivity {

    private TextView Privacy;
    private TextView TOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optin);

        Privacy = findViewById(R.id.privacy_text);
        TOS = findViewById(R.id.tos_text);

        Privacy.setMovementMethod(LinkMovementMethod.getInstance());
        TOS.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // Data IS NOT shared by user
    public void goNexusNo(View view) {
        // By default, on sign up the value of affectiveData is false - nothing to do
        Intent intent = new Intent(this, NexusActivity.class);
        startActivity(intent);
    }

    // Data IS shared by user
    public void goNexusYes(View view) {
        // By default, on sign up the value of affectiveData is false - change it to true
        JSONObject USER_DATA = getDataFromFile("user_data");
        updateSettingsAfterLoad(USER_DATA);

        Intent intent = new Intent(this, NexusActivity.class);
        startActivity(intent);
    }

    private JSONObject getDataFromFile(String fileName) {
        FileInputStream fis;
        JSONObject DATA = new JSONObject();
        try {
            fis = openFileInput(fileName);
            StringBuffer fileContent = new StringBuffer("");
            byte[] buffer = new byte[1024];

            int n;
            while ((n = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            DATA = new JSONObject(fileContent.toString());
            Log.i("Status", "Loaded data. - " + fileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return DATA;
    }

    private void updateSettingsAfterLoad(final JSONObject USER_DATA) {
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String assistantURL = ConfigHelper.getConfigValue(this, "assistantServer") + ':' + ConfigHelper.getConfigValue(this, "assistantPort");
        String url = assistantURL + "/users/account/";

        final String FINAL_ACCESS_TOKEN = ACCESS_TOKEN;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Set/update settings info in a file for later
                try {
                    String FILENAME = "user_settings_data";
                    String string = response.get("user").toString();
                    Log.i("Status", "Saved new settings user data.");

                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    // Save the response
                    fos.write(string.getBytes());
                    fos.close();

                    JSONObject USER_SETTINGS_DATA = getDataFromFile("user_settings_data");
                    patchProfile(USER_DATA, USER_SETTINGS_DATA, "affectiveData", "1");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            // Headers are specified here.
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Access-Control-Allow-Origin", "*");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + FINAL_ACCESS_TOKEN);
                return headers;
            }
        };

        // Add the request to the queue
        HTTPService.getInstance(OptinActivity.this).addToRequestQueue(jsonRequest);
    }

    private void patchProfile(JSONObject USER_DATA, JSONObject USER_SETTINGS_DATA, String toggleString, String toggleStringValue) {

        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject payload = new JSONObject();

        // Handle toggles
        String affectiveDataValue = "0"; // Default to false if something CRITICAL HAPPENS
        try {
            affectiveDataValue = (String) USER_SETTINGS_DATA.get("affectiveData");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (toggleString == "affectiveData") {
            affectiveDataValue = toggleStringValue;
        } else {
            Log.i("TOGGLE_STRING", "Received a value other than 'affectiveData'");
        }

        String displayName = "";
        // If the user never changes the display name, then it possible that it is null.
        // This logic is for backwards compatibility.
        try {
            if (USER_SETTINGS_DATA.get("displayName") != null) {
                displayName = String.valueOf(USER_SETTINGS_DATA.get("displayName"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            payload.put("firstName", USER_SETTINGS_DATA.get("firstName"));
            payload.put("lastName", USER_SETTINGS_DATA.get("lastName"));
            payload.put("displayName", displayName);
            payload.put("affectiveData", affectiveDataValue);
            payload.put("emailSub", USER_SETTINGS_DATA.get("emailSub"));
            payload.put("interfaceComplexity", USER_SETTINGS_DATA.get("interfaceComplexity"));
            payload.put("email", USER_SETTINGS_DATA.get("email"));
            payload.put("newPassword", ""); // Empty password strings indicate no-password-update to back-end
            payload.put("confirmPassword", ""); // Empty password strings indicate no-password-update to back-end
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String assistantURL = ConfigHelper.getConfigValue(this, "assistantServer") + ':' + ConfigHelper.getConfigValue(this, "assistantPort");
        String url = assistantURL + "/users/account";

        final String FINAL_ACCESS_TOKEN = ACCESS_TOKEN;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PATCH,
                url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("Settings", "Updated Profile");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                // Normally, 'handleServerError()' would be called, but we want the user to start using
                // the app. We can always ask them about opting in later.
            }
        }) {
            // Headers are specified here.
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Access-Control-Allow-Origin", "*");
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + FINAL_ACCESS_TOKEN);
                return headers;
            }
        };

        // Add the request to the queue
        HTTPService.getInstance(OptinActivity.this).addToRequestQueue(jsonRequest);
    }
}
