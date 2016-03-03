package sw805f16.codenamewims;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by kbrod on 02-03-2016.
 * Class for creating a transparent Imageview with a dot that is to be placed on top of a
 * map, in order to indicate position
 */
public class PositionOverlay {


    /*Global values used for the creation of the imageview*/
    ImageView overlay;
    Paint paints = new Paint();
    Context con;
    Bitmap emptyBit;


    /*****
     * The constructor the class which has the needed parameters for generating the view
     * @param context The applicationContext
     */
    PositionOverlay(Context context){

        con = context;
    }


    /***
     * The function that generates the Imageview
     * @param positionX is the X offset
     * @param positionY is the Y offset
     * @return Imageview with a point in the specified location
     */
    public ImageView generateImageView(int positionX, int positionY)
    {



        /*The bitmap on which the point is drawn*/
        emptyBit = Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);


        Canvas can = new Canvas(emptyBit);

        /*Is set to transparrent so the view below is visible*/
        can.drawColor(Color.TRANSPARENT);

        /*The point is drawn*/
        can.drawCircle((float) positionX, (float) positionY,5f, paints);
        //can.drawPoint((float) positionX, (float) positionY, paints);

        /*The imageview is Instantiated*/
        overlay = new ImageView(con);

        /*The bitmap is added to the Imageview*/
        overlay.setImageBitmap(emptyBit);

        /*Returns the Imageview*/
        return overlay;
    }



}
