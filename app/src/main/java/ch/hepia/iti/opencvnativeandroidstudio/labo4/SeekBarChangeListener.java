package ch.hepia.iti.opencvnativeandroidstudio.labo4;

import android.content.Intent;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by raed on 27.03.18.
 */

public class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
    private TextView textView;

    public SeekBarChangeListener(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            textView.setText(String.valueOf(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
