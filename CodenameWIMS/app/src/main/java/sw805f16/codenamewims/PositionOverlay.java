package sw805f16.codenamewims;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

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
        emptyBit = Bitmap.createBitmap(500,750,Bitmap.Config.ARGB_8888);

        /*Make the canvas draw on the bitmap*/
        Canvas can = new Canvas(emptyBit);

        /*Is set to transparrent so the view below is visible*/
        can.drawColor(Color.TRANSPARENT);

        /*The point is drawn*/
        can.drawCircle((float) positionX, (float) positionY, 5f, paints);
        //can.drawPoint((float) positionX, (float) positionY, paints);

        /*The imageview is Instantiated*/
        overlay = new ImageView(con);

        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        overlay.setLayoutParams(param);

        /*The bitmap is added to the Imageview*/
        overlay.setImageBitmap(emptyBit);

        /*Returns the Imageview*/
        return overlay;
    }

    public ImageView drawRoute(WimsPoints StartPoint, WimsPoints EndPoint){

        /*Start Values */
        ArrayList<WimsPoints> closedSet = new ArrayList<>();
        ArrayList<WimsPoints> openSet = new ArrayList<>();
        openSet.add(StartPoint);
        WimsPoints cameFrom;
        WimsPoints current;
        float temp_score = 0;
        StartPoint.Parent = null;

        StartPoint.gscore = 0;
        StartPoint.fscore = EndPoint.distance(StartPoint.x, StartPoint.y);

         /*The bitmap on which the point is drawn*/
        emptyBit = Bitmap.createBitmap(500,750,Bitmap.Config.ARGB_8888);

        /*Make the canvas draw on the bitmap*/
        Canvas can = new Canvas(emptyBit);

        /*Is set to transparrent so the view below is visible*/
        can.drawColor(Color.TRANSPARENT);

         /*The imageview is Instantiated*/
        overlay = new ImageView(con);


        while(!openSet.isEmpty()){
            current = getPointwithLowestScore(openSet, EndPoint);

            if(current == EndPoint){
                constructPath(current,can);
                break;
            }
            openSet.remove(current);
            closedSet.add(current);

            for(int i = 0; i< current.Neighbours.size(); i++){
                if(!closedSet.contains(current.Neighbours.get(i))){

                    temp_score = current.gscore + current.Neighbours.get(i).distance(current.x, current.y);
                    if(!openSet.contains(current.Neighbours.get(i))){
                        openSet.add(current.Neighbours.get(i));
                    } else if(temp_score >= current.Neighbours.get(i).gscore){
                        continue;
                    }

                    current.Neighbours.get(i).Parent = current;
                    current.Neighbours.get(i).gscore = temp_score;
                    current.Neighbours.get(i).fscore = temp_score +
                                                       current.Neighbours.get(i).distance(EndPoint.x, EndPoint.y);
                }


            }


        }

        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        overlay.setLayoutParams(param);

        /*The bitmap is added to the Imageview*/
        overlay.setImageBitmap(emptyBit);

        /*Returns the Imageview*/
        return overlay;


    }


    private WimsPoints getPointwithLowestScore(ArrayList<WimsPoints> Openset, WimsPoints endPoint){

        WimsPoints result = Openset.get(0);
            for(int i = 1; i<Openset.size();i++){

                if(Openset.get(i).fscore <
                        result.fscore){
                    result = Openset.get(i);
                }

            }
        return result;

    }

    private void constructPath(WimsPoints point,Canvas can){

        if(point.Parent != null)
        {
            can.drawLine(point.x,point.y,point.Parent.x,point.Parent.y,paints);
            constructPath(point.Parent,can);
        }

    }

    public ImageView drawSpotOnSameBitmap(ImageView view, int positionX, int positionY){
        /*The bitmap on which the point is drawn*/


        ImageView viewToReturn = new ImageView(con);
        viewToReturn.setLayoutParams(view.getLayoutParams());
        Bitmap temp_bitmap = ((BitmapDrawable)view.getDrawable()).getBitmap();

        Bitmap mutable_bitmap = temp_bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Bitmap bitmap = Bitmap.createScaledBitmap(temp_bitmap,500,750,false);

        /*Make the canvas draw on the bitmap*/
        Canvas can = new Canvas(bitmap);

        /*Is set to transparrent so the view below is visible*/


        /*The point is drawn*/
        can.drawCircle((float) positionX, (float) positionY, 5f, paints);
        //can.drawPoint((float) positionX, (float) positionY, paints);

        /*The imageview is Instantiated*/
        viewToReturn.setImageBitmap(bitmap);

        return viewToReturn;


    }

    public ImageView drawLineOnSameMap(ImageView view, int StartPositionX, int StartPositionY,
                                       int EndPositionX, int EndPositionY){
        ImageView viewToReturn = new ImageView(con);
        viewToReturn.setLayoutParams(view.getLayoutParams());
        Bitmap temp_bitmap = ((BitmapDrawable)view.getDrawable()).getBitmap();

        Bitmap mutable_bitmap = temp_bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Bitmap bitmap = Bitmap.createScaledBitmap(temp_bitmap,500,750,false);

        /*Make the canvas draw on the bitmap*/
        Canvas can = new Canvas(bitmap);

        /*Is set to transparrent so the view below is visible*/


        /*The point is drawn*/
        can.drawLine(StartPositionX,StartPositionY,EndPositionX,EndPositionY,paints);


        /*The imageview is Instantiated*/
        viewToReturn.setImageBitmap(bitmap);

        return viewToReturn;


    }



}
