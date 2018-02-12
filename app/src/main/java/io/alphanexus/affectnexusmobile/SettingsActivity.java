package io.alphanexus.affectnexusmobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
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

public class SettingsActivity extends AppCompatActivity {

    private Switch AffectiveDataToggle;
    private Switch EmailToggle;
    private TextView AffectiveDesciption;
    private TextView EmailDescription;
    private TextView InfoText;

    private Button ProfileButton;
    private Button PasswordButton;
    private Button AboutButton;

    private void handleErrorMessage() {
        InfoText.setText("The was an error. We\'ll fix it when we find it.");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsActivity.this, R.color.colorErrorFont)
        );
    }

    private void handleServerError() {
        InfoText.setText("The was a server error.");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsActivity.this, R.color.colorErrorFont)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();

        AffectiveDataToggle = findViewById(R.id.affective_data_toggle);
        AffectiveDesciption = findViewById(R.id.affective_data_description);
        EmailToggle = findViewById(R.id.email_toggle);
        EmailDescription = findViewById(R.id.email_description);

        ProfileButton = findViewById(R.id.profile_button);
        PasswordButton = findViewById(R.id.password_button);
        AboutButton = findViewById(R.id.about_button);
        InfoText = findViewById(R.id.info_text);


        AffectiveDataToggle.setVisibility(View.INVISIBLE);
        AffectiveDesciption.setText("Loading...");
        EmailToggle.setVisibility(View.INVISIBLE);
        EmailDescription.setText("Loading...");

        InfoText.setText("Loading...");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsActivity.this, R.color.colorSecondaryFont)
        );

        // Make the updates for the AffectiveDataToggle and EmailToggle
        AffectiveDataToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject USER_DATA = getDataFromFile("user_data");
                JSONObject USER_SETTINGS_DATA = getDataFromFile("user_settings_data");
                // Disable the other toggle until the call is finished
                EmailToggle.setEnabled(false);
                if (isChecked) {
                    AffectiveDesciption.setText(R.string.settings_affective_data_description_on);
                    // The toggle is ON
                    patchProfile(USER_DATA, USER_SETTINGS_DATA, "affectiveData", "1");
                } else {
                    AffectiveDesciption.setText(R.string.settings_affective_data_description_off);
                    // The toggle is OFF
                    patchProfile(USER_DATA, USER_SETTINGS_DATA, "affectiveData", "0");
                }
            }
        });

        EmailToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject USER_DATA = getDataFromFile("user_data");
                JSONObject USER_SETTINGS_DATA = getDataFromFile("user_settings_data");
                // Disable the other toggle until the call is finished
                AffectiveDataToggle.setEnabled(false);
                ProfileButton.setEnabled(false);
                PasswordButton.setEnabled(false);
                AboutButton.setEnabled(false);
                if (isChecked) {
                    // The toggle is ON
                    EmailDescription.setText(R.string.settings_email_subscription_description_on);
                    patchProfile(USER_DATA, USER_SETTINGS_DATA, "emailSub", "1");
                } else {
                    EmailDescription.setText(R.string.settings_email_subscription_description_off);
                    // The toggle is OFF
                    patchProfile(USER_DATA, USER_SETTINGS_DATA, "emailSub", "0");
                }
            }
        });

        // Make a call to the database and check the settings for the user
        JSONObject USER_DATA = getDataFromFile("user_data");
        loadSettings(USER_DATA);
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

                    JSONObject userSettings = (JSONObject) response.get("user");

                    boolean affectiveDataValue = 1 == Integer.parseInt((String) userSettings.get("affectiveData"));
                    if (affectiveDataValue) {
                        AffectiveDataToggle.setChecked(true);
                        AffectiveDesciption.setText(R.string.settings_affective_data_description_on);
                    } else {
                        AffectiveDataToggle.setChecked(false);
                        AffectiveDesciption.setText(R.string.settings_affective_data_description_off);
                    }

                    boolean emailSubValue = 1 == Integer.parseInt((String) userSettings.get("emailSub"));
                    if ((emailSubValue)) {
                        EmailToggle.setChecked(true);
                        EmailDescription.setText(R.string.settings_email_subscription_description_on);
                    } else {
                        EmailToggle.setChecked(false);
                        EmailDescription.setText(R.string.settings_email_subscription_description_off);
                    }

                    AffectiveDataToggle.setVisibility(View.VISIBLE);
                    EmailToggle.setVisibility(View.VISIBLE);
                    InfoText.setText("");

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
        HTTPService.getInstance(SettingsActivity.this).addToRequestQueue(jsonRequest);
    }

    private void patchProfile(JSONObject USER_DATA, JSONObject USER_SETTINGS_DATA, String toggleString, String toggleStringValue) {
        InfoText.setText("Loading...");
        InfoText.setTextColor(
                ContextCompat.getColor(SettingsActivity.this, R.color.colorSecondaryFont)
        );
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
            handleErrorMessage();
        }

        JSONObject payload = new JSONObject();

        // Handle toggles
        String affectiveDataValue = "0"; // Default to false if something CRITICAL HAPPENS
        String emailSubValue = "0"; // Default to false if something CRITICAL HAPPENS
        try {
            affectiveDataValue = (String) USER_SETTINGS_DATA.get("affectiveData");
            emailSubValue = (String) USER_SETTINGS_DATA.get("emailSub");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (toggleString == "affectiveData") {
            affectiveDataValue = toggleStringValue;
        } else if (toggleString == "emailSub") {
            emailSubValue = toggleStringValue;

        } else {
            Log.i("TOGGLE_STRING", "Received a value other than 'affectiveData' or 'emailSub'");
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
            payload.put("emailSub", emailSubValue);
            payload.put("interfaceComplexity", USER_SETTINGS_DATA.get("interfaceComplexity"));
            payload.put("email", USER_SETTINGS_DATA.get("email"));
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
                InfoText.setText("Settings Synced");
                InfoText.setTextColor(
                        ContextCompat.getColor(SettingsActivity.this, R.color.colorSuccessFont)
                );
                Log.i("Settings", "Updated Profile");
                updateUserSettingsDataFile(response);
                Log.i("Settings", "Updated User Settings File");
                // Enable toggles since update is finished
                ProfileButton.setEnabled(true);
                PasswordButton.setEnabled(true);
                AboutButton.setEnabled(true);
                AffectiveDataToggle.setEnabled(true);
                EmailToggle.setEnabled(true);
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
        HTTPService.getInstance(SettingsActivity.this).addToRequestQueue(jsonRequest);
    }

    private void updateUserSettingsDataFile(JSONObject newUserObject) {
        try {
            String FILENAME = "user_settings_data";
            String string = newUserObject.get("user").toString();
            Log.i("Status", "Saved new settings user data.");

            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            // Save the response
            fos.write(string.getBytes());
            fos.close();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goProfile(View view) {
        Intent intent = new Intent(this, SettingsProfileActivity.class);
        startActivity(intent);
    }

    public void goPassword(View view) {
        Intent intent = new Intent(this, SettingsPasswordActivity.class);
        startActivity(intent);
    }

    public void goAbout(View view) {
        Intent intent = new Intent(this, SettingsAboutActivity.class);
        startActivity(intent);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}
