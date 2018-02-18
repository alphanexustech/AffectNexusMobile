package io.alphanexus.affectnexusmobile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private AutoCompleteTextView Email;
    private AutoCompleteTextView Username;
    private EditText ConfirmPassword;
    private EditText Password;
    private Button Signup;

    private TextView Info;
    private TextView Error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setupActionBar();

        Email = (AutoCompleteTextView)findViewById(R.id.email);
        Username = (AutoCompleteTextView)findViewById(R.id.username);
        Password = (EditText) findViewById(R.id.password);
        ConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        Signup = (Button) findViewById(R.id.sign_in_button);

        Info = (TextView)findViewById(R.id.info);
        Error = (TextView)findViewById(R.id.error);

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(
                    Email.getText().toString(),
                    Username.getText().toString(),
                    Password.getText().toString(),
                    ConfirmPassword.getText().toString()
                );
            }
        });
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void validate(String email, String username, String password, String confirmPassword) {
        boolean cancel = false;
        Signup.setEnabled(false);
        View focusView = null;

        // Check for a valid confirm password.
        if (TextUtils.isEmpty(confirmPassword)) {
            ConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = ConfirmPassword;
            cancel = true;
        } else if (!(password.equals(confirmPassword))) {
            ConfirmPassword.setError(getString(R.string.error_incorrect_confirm_password));
            focusView = ConfirmPassword;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            Password.setError(getString(R.string.error_field_required));
            focusView = Password;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            Password.setError(getString(R.string.error_invalid_password));
            focusView = Password;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            Username.setError(getString(R.string.error_field_required));
            focusView = Username;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            Username.setError(getString(R.string.error_invalid_username));
            focusView = Username;
            cancel = true;
        }

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
            // Also, set the other error messages.
            Error.setText("The sign up information is invalid.");
            Info.setText("");
            Signup.setEnabled(true);
        } else {
            Error.setText("");
            Info.setText("Loading...");
            serverValidation(email, username, password, confirmPassword);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    private boolean isUsernameValid(String username) {
        return username.length() > 2;
    }

    private boolean isEmailValid(String email) {
        String regex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher((CharSequence) email);
        return matcher.matches();
    }

    private void serverValidation( final String email, final String username,
            final String password, final String confirmPassword) {
        String assistantURL = ConfigHelper.getConfigValue(this, "assistantServer") + ':' + ConfigHelper.getConfigValue(this, "assistantPort");
        String url = assistantURL + "/users/signup";
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("username", username);
            payload.put("password", password);
            payload.put("confirmPassword", confirmPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,
                url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String FILENAME = "user_data";
                    String string = response.toString();
                    Log.i("Status", "Saved new user data.");

                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    // Save the response
                    fos.write(string.getBytes());
                    fos.close();
                    
                    Info.setText("");
                    Intent intent = new Intent(SignupActivity.this, OptinActivity.class);
                    startActivity(intent);
                    Signup.setEnabled(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Error.setText("There was a server error");
                    Info.setText("");
                    Signup.setEnabled(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    Error.setText("There was a server error");
                    Info.setText("");
                    Signup.setEnabled(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Error.setText("Sorry, the username is already in use.");
                Info.setText("");
                Signup.setEnabled(true);
            }
        });

        // Add the request to the queue
        HTTPService.getInstance(SignupActivity.this).addToRequestQueue(jsonRequest);
    }



}
