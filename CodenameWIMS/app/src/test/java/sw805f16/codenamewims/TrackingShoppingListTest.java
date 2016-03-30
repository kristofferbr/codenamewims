package sw805f16.codenamewims;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowListView;

import static org.robolectric.Shadows.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by kbrod on 09/03/2016.
 * So close no matter how far. Couldn't be much more from the heart..!
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class TrackingShoppingListTest {

    StoreMapActivity activity;
    ShoppingListFragment fragment;
    ShadowListView shadowItemList;
    ListView itemList;

    @Before
    public void setup() {
        activity = Robolectric.setupActivity(StoreMapActivity.class);
        fragment = (ShoppingListFragment) activity.getFragmentManager().findFragmentByTag("shoppingFragment");
        SearchView search = (SearchView) fragment.getView().findViewById(R.id.shopSearch);
        ShadowListView suggestionList = shadowOf((ListView) fragment.getView().findViewById(R.id.suggestions));
        shadowItemList = shadowOf((ListView) fragment.getView().findViewById(R.id.itemList));
        itemList = (ListView) fragment.getView().findViewById(R.id.itemList);

        try {
            String jsonString = activity.getResources().getString(R.string.shop_json);
            JSONObject dummyJson = new JSONObject(jsonString);
            fragment.extractInformationFromJson(dummyJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        search.setQuery("Milk", false);
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowItemList.populateItems();
    }

    @Test
    // As a user
    // I want to track my shopping by  checking in or out if I have found a specified Item
    // so that I can keep track of how my shopping has progressed.
    public void tracking_shopping_list(){
        // Given that I am a user

        // When I am shopping

        // Then I want to indicate that I have put an item in my basket
        shadowItemList.performItemClick(0);
        LinearLayout item = (LinearLayout) itemList.getItemAtPosition(0);
        ImageView actual = (ImageView) item.getChildAt(1);
        assertThat(actual.getDrawable(), is(activity.getResources().getDrawable(R.drawable.checkmark)));
        // And then I want to indicate that i am skipping an Item because I do not want to buy it
        itemList.performLongClick();
        item = (LinearLayout) itemList.getItemAtPosition(0);
        actual = (ImageView) item.getChildAt(1);
        assertThat(actual.getDrawable(), is(activity.getResources().getDrawable(R.drawable.skip)));
        // Then I want to regret that I have skipped an item an unmark it
        itemList.performLongClick();
        item = (LinearLayout) itemList.getItemAtPosition(0);
        actual = (ImageView) item.getChildAt(1);
        assertNull(actual.getDrawable());
        // When I accidentally mark an item as "put in basket"

        // Then I want to undo the action

    }
}
