package ch.hepia.iti.opencvnativeandroidstudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ch.hepia.iti.opencvnativeandroidstudio.labo4.Labo4Activity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickButton(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.labo1_button:
                intent = new Intent(MainActivity.this, Labo1Activity.class);
                break;
            case R.id.labo2_botton:
                intent = new Intent(MainActivity.this, Labo2Activity.class);
                break;
            case R.id.labo3_botton:
                intent = new Intent(MainActivity.this, Labo3Activity.class);
                break;
            case R.id.labo4_botton:
                intent = new Intent(MainActivity.this, Labo4Activity.class);
                break;
        }
        startActivity(intent);
    }
}
