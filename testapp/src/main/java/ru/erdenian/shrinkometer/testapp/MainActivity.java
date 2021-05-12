package ru.erdenian.shrinkometer.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Toast", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unused")
    private String unusedMethod() {
        return null;
    }
}
