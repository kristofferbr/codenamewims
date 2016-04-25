package sw805f16.codenamewims;

import android.content.Intent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
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
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class ViewShoppingListTest {

    ShoppingListFragment fragment;
    ShoppingListActivity shoppingActivity;

    @Before
    public void setup() {
        shoppingActivity = Robolectric.setupActivity(ShoppingListActivity.class);
        fragment = (ShoppingListFragment) shoppingActivity.getFragmentManager().findFragmentByTag("shoppingFragment");
        EditText search = (EditText) fragment.getView().findViewById(R.id.item_textfield);
        ShadowListView suggestionList = shadowOf((ListView) fragment.getView().findViewById(R.id.suggestions));
        ShadowListView itemList = shadowOf((ListView) fragment.getView().findViewById(R.id.itemList));

        try {
            String jsonString = shoppingActivity.getResources().getString(R.string.shop_json);
            JSONObject dummyJson = new JSONObject(jsonString);
            JSONContainer.extractProductInformationFromJson(dummyJson,shoppingActivity);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        search.setText((CharSequence)"Milk");
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        itemList.populateItems();
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
        FrameLayout currentItem = (FrameLayout) fragment.getView().findViewById(R.id.currentItem);
        LinearLayout item = (LinearLayout) currentItem.getChildAt(0);
        TextView actual = (TextView) item.getChildAt(0);

        // Then I want to see my shopping list without going away from the map

        assertThat(actual.getText().toString(), is("Milk"));
    }
}
