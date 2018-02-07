package io.alphanexus.affectnexusmobile;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private Switch AffectiveDataToggle;
    private Switch EmailToggle;
    private TextView AffectiveDesciption;
    private TextView EmailDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();

        AffectiveDataToggle = findViewById(R.id.affective_data_toggle);
        AffectiveDesciption = findViewById(R.id.affect_description_text);
        EmailToggle = findViewById(R.id.email_toggle);
        EmailDescription = findViewById(R.id.email_description);

        // Make a call to the database and check the settings for the user

        // Make the updates for:
        // AffectiveDataToggle, EmailToggle

        AffectiveDataToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    AffectiveDesciption.setText(R.string.settings_affective_data_description_on);
                } else {
                    AffectiveDesciption.setText(R.string.settings_affective_data_description_off);
                    // The toggle is disabled
                }
            }
        });

        EmailToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    EmailDescription.setText(R.string.settings_email_subscription_description_on);
                } else {
                    EmailDescription.setText(R.string.settings_email_subscription_description_off);
                    // The toggle is disabled
                }
            }
        });
    }

    public void goProfile(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goPassword(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goAbout(View view) {
        Intent intent = new Intent(this, MainActivity.class);
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
