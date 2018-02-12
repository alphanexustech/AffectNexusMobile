package io.alphanexus.affectnexusmobile;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class DeleteProfileActivity extends AppCompatActivity {

    private TextView InfoText;
    private Button Nevermind;
    private Button DeleteProfile;

    private void handleErrorMessage() {
        InfoText.setVisibility(View.VISIBLE);
        InfoText.setText("The was an error. We\'ll fix it when we find it.");
        InfoText.setTextColor(
                ContextCompat.getColor(DeleteProfileActivity.this, R.color.colorErrorFont)
        );
        Nevermind.setEnabled(true);
        DeleteProfile.setEnabled(true);
    }

    private void handleServerError() {
        InfoText.setVisibility(View.VISIBLE);
        InfoText.setText("The was a server error.");
        InfoText.setTextColor(
                ContextCompat.getColor(DeleteProfileActivity.this, R.color.colorErrorFont)
        );
        Nevermind.setEnabled(true);
        DeleteProfile.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);
        setupActionBar();

        InfoText = findViewById(R.id.info_text);
        Nevermind = findViewById(R.id.no_action);
        DeleteProfile = findViewById(R.id.yes_action);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void goBack(View view) {
        Intent intent = new Intent(DeleteProfileActivity.this, SettingsProfileActivity.class);
        startActivity(intent);
    }

    public void reallyDeleteProfile(View view) {
        Nevermind.setEnabled(false);
        DeleteProfile.setEnabled(false);

        InfoText.setText("Loading...");
        InfoText.setTextColor(
                ContextCompat.getColor(DeleteProfileActivity.this, R.color.colorSecondaryFont)
        );
        JSONObject USER_DATA = getDataFromFile("user_data");
        deleteProfile(USER_DATA);
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

    private void deleteProfile(JSONObject USER_DATA) {
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorMessage();
        }
        String assistantURL = ConfigHelper.getConfigValue(this, "assistantServer") + ':' + ConfigHelper.getConfigValue(this, "assistantPort");
        String url = assistantURL + "/users/account/delete";

        final String FINAL_ACCESS_TOKEN = ACCESS_TOKEN;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.DELETE,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Set/update settings info in a file for later
                try {
                    // EMPTY File 1, maybe we will add new user content later.
                    String FILENAME = "user_settings_data";

                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    // Save the response
                    fos.write("".getBytes());
                    fos.close();

                    // EMPTY File 2, maybe we will add new user content later.
                    FILENAME = "user_data";

                    fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    // Save the response
                    fos.write("".getBytes());
                    fos.close();

                    Log.i("Status", "Delete user, and all file data.");
                    Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    handleErrorMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    handleErrorMessage();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                handleServerError();
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
        HTTPService.getInstance(DeleteProfileActivity.this).addToRequestQueue(jsonRequest);
    }
}
