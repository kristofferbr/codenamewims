package sw805f16.codenamewims;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.service.voice.VoiceInteractionSession;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


/**
 * Adresss for server: "nielsema.ddns.net:3000"
 *
 * */

public class MainActivity extends AppCompatActivity {

    Boolean clicked = false;
    ScaleGestureDetector Scale;
    Matrix matrix;
    float scale = 1;
    int i = 1;
    ImageView map;

    /*Test for the test framework*/
    public int addTwoNumbers(int x, int y)
    {
        return x+y;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*Instantiate a Request Queue and a Requrst String*/
        //final RequestQueue rqueue = Volley.newRequestQueue(this);
        //final String url = "http://nielsema.ddns.net:3000/api/product";

        Button but = (Button) findViewById(R.id.chooseStore);

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),StoreMapActivity.class);

                startActivity(intent);
                /*Start the storemap activity*/
            }
        });

    }


    /****
     * The function that performs a request against our server
     * @param req The queue to add the request
     * @param url The url to request
     */
//    public void request(RequestQueue req, String url){
//
//        StringRequest stringreq = new StringRequest(Request.Method.GET,url,
//                new Response.Listener<String>(){
//
//                    TextView tex = (TextView) findViewById(R.id.response);
//
//                    @Override
//                    public void onResponse(String response){
//                        /*Set this response to a textview of sorts*/
//                        tex.setText(response.substring(0));
//                    }
//
//                },
//                new Response.ErrorListener(){
//                    TextView tex = (TextView) findViewById(R.id.response);
//                    @Override
//                    public void onErrorResponse(VolleyError error){
//                        tex.setText("INGEN FORBINDELSE");
//                        /*Do something with the error*/
//                    }
//                }
//        );
//
//        req.add(stringreq);
//    }




    /*Code for Scaling with pinch gestures*/
    /**
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Scale.onTouchEvent(event);
        return true;
    }

    public class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener{


        ImageView v = (ImageView) findViewById(R.id.billede);

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float tempscale = 1;

            tempscale = detector.getScaleFactor() * (scale);


            v.setScaleX(tempscale);
            v.setScaleY(tempscale);

            return super.onScale(detector);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            v.setScaleX(scale);
            v.setScaleY(scale);

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            scale = v.getScaleX();

            super.onScaleEnd(detector);
        }
    }
*/

}
