package io.alphanexus.affectnexusmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ProcessActivity extends Activity {

    private ImageView SettingsIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        SettingsIcon = findViewById(R.id.settings_icon);

        SettingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProcessActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }


}
