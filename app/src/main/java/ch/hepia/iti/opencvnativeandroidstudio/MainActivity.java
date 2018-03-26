package ch.hepia.iti.opencvnativeandroidstudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickLabo1Botton(View view) {
        Intent intent = new Intent(MainActivity.this, Labo1Activity.class);
        startActivity(intent);
    }

    public void onClickLabo2Botton(View view) {
        Intent intent = new Intent(MainActivity.this, Labo2Activity.class);
        startActivity(intent);
    }

    public void onClickLabo3Botton(View view) {
        Intent intent = new Intent(MainActivity.this, Labo3Activity.class);
        startActivity(intent);
    }
}
