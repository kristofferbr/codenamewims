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
    Context con;
    PositionOverlay pos;


    public PositionOverlayFactory(Context contt){
        con = contt;
        pos = new PositionOverlay(con);
    }


    public ImageView getPostitionOverlay(int x, int y)
    {
        return pos.generateImageView(x,y);
    }


}
