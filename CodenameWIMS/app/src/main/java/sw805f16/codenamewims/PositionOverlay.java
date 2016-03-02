package sw805f16.codenamewims;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by kbrod on 02-03-2016.
 */
public class PositionOverlay {

    int positionX;
    int positionY;
    ImageView overlay;

    PositionOverlay(int x, int y, Context context){

        positionX = x;
        positionY = y;
        overlay = new ImageView(context);
    }


    



}
