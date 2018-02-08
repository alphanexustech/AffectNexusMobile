package io.alphanexus.affectnexusmobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsAboutActivity extends AppCompatActivity {

    private TextView FacebookLink;
    private Button PrivacyButton;
    private Button TOSButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_settings);
        setupActionBar();

        FacebookLink = findViewById(R.id.author_link);
        PrivacyButton = findViewById(R.id.privacy_button);
        TOSButton = findViewById(R.id.tos_button);

        FacebookLink.setMovementMethod(LinkMovementMethod.getInstance());

        PrivacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://alphanex.us/privacy.pdf"));
                startActivity(myWebLink);
            }
        });

        TOSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://alphanex.us/tos.pdf"));
                startActivity(myWebLink);
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
}
