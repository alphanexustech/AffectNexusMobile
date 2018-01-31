package io.alphanexus.affectnexusmobile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView Username;
    private EditText Password;
    private Button Login;

    private TextView Info;
    private TextView Error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();

        Username = (AutoCompleteTextView)findViewById(R.id.username);
        Password = (EditText) findViewById(R.id.password);
        Login = (Button) findViewById(R.id.log_in_button);

        Info = (TextView)findViewById(R.id.info);
        Error = (TextView)findViewById(R.id.error);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(Username.getText().toString(), Password.getText().toString());
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

    private void validate(String username, String password) {
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            Password.setError(getString(R.string.error_field_required));
            focusView = Password;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            Username.setError(getString(R.string.error_field_required));
            focusView = Username;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            // Also, set the other error messages.
            Error.setText("The log in information is invalid.");
            Info.setText("");
        } else {
            Error.setText("");
            Info.setText("Loading...");
            serverValidation(username, password);
        }
    }

    private void serverValidation(final String username, final String password) {
        String url = "http://affectnexus.com:3000/users/login";
        JSONObject payload = new JSONObject();
        try {
            payload.put("password", password);
            payload.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,
                url, payload, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String FILENAME = "user_data";
                    String string = "hello world!";

                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    // Save the response
                    fos.write(string.getBytes());
                    fos.close();
                    
                    Info.setText("");
                    Intent intent = new Intent(LoginActivity.this, NexusActivity.class);
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Error.setText("There was a server error.");
                    Info.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                    Error.setText("There was a server error.");
                    Info.setText("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Error.setText("Sorry, the username and password do not match.");
                Info.setText("");
            }
        });

        // Add the request to the queue
        HTTPService.getInstance(LoginActivity.this).addToRequestQueue(jsonRequest);
    }



}
