package sw805f16.codenamewims;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by kbrod on 02-03-2016.
 * The factory class that generates the Imageview with the point
 *
 */
public class PositionOverlayFactory{
    private static final int BITMAP_WIDTH = 1312;
    private static final int BITMAP_HEIGHT = 2132;

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

    public ImageView getRoute(WimsPoints start, ArrayList<WimsPoints> intermediateGoals){
        return pos.drawPath(start, intermediateGoals);
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
