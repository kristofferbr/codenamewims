package sw805f16.codenamewims;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import java.util.List;

/**
 * Created by Kogni on 06-Apr-16.
 */
public class ShoppingListAdapter extends ArrayAdapter<LinearItemLayout> {

    public ShoppingListAdapter(Context context, @LayoutRes int resource, @NonNull List<LinearItemLayout> objects) {
        super(context, resource, objects);
    }

    public ShoppingListAdapter(Context context, @LayoutRes int resource, @NonNull List<LinearItemLayout> objects,
                               @NonNull ArrayList<WimsPoints> supprod) {
        this(context, resource, objects);
    }

    public void swap(int i, int j) {
        LinearItemLayout tempPair1 = getItem(i);
        LinearItemLayout tempPair2 = getItem(j);
        remove(tempPair1);
        insert(tempPair2, i);
        remove(tempPair2);
        insert(tempPair1, j);

    }

    public void editItemEnum(int i, ItemEnum status) {
        LinearItemLayout tempPair1 = getItem(i);
        LinearItemLayout edittedPair = tempPair1;
        remove(tempPair1);
        edittedPair.setStatus(status);
        insert(edittedPair, i);
    }

    private void reorderList() {

    }

    /**
     * This method is used for marking and unmarking the items in the item list
     *
     * @param position  The position of the item in the list
     * @param longClick True if the method is called from a long click, False if it should be marked as put in the basket
     */
    public void markUnmarkItem(int position, boolean longClick) {
        LinearItemLayout item = getItem(position);
        ImageView mark = (ImageView) item.getChildAt(1);
        //First, we determine what the current status of the item is
        //If the item is set to checkmark, we either want to change it to skip or unmark it

        if (item.getImageId() != null && item.getImageId() == R.drawable.checkmark) {
            //If the item was longClicked, we change the drawable to skip
            if (longClick) {
                mark.setImageDrawable(getContext().getResources().getDrawable(R.drawable.skip));
                item.setImageId(R.drawable.skip);
            }
            //If the item was not longClicked we set it to UNMARKED.
            else {
                //Because this is called with a normal click the checkmark is removed
                mark.setImageDrawable(null);
                editItemEnum(position, ItemEnum.UNMARKED);
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
                mark.setImageDrawable(null);
                editItemEnum(position, ItemEnum.UNMARKED);
                item.setImageId(0);
                // TODO: When we have positions, change this to that
            }
            //If a normal click is performed, we checkmark the item
            else {
                //When the user clicks a skipped item with a normal click, the drawable is replaced with a checkmark
                mark.setImageDrawable(getContext().getResources().getDrawable(R.drawable.checkmark));
                item.setImageId(R.drawable.checkmark);
            }
            //If the item isnt set to any drawable, we either want to set it to skip or checkmark
        } else {
            //If a long click is performed, the item is set to skip
            if (longClick) {
                mark.setImageDrawable(getContext().getResources().getDrawable(R.drawable.skip));
                item.setImageId(R.drawable.skip);

            }
            //Otherwise, we place the checkmark drawable
            else {
                mark.setImageDrawable(getContext().getResources().getDrawable(R.drawable.checkmark));
                item.setImageId(R.drawable.checkmark);
            }
            editItemEnum(position, ItemEnum.MARKED);
        }

        sortItemList(new WimsPoints(0, 0));

    }

    public void sortItemList(WimsPoints start) {
        ArrayList<WimsPoints> tmpSet = new ArrayList<>();
        String text;
        int index;
        //The first loop through the unmarked item list is to pull the text from the LinearLayouts
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getStatus() == ItemEnum.UNMARKED) {
                text = ((TextView) getItem(i).getChildAt(0)).getText().toString();
                //We then find the index of the product with that name, in the products list
                index = JSONContainer.indexOfProductWithName(text);
                tmpSet.add(JSONContainer.getProducts().get(index));
            }
        }
        WimsPoints point;
        LinearItemLayout tmpLayout;
        //In the second loop we sort the list
        for (int i = 0; i < getCount(); i++) {
            if (getItem(i).getStatus() == ItemEnum.UNMARKED) {
                //First we find the nearest neighbor between the start and the WimsPoints in the tmpSet
                point = nearestNeightbor(start, tmpSet);
                //When the nearest neighbor is found the start is updated
                start = point;
                //Then we remove the point from the tmpSet
                tmpSet.remove(point);
                //In the third loop we do the actual sorting, using point
                for (int n = 0; n < getCount(); n++) {
                    if (getItem(n).getStatus() == ItemEnum.UNMARKED) {
                        text = ((TextView) getItem(n).getChildAt(0)).getText().toString();
                        //We check if the item has the same name as point
                        if (text.equalsIgnoreCase(point.getProductName())) {
                            //If we have a match we remove the item and add it on the position i from the enclosing loop
                            tmpLayout = getItem(n);
                            remove(tmpLayout);
                            insert(tmpLayout, i);
                            //When the match is found we break the inner loop
                            break;
                        }
                    }
                }
            }
        }

        //We then set the visibility of the top item to GONE
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
}
