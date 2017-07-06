package com.example.tenthtask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {

    private TextView textView;

    private ImageView HeaderImage;
    private ImageView weather;
    private ImageView magnet;
    private float RotateDegree = 0f;
    private SensorManager mSensorManager;
    SQLiteDatabase SQLITEDATABASE;
    TextView Magneticc;
    TextView Baroment;
    TextView CompOrient;
    TextView sensor;
    Button Submit,EditData;
    String Name, PhoneNumber, Subject ;
    Boolean CheckEditTextEmpty ;
    String SQLiteQuery ;


    final String FILENAME = "file";
    final String DIR_SD = "MyFiles";
    final String FILENAME_SD = "fileSD";

    public static DecimalFormat DECIMAL_FORMATTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.view);

        HeaderImage = (ImageView) findViewById(R.id.CompassView);
        weather = (ImageView) findViewById(R.id.weather);
        magnet = (ImageView) findViewById(R.id.magnet);

        Submit = (Button)findViewById(R.id.button1);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBCreate();
                SubmitData2SQLiteDB();
            }
        });

        EditData = (Button)findViewById(R.id.button2);
        EditData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditDataActivity.class);
                startActivity(intent);
            }
        });

        CompOrient = (TextView) findViewById(R.id.Header);
        Magneticc = (TextView) findViewById(R.id.Magneticc);
        Baroment = (TextView) findViewById(R.id.Baroment);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMATTER = new DecimalFormat("#.0000", symbols);


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
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if(event.sensor.getType()==Sensor.TYPE_ORIENTATION) {
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
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float magX = event.values[0];
            float magY = event.values[1];
            float magZ = event.values[2];
            double magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));

            Magneticc.setText(DECIMAL_FORMATTER.format(magnitude) + " \u00B5Tesla");
            if(magnitude>=65){
                magnet.setImageResource(R.drawable.magnetv);
            }
            else{
                magnet.setImageResource(R.drawable.magnet);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float value  = event.values[0];

            Baroment.setText(value + " hPa");
            if(value<=945){
                weather.setImageResource(R.drawable.shturm);
            }
            else if(value>=946&&value<=965){
                weather.setImageResource(R.drawable.rain);
            }
            else if(value>=966&&value<=985){
                weather.setImageResource(R.drawable.cloude);
            }
            else if(value>=986&&value<=1015){
                weather.setImageResource(R.drawable.cloudesun);
            }
            else{
                weather.setImageResource(R.drawable.sun);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onclick(View v) {
        switch (v.getId()) {
            case R.id.btnWrite:
                writeFile();
                break;
            case R.id.btnRead:
                readFile();
                break;
            case R.id.btnWriteSD:
                writeFileSD();
                break;
            case R.id.btnReadSD:
                readFileSD();
                break;
        }
    }
    void writeFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput(FILENAME, MODE_PRIVATE)));
            String text = "Compass\n"+CompOrient.getText().toString()+"\nMagnetic\n"+Magneticc.getText().toString()+"\nBaroment\n"+Baroment.getText().toString();
            bw.write(text); bw.write("\n"); bw.close();
            Toast.makeText(getApplicationContext(), "File saved", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(FILENAME)));
            String str = "";
            String buf="";

            while ((str = br.readLine()) != null) {
                buf+=str+"\n";
            }
            textView.setText(buf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeFileSD() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "ERROR cant use SD" + Environment.getExternalStorageState(), Toast.LENGTH_SHORT).show();
            return;
        }
        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        sdPath.mkdirs();
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
            String text = "Compass\n"+CompOrient.getText().toString()+"\nMagnetic\n"+Magneticc.getText().toString()+"\nBaroment\n"+Baroment.getText().toString();
            bw.write(text); bw.write("\n"); bw.close();
            Toast.makeText(getApplicationContext(), "Saved on SD: " + sdFile.getAbsolutePath().toString(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readFileSD() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "ERROR cant use SD" + Environment.getExternalStorageState(), Toast.LENGTH_SHORT).show();
            return;
        }
        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            String str = "";
            while ((str = br.readLine()) != null) {
                textView.setText(str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void DBCreate(){
        SQLITEDATABASE = openOrCreateDatabase("DemoDataBase", Context.MODE_PRIVATE, null);
        SQLITEDATABASE.execSQL("CREATE TABLE IF NOT EXISTS demoTable(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name VARCHAR, phone_number VARCHAR, subject VARCHAR);");
    }
    public void SubmitData2SQLiteDB(){
        Name = CompOrient.getText().toString();
        PhoneNumber = Magneticc.getText().toString();
        Subject = Baroment.getText().toString();
        CheckEditTextIsEmptyOrNot( Name,PhoneNumber, Subject);
        if(CheckEditTextEmpty == true)
        {
            SQLiteQuery = "INSERT INTO demoTable (name,phone_number,subject) VALUES('"+Name+"', '"+PhoneNumber+"', '"+Subject+"');";
            SQLITEDATABASE.execSQL(SQLiteQuery);
            Toast.makeText(MainActivity.this,"Data Submit Successfully", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(MainActivity.this,"Please Fill All the Fields", Toast.LENGTH_LONG).show();
        }
    }
    public void CheckEditTextIsEmptyOrNot(String Name,String PhoneNumber, String subject ){
        if(TextUtils.isEmpty(Name) || TextUtils.isEmpty(PhoneNumber) || TextUtils.isEmpty(Subject)){
            CheckEditTextEmpty = false ;
        }
        else {
            CheckEditTextEmpty = true ;
        }
    }
}
