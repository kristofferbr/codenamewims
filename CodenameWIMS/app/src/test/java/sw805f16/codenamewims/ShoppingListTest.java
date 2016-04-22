package sw805f16.codenamewims;

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
 * Testclass for use case Shopping List
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = OutlineShadow.class)
public class ShoppingListTest {

    JSONObject dummyJson;
    ShoppingListActivity shoppingListActivity;
    ShoppingListFragment testFragment;
    TextView search;
    ShadowListView suggestionList;
    ListView testList;
    ShadowListView shadowTestList;

    @Before
    public void setup() {

        //We set up the activity and the fragment
        shoppingListActivity = Robolectric.setupActivity(ShoppingListActivity.class);
        testFragment = (ShoppingListFragment) shoppingListActivity.getFragmentManager().findFragmentByTag("shoppingFragment");

        //We then pull all the necessary views
        search = (TextView) testFragment.getView().findViewById(R.id.item_textfield);
        suggestionList = (ShadowListView) shadowOf(testFragment.getView().findViewById(R.id.suggestions));
        testList = (ListView) testFragment.getView().findViewById(R.id.itemList);
        shadowTestList = (ShadowListView) shadowOf(testFragment.getView().findViewById(R.id.itemList));

        //This is the test json object from the server
        String jsonString = shoppingListActivity.getResources().getString(R.string.shop_json);

        try {
            dummyJson = new JSONObject(jsonString);
            //We extract the information from the JSONObject to fill the products HashMap
            JSONContainer.extractProductInformationFromJson(dummyJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    //As a user
    // I want to be able to add/delete items to/from my shopping list
    public void shopping_list(){
        // Given I am a user
        // And I am in my shopping list

        // Then I want to be able to add items to my list

        //We set the query, pick the first item, and populate the item list
        search.setText("Milk");
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowTestList.populateItems();

        //We assert whether the first item in the list is "Milk"
        LinearLayout tmpLayout = (LinearLayout) testList.getItemAtPosition(0);
        TextView actual = (TextView) tmpLayout.getChildAt(0);
        assertThat(actual.getText().toString(), is("Milk"));

        // And then I want to be able to delete items from my list.

        //Because the delete method is a fling gesture it is difficult to test, therefor we test that on the phone
    }

    /*@Test
    public void change_from_shopping_list_to_start_screen() {
        Button testButton = (Button) testFragment.getView().findViewById(R.id.startScreenButton);

        testButton.performClick();
        Intent shadowIntent = shadowOf(shoppingListActivity).peekNextStartedActivity();
        //We assert that the MainActivity is started
        assertThat(MainActivity.class.getCanonicalName(), is(shadowIntent.getComponent().getClassName()));
    }

    @Test
    public void change_from_shopping_list_to_storemap() {
        Button testButton = (Button) testFragment.getView().findViewById(R.id.shopStoreButton);

        testButton.performClick();
        Intent shadowIntent = shadowOf(shoppingListActivity).peekNextStartedActivity();
        assertThat(StoreMapActivity.class.getCanonicalName(), is(shadowIntent.getComponent().getClassName()));
    }*/

    @Test
    public void items_change_color() {
        //We place the item "Milk" in the item list
        search.setText("Milk");
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowTestList.populateItems();

        try {
            //Here we have taken a new JSONObject, where there are no products
            String newJsonString = "{\n" +
                    "  \"_id\": \"56e6a28a28c3e3314a6849e0\",\n" +
                    "  \"products\": []\n" +
                    "}";
                JSONObject newJson = new JSONObject(newJsonString);
            JSONContainer.extractProductInformationFromJson(newJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Then we set the store id to the new id
        testFragment.setStoreId("56e6a28a28c3e3314a6849e0");

        LinearItemLayout actual = (LinearItemLayout) testList.getItemAtPosition(0);
        //Lastly we assert whether the background of the items have been grayed out
        assertThat(actual.getBackground(), is(shoppingListActivity.getResources().getDrawable(R.drawable.grayout)));
    }

}
