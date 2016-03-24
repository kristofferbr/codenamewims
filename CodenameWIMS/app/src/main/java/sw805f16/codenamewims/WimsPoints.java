package sw805f16.codenamewims;

import android.graphics.PointF;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by kbrod on 21/03/2016.
 *
 *
 */
public class WimsPoints extends PointF implements Parcelable{
    static Creator CREATOR;

    public ArrayList<WimsPoints> Neighbours = new ArrayList<>();
    public float gscore;
    public float fscore;
    public WimsPoints Parent;

    public WimsPoints(){

    }

    public WimsPoints(float x, float y){
        set(x,y);
    }



    public void AddNeighbours(ArrayList<WimsPoints> NeighboursToAdd){
        for(int i = 0; i > NeighboursToAdd.size();i++){
            Neighbours.add(NeighboursToAdd.get(i));
        }

    }


    public void DeleteNeighbor(WimsPoints point)
    {
        Neighbours.remove(point);
    }


    public float distance(float thisX,float thisy){
        float result;
        float tempX = Math.abs(x - thisX);
        float tempY = Math.abs(y - thisy);

        return (float)Math.sqrt(tempX*tempX+tempY*tempY);
    }


}