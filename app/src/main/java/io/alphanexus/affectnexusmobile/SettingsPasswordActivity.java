package io.alphanexus.affectnexusmobile;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SettingsPasswordActivity extends AppCompatActivity {

    private EditText NewPassword;
    private EditText ConfirmPassword;
    private Button UpdateProfile;
    private TextView InfoText;

    private void handleErrorMessage() {
        InfoText.setText("The was an error. We\'ll fix it when we find it.");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsPasswordActivity.this, R.color.colorErrorFont)
        );
        UpdateProfile.setEnabled(true);
    }

    private void handleServerError() {
        InfoText.setText("The was a server error.");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsPasswordActivity.this, R.color.colorErrorFont)
        );
        UpdateProfile.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_settings);
        setupActionBar();

        NewPassword = findViewById(R.id.new_password);
        ConfirmPassword = findViewById(R.id.confirm_password);
        UpdateProfile = findViewById(R.id.profile_button);
        InfoText = findViewById(R.id.info_text);

        JSONObject USER_DATA = getDataFromFile("user_data");
        loadSettings(USER_DATA);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
    
    private void loadSettings(JSONObject USER_DATA) {
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorMessage();
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    handleErrorMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                    handleErrorMessage();
                } catch (JSONException e) {
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
        HTTPService.getInstance(SettingsPasswordActivity.this).addToRequestQueue(jsonRequest);
    }

    public void updateProfile(View view) {

        String newPassword = NewPassword.getText().toString();
        String confirmPassword = ConfirmPassword.getText().toString();

        boolean cancel = false;
        UpdateProfile.setEnabled(false);
        View focusView = null;

        // Check for a valid confirm password.
        if (TextUtils.isEmpty(confirmPassword)) {
            ConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = ConfirmPassword;
            cancel = true;
        } else if (!(newPassword.equals(confirmPassword))) {
            ConfirmPassword.setError(getString(R.string.error_incorrect_confirm_password));
            focusView = ConfirmPassword;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(newPassword)) {
            NewPassword.setError(getString(R.string.error_field_required));
            focusView = NewPassword;
            cancel = true;
        } else if (!isPasswordValid(newPassword)) {
            NewPassword.setError(getString(R.string.error_invalid_password));
            focusView = NewPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            InfoText.setText("The confirmation password \nmust match the password");
            InfoText.setTextColor(
                    ContextCompat.getColor(SettingsPasswordActivity.this, R.color.colorErrorFont)
            );
            UpdateProfile.setEnabled(true);
        } else {
            ConfirmPassword.setError(null);
            InfoText.setText("Loading...");
            InfoText.setTextColor(
                    ContextCompat.getColor(SettingsPasswordActivity.this, R.color.colorSecondaryFont)
            );
            JSONObject USER_DATA = getDataFromFile("user_data");
            JSONObject USER_SETTINGS_DATA = getDataFromFile("user_settings_data");
            patchProfile(USER_DATA, USER_SETTINGS_DATA, newPassword, confirmPassword);
        }
    }


    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    private void patchProfile(JSONObject USER_DATA, JSONObject USER_SETTINGS_DATA, String newPassword, String confirmPassword) {
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorMessage();
        }

        JSONObject payload = new JSONObject();
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
            payload.put("firstName", USER_SETTINGS_DATA.get("email"));
            payload.put("lastName", USER_SETTINGS_DATA.get("username"));
            payload.put("displayName", displayName);
            payload.put("affectiveData", USER_SETTINGS_DATA.get("affectiveData"));
            payload.put("emailSub", USER_SETTINGS_DATA.get("emailSub"));
            payload.put("interfaceComplexity", USER_SETTINGS_DATA.get("interfaceComplexity"));
            payload.put("email", USER_SETTINGS_DATA.get("email"));
            payload.put("newPassword", newPassword);
            payload.put("confirmPassword", confirmPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorMessage();
        }

        String assistantURL = ConfigHelper.getConfigValue(this, "assistantServer") + ':' + ConfigHelper.getConfigValue(this, "assistantPort");
        String url = assistantURL + "/users/account/";

        final String FINAL_ACCESS_TOKEN = ACCESS_TOKEN;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PATCH,
                url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                InfoText.setText("Updated Successfully");
                InfoText.setTextColor(
                        ContextCompat.getColor(SettingsPasswordActivity.this, R.color.colorSuccessFont)
                );
                Log.i("Settings", "Updated Profile");
                UpdateProfile.setEnabled(true);
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
        HTTPService.getInstance(SettingsPasswordActivity.this).addToRequestQueue(jsonRequest);
    }
}
