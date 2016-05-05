package sw805f16.codenamewims;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import java.util.List;

/**
 * Created by Kogni on 06-Apr-16.
 */
public class ShoppingListAdapter extends ArrayAdapter<RelativeItemLayout> {

    private final Activity context;
    private WimsPoints currentStart = new WimsPoints(0,0);

    private ArrayList<RelativeItemLayout> objectList;

    /*public ShoppingListAdapter(Context context, @LayoutRes int resource, @NonNull List<LinearItemLayout> objects) {
        super(context, resource, objects);
    }*/

    public ShoppingListAdapter(Activity context, @LayoutRes int resource, @NonNull List<RelativeItemLayout> objects) {
        super(context, resource, objects);
        this.objectList = new ArrayList<>(objects);
        this.context = context;
    }

    /*public ShoppingListAdapter(Context context, @LayoutRes int resource, @NonNull List<LinearItemLayout> objects,
                               @NonNull ArrayList<WimsPoints> supprod) {
        this(context, resource, objects);
    }*/

    public void swap(int i, int j) {
        RelativeItemLayout tempPair1 = getItem(i);
        RelativeItemLayout tempPair2 = getItem(j);
        remove(tempPair1);
        insert(tempPair2, i);
        remove(tempPair2);
        insert(tempPair1, j);

    }

    public void editItemEnum(int i, ItemEnum status) {
        RelativeItemLayout tempPair1 = getItem(i);
        tempPair1.setStatus(status);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = context.getLayoutInflater();
        RelativeItemLayout rowView = (RelativeItemLayout) inflater.inflate(R.layout.item_layout, null, true);
        rowView.setText(getItem(position).getText());
        rowView.setImageId(getItem(position).getImageId());
        if(getItem(position).getImageId() != 0){
            ImageView imageView = (ImageView) getItem(position).getChildAt(1);
            imageView.setVisibility(View.VISIBLE);
        }

        return rowView;
    }

    /**
     * This method is used for marking and unmarking the items in the item list
     *
     * @param position  The position of the item in the list
     * @param longClick True if the method is called from a long click, False if it should be marked as put in the basket
     */
    public void markUnmarkItem(int position, boolean longClick) {
        RelativeItemLayout item =  getItem(position);
        ItemEnum marking = ItemEnum.MARKED;
        //First, we determine what the current status of the item is
        //If the item is set to checkmark, we either want to change it to skip or unmark it

        if (item.getImageId() != null && item.getImageId() == R.drawable.checkmark) {
            //If the item was longClicked, we change the drawable to skip
            if (longClick) {
                item.setImageId(R.drawable.skip);
            }
            //If the item was not longClicked we set it to UNMARKED.
            else {
                marking = ItemEnum.UNMARKED;
                //We also remove the drawable reference
                item.setImageId(0);
                //After adding it the item to the unmarked list we sort the list
                // TODO: When we have positions, change this to that
            }
        }
        //If the item is set to skip, we either want to change it to checkmark or unmark it.
        else if (item.getImageId() != null && item.getImageId() == R.drawable.skip) {
            //If a long click was performed we unmark the item
            if (longClick) {
                marking = ItemEnum.UNMARKED;
                item.setImageId(0);
                // TODO: When we have positions, change this to that
            }
            //If a normal click is performed, we checkmark the item
            else {
                //When the user clicks a skipped item with a normal click, the drawable is replaced with a checkmark
                item.setImageId(R.drawable.checkmark);
            }
            //If the item isnt set to any drawable, we either want to set it to skip or checkmark
        } else {
            //If a long click is performed, the item is set to skip
            if (longClick) {
                item.setImageId(R.drawable.skip);

            }
            //Otherwise, we place the checkmark drawable
            else {
                item.setImageId(R.drawable.checkmark);
            }
            marking = ItemEnum.MARKED;
        }
        editItemEnum(position, marking);
        sortItemList(currentStart);

    }

    public void sortItemList(WimsPoints start) {
        ArrayList<WimsPoints> tmpSet = new ArrayList<>();
        ArrayList<WimsPoints> path = new ArrayList<>();
        WimsPoints point;
        String text;
        Integer index;

        //The first loop through the unmarked item list is to pull the text from the LinearLayouts
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getStatus() != ItemEnum.UNAVAILABLE) {
                text = getItem(i).getText();
                //We then find the index of the product with that name, in the products list
                index = JSONContainer.indexOfProductWithName(text);
                if (index != null) {
                    tmpSet.add(JSONContainer.getProducts().get(index));
                }
            }
        }

        path.add(start);

        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getStatus() != ItemEnum.UNAVAILABLE && !tmpSet.isEmpty()) {
                //First we find the nearest neighbor between the start and the WimsPoints in the tmpSet
                point = nearestNeightbor(start, tmpSet);
                //When the nearest neighbor is found the start is updated
                start = point;
                path.add(point);
                //Then we remove the point from the tmpSet
                tmpSet.remove(point);
            }
        }

        this.sort(new RelativeItemLayoutStatusComparator(path));
    }

    /**
     * A nearest neighbor algorithm. It finds the point in a set that is nearest the start point
     *
     * @param start The start point
     * @param set   The set of points for comparison
     * @return The WimsPoint closest to the start point
     */
    private WimsPoints nearestNeightbor(WimsPoints start, ArrayList<WimsPoints> set) {
        WimsPoints closestPoint = null;
        //We initialize a closest distance to the max value of a float
        float closestDistance = Float.MAX_VALUE;
        //We iterate over the set to find the nearest neighbor
        for (int i = 0; i < set.size(); i++) {
            //If the Euclidean distance between start and this point is lower than closestDistance we update it
            if (start.distance(set.get(i).x, set.get(i).y) < closestDistance) {
                //We set the closest point to be this point and update the closest distance
                closestPoint = set.get(i);
                closestDistance = start.distance(set.get(i).x, set.get(i).y);
            }
        }
        //When we have found the nearest neighbor we return it
        return closestPoint;
    }

    public int getPositionByName(String name){
        int position = 0;
        for(int i = 0; i < getCount(); i++){
            if(getItem(i).getText().equals(name)){
                position = i;
                break;
            }
        }
        return position;
    }
}
