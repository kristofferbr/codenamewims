package sw805f16.codenamewims;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;
import android.hardware.SensorManager;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    /*For smoothing purposes*/
    static final float ALPHA = 0.15f;

    /*Orientation Vector smoothing arrays*/
    float vecvals[];
    float vecinputX[] = new float[2];
    float vecinputY[] = new float[2];
    float vecinputZ[] = new float[2];
    float vecoutput[] = new float[2];
    float vecoutputX[] = new float[2];
    float vecoutputY[] = new float[2];
    float vecoutputZ[] = new float[2];

    /*Magnetic Readings*/
    public double magX;
    public double magY;
    public double magZ;
    double result[] = new double[2];

    /*Rotation Vector Readings*/
    double vecX = 0;
    double vecY = 0;
    double vecZ = 0;

    /*Degrees Readings*/
    double angleX;
    double angleY;
    double angleZ;

    /*Incrementors*/
    int i = 0;
    int y = 0;

    /*Wifi stuff*/
    WifiManager wifi;
    List<ScanResult> scan;


    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Sensor cSensor;



    private SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sens = event.sensor;

            if(sens.getType() == Sensor.TYPE_MAGNETIC_FIELD){

                magX = event.values[0];
                magY = event.values[1];
                magZ = event.values[2];

                result = rotatevector(magX,magY,(float)angleZ);

            }

            else if(sens.getType() == Sensor.TYPE_ROTATION_VECTOR)
            {

                vecX = event.values[0];
                vecY = event.values[1];
                vecZ = event.values[2];


                angleX = correctdegrees((vecX * 180));
                angleY = correctdegrees((vecY * 180));
                angleZ = correctdegrees((vecZ * 180));


                writeToVec(vecX,vecY,vecZ);


            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        wifi.startScan();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        cSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan = wifi.getScanResults();
                for(ScanResult s : scan){
                    s.
                }

            }
        });
    }


    @Override
    protected void onResume(){
            super.onResume();

        mSensorManager.registerListener(listener, mSensor, 200000);
        mSensorManager.registerListener(listener, cSensor, 100000);

    }

    public void writeToVec(double x, double y, double z)
    {

        TextView X = (TextView) findViewById(R.id.vectorX);
        TextView Y = (TextView) findViewById(R.id.vectorY);
        TextView Z = (TextView) findViewById(R.id.vectorZ);
        TextView orientX = (TextView) findViewById(R.id.orientationX);
        TextView orientY = (TextView) findViewById(R.id.orientationY);
        TextView orientZ = (TextView) findViewById(R.id.orientationZ);

        X.setText("Raw RotX: " + Double.toString(x));
        Y.setText("Raw RotY: " + Double.toString(y));
        Z.setText("Raw RotZ " + Double.toString(z));

        orientX.setText("Degrees X: " + Double.toString(angleX));
        orientY.setText("Degrees Y: " + Double.toString(angleY));
        orientZ.setText("Degrees Z: " + Double.toString(angleZ));


    }

    /***
     * hence:
     * x' = x cos f - y sin f
     * y' = y cos f + x sin f
     * @param x
     * @param y
     * @param degrees
     * @return
     */
    public double[] rotatevector(double x, double y, float degrees){
        double res[] = new double[2];

        res[0] = x*Math.cos(Math.toRadians(degrees)) - y*Math.sin(Math.toRadians(degrees));
        res[1] = y*Math.cos(Math.toRadians(degrees)) + x*Math.sin(Math.toRadians(degrees));

        TextView magnetX = (TextView) findViewById(R.id.magX);
        TextView magnetY = (TextView) findViewById(R.id.magY);

        magnetX.setText("magX: " + Double.toString(res[0]));
        magnetY.setText("magY: " + Double.toString(res[1]));

        return res;

    }


    public void rotateMagneticFieldVector(double degreeX,double degreeY, double degreeZ, double mX, double mY, double mZ)
    {

        float MagneticVector[] = {(float)mX,(float) mY, (float)mZ,1};

        float Xrotated[];
        float Yrotated[];
        float Zrotated[];


        // First the X axis
        Xrotated = rotatevector1((float)degreeX,MagneticVector,1f,0f,0f);

        // Secondly Y rotated
        Yrotated = rotatevector1((float)degreeY,Xrotated,0f,1f,0f);

        //Lastly Z rotated
        Zrotated = rotatevector1((float)degreeZ,Yrotated,0f,0f,1f);


        TextView magnetX = (TextView) findViewById(R.id.magX);
        TextView magnetY = (TextView) findViewById(R.id.magY);
        TextView magnetZ = (TextView) findViewById(R.id.magZ);


        magnetX.setText("magX: " + Float.toString(Zrotated[0]));
        magnetY.setText("magY: " + Float.toString(Zrotated[1]));
        magnetZ.setText("magZ: " + Float.toString(Zrotated[2]));

    }


    public float[] rotatevector1(float degr,float vec[], float x,float y,float z){

        float[] matrix = new float[16];
        float[] rotatedVector = new float[4];

        Matrix.setIdentityM(matrix, 0);
        Matrix.rotateM(matrix, 0, degr, x, y, z);
        Matrix.multiplyMV(rotatedVector, 0, matrix, 0, vec, 0);

        return rotatedVector;

    }

    public double correctdegrees(double degr){
        /*
        if(degr<0)
        {
            return 180+(180+degr);
        }
        else {
            return degr;
        }*/

        return degr;
    }

    /*Smoothing algorithm provided by:
    * http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter
    *
    * */
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}




