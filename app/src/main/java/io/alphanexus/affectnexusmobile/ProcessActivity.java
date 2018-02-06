package io.alphanexus.affectnexusmobile;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProcessActivity extends Activity {

    private static final int MY_SOCKET_TIMEOUT_MS = 60000; // 1 minute, that's all the time it gets.
    private static final int MY_DEFAULT_MAX_RETRIES = 3;
    private ImageView SettingsIcon;
    private EditText ProcessText;
    private TextView RemainingCharacters;
    private Button StartProcessButton;
    private TextView InfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        SettingsIcon = findViewById(R.id.settings_icon);
        ProcessText = findViewById(R.id.process_text_field);
        RemainingCharacters = findViewById(R.id.characters_remaining);
        StartProcessButton = findViewById(R.id.start_process_button);
        InfoText = findViewById(R.id.process_info_text);

        SettingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProcessActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int maxAmount = 300;
                int amountRemaining;

                //This sets a textview to the current length
                amountRemaining = maxAmount - s.length();
                RemainingCharacters.setText(String.valueOf(amountRemaining));
                if (amountRemaining < 0) {
                    StartProcessButton.setEnabled(false);
                    RemainingCharacters.setTextColor(
                        ContextCompat.getColor(ProcessActivity.this, R.color.colorErrorFont)
                    );
                } else {
                    StartProcessButton.setEnabled(true);
                    RemainingCharacters.setTextColor(
                        ContextCompat.getColor(ProcessActivity.this, R.color.colorAccent)
                    );
                }
            }

            public void afterTextChanged(Editable s) {
            }
        };

        ProcessText.addTextChangedListener(mTextEditorWatcher);

        StartProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileInputStream fis;
                JSONObject USER_DATA;
                try {
                    fis = openFileInput("user_data");
                    StringBuffer fileContent = new StringBuffer("");
                    byte[] buffer = new byte[1024];

                    int n;
                    while ((n = fis.read(buffer)) != -1) {
                        fileContent.append(new String(buffer, 0, n));
                    }
                    USER_DATA = new JSONObject(fileContent.toString());
                    Log.i("Status", "Loaded user data.");

                    // Starts the process of calling the server - analyzes emotion.
                    InfoText.setText("Loading...");
                    startProcess(String.valueOf(ProcessText.getText()), USER_DATA);
                } catch (FileNotFoundException e) {
                    InfoText.setText("Sorry, there was an error. We'll fix it when we find it.");
                    e.printStackTrace();
                } catch (JSONException e) {
                    InfoText.setText("Sorry, there was an error. We'll fix it when we find it.");
                    e.printStackTrace();
                } catch (IOException e) {
                    InfoText.setText("Sorry, there was an error. We'll fix it when we find it.");
                    e.printStackTrace();
                }
            }
        });

    }

    private void startProcess(String processText, JSONObject USER_DATA) {
        String url = "http://affectnexus.com:3000/scorer/analyze_emotion_set";
        JSONObject payload = new JSONObject();
        try {
            // Request for processing emotions has a few knobs and levers.
            payload.put("doc", processText); // <-- We simplify for the user. User controls here, and only here.
            payload.put("lang", "english");
            payload.put("natural", "1");
            payload.put("stemmer", "1");
            payload.put("lemma", "1");
            payload.put("ub", 2);
            payload.put("lb", 2);
            payload.put("emotion_set", "all_emotions");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String FINAL_ACCESS_TOKEN = ACCESS_TOKEN;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,
                url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    InfoText.setText("");
                    Intent intent = new Intent(ProcessActivity.this, NexusActivity.class);
                    startActivity(intent);
                    StartProcessButton.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    InfoText.setText("There was a server error.");
                    StartProcessButton.setEnabled(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                InfoText.setText("There was a server error.");
                StartProcessButton.setEnabled(true);
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

        // Configure this request
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                MY_DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); // Set to 1.0 when I checked/logged on 2018-Feb-05

        // Add the request to the queue
        HTTPService.getInstance(ProcessActivity.this).addToRequestQueue(jsonRequest);
    }


}
