package sw805f16.codenamewims;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
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


    public int addTwoNumbers(int x, int y)
    {
        return x+y;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Scale = new ScaleGestureDetector(this,new ScaleDetector());

        Button but = (Button)findViewById(R.id.talskrift);
        Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.mapb);

        map = new ImageView(getApplicationContext());
        final FrameLayout frame = (FrameLayout) findViewById(R.id.ramme);
        final PositionOverlayFactory fac = new PositionOverlayFactory(getApplicationContext());
        map.setImageBitmap(bit);
        frame.addView(map);

        frame.addView(fac.getPostitionOverlay(50, 50));


        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                frame.removeViewAt(1);

                if(i==1){
                    frame.addView(fac.getPostitionOverlay(50, 50));
                    i++;
                } else if(i == 2){
                    frame.addView(fac.getPostitionOverlay(100, 50));
                    i++;
                }else if (i ==3){
                    frame.addView(fac.getPostitionOverlay(100, 100));
                    i++;
                }else
                {
                    frame.addView(fac.getPostitionOverlay(50, 100));
                    i = 1;
                }

            }
        });

    }







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
