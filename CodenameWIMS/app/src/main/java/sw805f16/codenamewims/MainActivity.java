package sw805f16.codenamewims;

import android.opengl.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    Boolean clicked = false;
    ScaleGestureDetector Scale;
    Matrix matrix;
    float scale = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

        Scale = new ScaleGestureDetector(this,new ScaleDetector());

    }

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


}
