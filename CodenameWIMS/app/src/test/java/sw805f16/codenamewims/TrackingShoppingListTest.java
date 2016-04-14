package sw805f16.codenamewims;

import android.content.Intent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

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
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class TrackingShoppingListTest {

    StoreMapActivity activity;
    ShoppingListFragment fragment;
    ShadowListView shadowItemList;
    ListView itemList;
    FrameLayout currentItem;
    SearchView search;
    ShadowListView suggestionList;

    @Before
    public void setup() {
        Intent intent = new Intent();
        intent.putExtra("storeId", "56e6a28a28c3e3314a6849df");
        activity = Robolectric.buildActivity(StoreMapActivity.class).withIntent(intent).create().get();
        fragment = (ShoppingListFragment) activity.getFragmentManager().findFragmentByTag("shoppingFragment");
        search = (SearchView) fragment.getView().findViewById(R.id.shopSearch);
        suggestionList = shadowOf((ListView) fragment.getView().findViewById(R.id.suggestions));
        shadowItemList = shadowOf((ListView) fragment.getView().findViewById(R.id.itemList));
        itemList = (ListView) fragment.getView().findViewById(R.id.itemList);
        currentItem = (FrameLayout) fragment.getView().findViewById(R.id.currentItem);

        try {
            String jsonString = activity.getResources().getString(R.string.shop_json);
            JSONObject dummyJson = new JSONObject(jsonString);
            JSONContainer.extractInformationFromJson(dummyJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        search.setQuery("Milk", false);
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowItemList.populateItems();

        search.setQuery("Ost", false);
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
        LinearLayout item = (LinearLayout) currentItem.getChildAt(0);
        ImageView actual = (ImageView) item.getChildAt(1);
        fragment.markCurrentItem(false);
        assertThat(actual.getDrawable(), is(activity.getResources().getDrawable(R.drawable.checkmark)));
        // And then I want to indicate that i am skipping an Item because I do not want to buy it
        item = (LinearItemLayout) currentItem.getChildAt(0);
        actual = (ImageView) item.getChildAt(1);
        fragment.markCurrentItem(true);
        assertThat(actual.getDrawable(), is(activity.getResources().getDrawable(R.drawable.skip)));
        // Then I want to regret that I have skipped an item an unmark it
        item = (LinearItemLayout) itemList.getItemAtPosition(1);
        actual = (ImageView) item.getChildAt(1);
        fragment.markUnmarkItemInAdapter(1, true);
        assertNull(actual.getDrawable());
        // When I accidentally mark an item as "put in basket"
        // Then I want to undo the action
        item = (LinearLayout) itemList.getItemAtPosition(1);
        actual = (ImageView) item.getChildAt(1);
        fragment.markUnmarkItemInAdapter(1, false);
        assertNull(actual.getDrawable());
    }

    @Test
    public void item_sorting_test() {
        search.setQuery("Minced Beef", false);
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowItemList.populateItems();

        fragment.sortItemListInAdapter(new WimsPoints(0, 0));
        TextView actual = (TextView) ((LinearLayout) itemList.getItemAtPosition(0)).getChildAt(0);
        assertThat(actual.getText().toString(), is("Minced Beef"));

        actual = (TextView) ((LinearLayout) itemList.getItemAtPosition(1)).getChildAt(0);
        assertThat(actual.getText().toString(), is("Milk"));

        actual = (TextView) ((LinearLayout) itemList.getItemAtPosition(2)).getChildAt(0);
        assertThat(actual.getText().toString(), is("Ost"));
    }
}
