package sw805f16.codenamewims;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.ViewDebug;
import android.widget.TextView;
import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity {

    public float magX;
    public float magY;
    public float magZ;
    public float[] magmX = {0,0,0};
    public float[] magmY = {0,0,0};
    public float orientation;
    int i = 0;

    public double vectorlength11;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Sensor cSensor;

    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sens = event.sensor;

            if(sens.getType() == Sensor.TYPE_MAGNETIC_FIELD){

                double tempmagX = event.values[0];
                double tempmagY = event.values[1];

                double magdX1 = tempmagX;
                double magdx2 = tempmagX;
                double magdY1 = tempmagY;
                double magdy2 = tempmagY;



                double orientationd = Math.toRadians((double) orientation);
                magdX1 = (magdX1*Math.cos(orientationd))-(magdY1*Math.sin(orientationd));
                magdY1 = (magdx2*Math.sin(orientationd))+(magdY1*Math.cos(orientationd));

                magmX[i%3] = (float)magdX1;
                magmY[i%3] = (float) magdY1;


                magX = (magmX[0] + magmX[1] + magmX[2])/3;
                magY = (magmY[0] + magmY[1] + magmY[2])/3;

                i++;

                if(i == 3000){
                    i = 0;
                }


            }
            else if(sens.getType() == Sensor.TYPE_ORIENTATION)
            {
                float orientationtemp = event.values[0];

                if(orientationtemp > 180)
                {
                    orientation = orientationtemp - 360;
                }
                else
                {
                    orientation = orientationtemp;
                }

                orientation = event.values[0];

            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        cSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Magnetometer should update", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                TextView magnetX = (TextView) findViewById(R.id.magX);
                TextView magnetY = (TextView) findViewById(R.id.magY);
                TextView magnetZ = (TextView) findViewById(R.id.magZ);
                TextView vectorlenght = (TextView) findViewById(R.id.vectorlength);
                TextView loc = (TextView) findViewById(R.id.location);
                TextView orien = (TextView) findViewById(R.id.orientation);

                vectorlength11 = Math.sqrt(Math.pow(magX,2)+Math.pow(magY,2)+Math.pow(magZ,2));

                magnetX.setText("X: " + Float.toString(magX));
                magnetY.setText("Y: " + Float.toString(magY));
                magnetZ.setText("Z: " + Float.toString(magZ));
                vectorlenght.setText(Integer.toString(mSensor.getMinDelay()));
                orien.setText(Float.toString(orientation));

                whereami(loc, magX, magY);


            }
        });
    }


    @Override
    protected void onResume(){
            super.onResume();
            mSensorManager.registerListener(listener, mSensor, 50000);
            mSensorManager.registerListener(listener, cSensor, 50000);


        }

    public void whereami(TextView text, Float x,Float y){

        if(x < 2.5 && x > -5.5 && y < 10.5 && y > 2.5){
            text.setText("Dør");
        }
        else if ( x < -0.5 && x > -8.5 && y < 14.5 && y > 6.5 ){
            text.setText("Vindue");
        }
        else if(x < -6 && x > -14 && y < 8 && y > 0){
            text.setText("Hjørne 1");
        }
        else if( x < 16 && x  > 10 && y < 8 && y > 0){
            text.setText("Hjørne 2");
        }
        else{
            text.setText("Cant locate");
        }
    }

}




