package io.alphanexus.affectnexusmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsProfileActivity extends AppCompatActivity {

    private AutoCompleteTextView Email;
    private EditText FirstName;
    private EditText LastName;
    private EditText DisplayName;
    private Button UpdateProfile;
    private Button DeleteProfile;
    private TextView InfoText;

    private void handleErrorMessage() {
        InfoText.setVisibility(View.VISIBLE);
        InfoText.setText("The was an error. We\'ll fix it when we find it.");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsProfileActivity.this, R.color.colorErrorFont)
        );
        UpdateProfile.setEnabled(true);
        DeleteProfile.setEnabled(true);
    }

    private void handleServerError() {
        InfoText.setVisibility(View.VISIBLE);
        InfoText.setText("The was a server error.");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsProfileActivity.this, R.color.colorErrorFont)
        );
        UpdateProfile.setEnabled(true);
        DeleteProfile.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        setupActionBar();

        Email = findViewById(R.id.email);
        FirstName = findViewById(R.id.first_name);
        LastName = findViewById(R.id.last_name);
        DisplayName = findViewById(R.id.display_name);
        UpdateProfile = findViewById(R.id.profile_button);
        DeleteProfile = findViewById(R.id.profile_delete_button);
        InfoText = findViewById(R.id.info_text);

        // Set visibility to hidden until data is loaded later.
        Email.setVisibility(View.GONE);
        FirstName.setVisibility(View.GONE);
        LastName.setVisibility(View.GONE);
        DisplayName.setVisibility(View.GONE);
        UpdateProfile.setVisibility(View.GONE);

        InfoText.setText("Loading...");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsProfileActivity.this, R.color.colorSecondaryFont)
        );
        JSONObject USER_DATA = getDataFromFile("user_data");
        loadSettings(USER_DATA);

        DeleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsProfileActivity.this, DeleteProfileActivity.class);
                startActivity(intent);
            }
        });
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

                    // Finished Loading/Set text in Fields

                    JSONObject userSettings = (JSONObject) response.get("user");

                    String displayName = "";
                    // If the user never changes the display name, then it possible that it is null.
                    // This logic is for backwards compatibility.
                    try {
                        if (userSettings.get("displayName") != null) {
                            displayName = String.valueOf(userSettings.get("displayName"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Email.setText(String.valueOf(userSettings.get("email")));
                    FirstName.setText(String.valueOf(userSettings.get("firstName")));
                    LastName.setText(String.valueOf(userSettings.get("lastName")));
                    DisplayName.setText(displayName);
                    Email.setVisibility(View.VISIBLE);
                    FirstName.setVisibility(View.VISIBLE);
                    LastName.setVisibility(View.VISIBLE);
                    DisplayName.setVisibility(View.VISIBLE);
                    UpdateProfile.setVisibility(View.VISIBLE);
                    InfoText.setText("");
                    InfoText.setVisibility(View.GONE);
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
        HTTPService.getInstance(SettingsProfileActivity.this).addToRequestQueue(jsonRequest);
    }

    public void updateProfile(View view) {

        String email = Email.getText().toString();
        String firstName = FirstName.getText().toString();
        String lastName = LastName.getText().toString();
        String displayName = DisplayName.getText().toString();

        boolean cancel = false;
        UpdateProfile.setEnabled(false);
        DeleteProfile.setEnabled(false);
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            Email.setError(getString(R.string.error_field_required));
            focusView = Email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            Email.setError(getString(R.string.error_invalid_email));
            focusView = Email;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            InfoText.setVisibility(View.VISIBLE);
            InfoText.setText("The E-mail field has an error.");
            InfoText.setTextColor(
                    ContextCompat.getColor(SettingsProfileActivity.this, R.color.colorErrorFont)
            );
            UpdateProfile.setEnabled(true);
            DeleteProfile.setEnabled(true);
        } else {

            InfoText.setVisibility(View.VISIBLE);
            InfoText.setText("Loading...");
            InfoText.setTextColor(
                    ContextCompat.getColor(SettingsProfileActivity.this, R.color.colorSecondaryFont)
            );
            JSONObject USER_DATA = getDataFromFile("user_data");
            JSONObject USER_SETTINGS_DATA = getDataFromFile("user_settings_data");
            patchProfile(USER_DATA, USER_SETTINGS_DATA,
                    email, firstName, lastName, displayName);
        }
    }

    private boolean isEmailValid(String email) {
        String regex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher((CharSequence) email);
        return matcher.matches();
    }

    private void patchProfile(
            JSONObject USER_DATA, JSONObject USER_SETTINGS_DATA,
            String email, String firstName, String lastName, String displayName) {
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorMessage();
        }

        JSONObject payload = new JSONObject();
        try {
            payload.put("firstName", firstName);
            payload.put("lastName", lastName);
            payload.put("displayName", displayName);
            payload.put("affectiveData", USER_SETTINGS_DATA.get("affectiveData"));
            payload.put("emailSub", USER_SETTINGS_DATA.get("emailSub"));
            payload.put("interfaceComplexity", USER_SETTINGS_DATA.get("interfaceComplexity"));
            payload.put("email", email);
            payload.put("newPassword", ""); // Empty password strings indicate no-password-update to back-end
            payload.put("confirmPassword", ""); // Empty password strings indicate no-password-update to back-end
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
                InfoText.setVisibility(View.VISIBLE);
                InfoText.setText("Updated Successfully");
                InfoText.setTextColor(
                        ContextCompat.getColor(SettingsProfileActivity.this, R.color.colorSuccessFont)
                );
                Log.i("Settings", "Updated Profile");
                UpdateProfile.setEnabled(true);
                DeleteProfile.setEnabled(true);
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
        HTTPService.getInstance(SettingsProfileActivity.this).addToRequestQueue(jsonRequest);
    }
}
