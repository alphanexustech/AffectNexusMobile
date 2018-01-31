package io.alphanexus.affectnexusmobile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
                    ConfirmPassword.getText().toString(),
                    Password.getText().toString()
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
        if (username != "" && password != "") {
            Error.setText("");
            Info.setText("Loading...");
            serverValidation(username, password);
        } else {
            Error.setText("The login information is invalid.");
            Info.setText("");
        }
    }

    private void serverValidation(final String username, final String password) {
        String url = "http://affectnexus.com:3000/users/signup";
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
                    Intent intent = new Intent(SignupActivity.this, NexusActivity.class);
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Info.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                    Error.setText("There was a server error");
                    Info.setText("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Error.setText("Sorry, ...");
                Info.setText("");
            }
        });

        // Add the request to the queue
        HTTPService.getInstance(SignupActivity.this).addToRequestQueue(jsonRequest);
    }



}
