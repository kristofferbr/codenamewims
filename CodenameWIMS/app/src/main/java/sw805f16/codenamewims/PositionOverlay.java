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
    int positionX;
    int positionY;
    ImageView overlay;
    Canvas can;
    Paint paints = new Paint();
    Context con;


    /*****
     * The constructor the class which has the needed parameters for generating the view
     * @param x The offset in X
     * @param y The offset in Y
     * @param context The applicationContext
     */
    PositionOverlay(int x, int y, Context context){

        positionX = x;
        positionY = y;
        con = context;
    }


    /***
     * The function that generates the Imageview
     * @return Imageview with a point in the specified location
     */
    public ImageView generateImageView()
    {

        Bitmap emptyBit = Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
        can = new Canvas(emptyBit);
        can.drawColor(Color.TRANSPARENT);
        can.drawPoint((float) positionX, (float) positionY, paints);
        overlay = new ImageView(con);

        overlay.setImageBitmap(emptyBit);


        return overlay;
    }



}
