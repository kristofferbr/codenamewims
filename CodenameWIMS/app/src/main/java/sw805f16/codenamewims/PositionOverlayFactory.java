package sw805f16.codenamewims;

import android.content.Context;
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
        return pos.generateImageViewWithSpot(x, y);
    }

    public ImageView getRouteBetweenTwoPoints(WimsPoints start, WimsPoints goal){
        return pos.drawRoute(start, goal);
    }

    public ImageView getBitMapReDrawnSpot(ImageView view, int x, int y){
        return pos.drawSpotOnSameBitmap(view, x, y);
    }

    public ImageView getBitMapReDrawnLine(ImageView view, int StartX, int StartY, int EndX, int EndY){
        return pos.drawLineOnSameMap(view,StartX,StartY,EndX,EndY);
    }

    public ImageView getPositionOfFingerPrintPoint(int x, int y){
        return pos.generateImageViewForFingerpriting(x,y);
    }
}
