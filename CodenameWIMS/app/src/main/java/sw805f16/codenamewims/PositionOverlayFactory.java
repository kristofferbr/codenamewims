package sw805f16.codenamewims;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by kbrod on 02-03-2016.
 * The factory class that generates the Imageview with the point
 *
 */
public class PositionOverlayFactory{

    public ImageView getPostitionOverlay(int x, int y, Context con)
    {
        PositionOverlay pos = new PositionOverlay(x,y,con);
        return pos.generateImageView();
    }


}
