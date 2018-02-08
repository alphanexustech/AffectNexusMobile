package io.alphanexus.affectnexusmobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NexusActivity extends AppCompatActivity {

    private TextView NexusContent;
    ArrayList<Process> processes;
    private ImageView SettingsIcon;
    private FloatingActionButton ProcessFAB;
    public TextView InfoText;
    public TextView DescriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nexus);

        SettingsIcon = findViewById(R.id.settings_icon);
        ProcessFAB = findViewById(R.id.process_fab);
        InfoText = findViewById(R.id.process_info_text);
        DescriptionText = findViewById(R.id.process_description);

        SettingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NexusActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        ProcessFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NexusActivity.this, ProcessActivity.class);
                startActivity(intent);
            }
        });

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
            loadNexus(USER_DATA);

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

    private void loadNexus(JSONObject USER_DATA) {
        String ACCESS_TOKEN = new String();
        try {
            ACCESS_TOKEN = (String) USER_DATA.get("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String collection = "beta_records-07jan2018";
        String url = "http://affectnexus.com:3000/scorer/analyses/" + collection + "/1/5"; // For now, 20 For mobile from most recent page.

        final String FINAL_ACCESS_TOKEN = ACCESS_TOKEN;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                NexusContent.setText(response.toString());

                JSONObject data;
                JSONArray processData = new JSONArray();
                try {
                    data = (JSONObject) response.get("data");
                    processData = data.getJSONArray("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (processData.length() > 0) {
                    // Lookup the recyclerView in activity layout
                    RecyclerView rvProcesses = (RecyclerView) findViewById(R.id.rvProcesses);

                    // Initialize processes
                    processes = Process.createProcessList(processData);
                    // Create adapter passing in the sample user data
                    ProcessesAdapter adapter = new ProcessesAdapter(NexusActivity.this, processes);
                    // Attach the adapter to the recyclerView to populate items
                    rvProcesses.setAdapter(adapter);
                    // Set layout manager to position the items
                    rvProcesses.setLayoutManager(new LinearLayoutManager(NexusActivity.this));
                    InfoText.setText("");
                    DescriptionText.setText(R.string.nexus_description);
                } else {
                    InfoText.setText("Welcome, let's get started! To run a process, click the button in the bottom right corner.");
                    DescriptionText.setText(R.string.nexus_description_start);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
//                NexusContent.setText("There was an error loading the content.");
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
        HTTPService.getInstance(NexusActivity.this).addToRequestQueue(jsonRequest);
    }
}
