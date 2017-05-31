package com.example.tenthtask;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {


    private ImageView HeaderImage;
    private float RotateDegree = 0f;
    private SensorManager mSensorManager;
    TextView CompOrient;

    TextView sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HeaderImage = (ImageView) findViewById(R.id.CompassView);

        CompOrient = (TextView) findViewById(R.id.Header);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensor = (TextView) findViewById(R.id.sensor);

        List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        String buf = null;
        for(Sensor sensor : list)
            buf += sensor.getName() + "\n";

        sensor.setText(buf);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float degree = Math.round(event.values[0]);
        CompOrient.setText("Deviation from the north: " + Float.toString(degree) + " degrees");

        RotateAnimation rotateAnimation = new RotateAnimation(
                RotateDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        rotateAnimation.setDuration(210);

        rotateAnimation.setFillAfter(true);

        HeaderImage.startAnimation(rotateAnimation);
        RotateDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
