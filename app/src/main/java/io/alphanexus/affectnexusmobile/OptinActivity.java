package io.alphanexus.affectnexusmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class OptinActivity extends AppCompatActivity {

    private TextView Privacy;
    private TextView TOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optin);

        Privacy = findViewById(R.id.privacy_text);
        TOS = findViewById(R.id.tos_text);

        Privacy.setMovementMethod(LinkMovementMethod.getInstance());
        TOS.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // Data IS NOT shared by user
    public void goNexusNo(View view) {
        // By default, on sign up the value of share_data is false - nothing to do
        Intent intent = new Intent(this, NexusActivity.class);
        startActivity(intent);
    }

    // Data IS shared by user
    public void goNexusYes(View view) {
        // By default, on sign up the value of share_data is false - change it to true

        Intent intent = new Intent(this, NexusActivity.class);
        startActivity(intent);
    }
}
