package sw805f16.codenamewims;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Button;
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
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by kbrod on 09/03/2016.
 * Never made it as a wise man, never made it as a poor man stealing and this is how you remind me
 * of where you really are
 * (Talking to bug)
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ViewShoppingListTest {

    ShoppingListFragment fragment;
    ShoppingListActivity shoppingActivity;

    @Before
    public void setup() {
        shoppingActivity = Robolectric.setupActivity(ShoppingListActivity.class);
        fragment = (ShoppingListFragment) shoppingActivity.getFragmentManager().findFragmentByTag("shoppingFragment");
        SearchView search = (SearchView) fragment.getView().findViewById(R.id.shopSearch);
        ShadowListView suggestionList = shadowOf((ListView) fragment.getView().findViewById(R.id.suggestions));
        ShadowListView itemList = shadowOf((ListView) fragment.getView().findViewById(R.id.itemList));
        Button storeButton = (Button) fragment.getView().findViewById(R.id.shopStoreButton);

        try {
            String jsonString = shoppingActivity.getResources().getString(R.string.shop_json);
            JSONObject dummyJson = new JSONObject(jsonString);
            fragment.extractInformationFromJson(dummyJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        search.setQuery("Milk", false);
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        itemList.populateItems();

        storeButton.performClick();
    }

    @Test
    // As a user
    // I want to be able to see my shopping list
    // so that I see what items to buy
    public void view_shopping_list(){
        // Given that I am a user

        // When I am at the storemap and shopping
        Intent intent = shadowOf(shoppingActivity).getNextStartedActivity();
        StoreMapActivity activity = Robolectric.buildActivity(StoreMapActivity.class).withIntent(intent).create().get();
        fragment = (ShoppingListFragment) activity.getFragmentManager().findFragmentByTag("shoppingFragment");
        ListView itemList = (ListView) fragment.getView().findViewById(R.id.itemList);
        LinearLayout item = (LinearLayout) itemList.getItemAtPosition(0);
        TextView actual = (TextView) item.getChildAt(0);

        // Then I want to see my shopping list without going away from the map

        assertThat(actual.getText().toString(), is("Milk"));
    }
}
