package sw805f16.codenamewims;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import java.util.List;

/**
 * Created by Kogni on 06-Apr-16.
 */
public class ShoppingListAdapter extends ArrayAdapter<Pair<LinearLayout, ItemEnum>> {

    public ShoppingListAdapter(Context context,@LayoutRes int resource,@NonNull List<Pair<LinearLayout, ItemEnum>> objects){
        super(context, resource, objects);
    }

    public void swap(int i, int j){

    }

}
