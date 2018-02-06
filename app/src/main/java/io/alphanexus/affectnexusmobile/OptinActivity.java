package io.alphanexus.affectnexusmobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class OptinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optin);
    }

    public void goNexus(View view) {
        Intent intent = new Intent(this, NexusActivity.class);
        startActivity(intent);
    }
}
