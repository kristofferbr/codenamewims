package sw805f16.codenamewims;

import android.content.Intent;
import android.gesture.Gesture;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
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
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowGestureDetector;
import org.robolectric.shadows.ShadowListView;
import org.robolectric.shadows.ShadowMotionEvent;
import static org.robolectric.Shadows.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by kbrod on 09/03/2016.
 * Testclass for use case Shopping List
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ShoppingListTest {

    JSONObject dummyJson;
    ShoppingListActivity shoppingListActivity;
    ShoppingListFragment testFragment;
    SearchView search;
    ShadowListView suggestionList;
    ListView testList;
    ShadowListView shadowTestList;

    @Before
    public void setup() {

        shoppingListActivity = Robolectric.setupActivity(ShoppingListActivity.class);
        testFragment = (ShoppingListFragment) shoppingListActivity.getFragmentManager().findFragmentByTag("shoppingFragment");

        search = (SearchView) testFragment.getView().findViewById(R.id.shopSearch);
        suggestionList = (ShadowListView) shadowOf(testFragment.getView().findViewById(R.id.suggestions));
        testList = (ListView) testFragment.getView().findViewById(R.id.itemList);
        shadowTestList = (ShadowListView) shadowOf(testFragment.getView().findViewById(R.id.itemList));

        String jsonString = "{\n" +
                "  \"_id\": \"56e6a28a28c3e3314a6849df\",\n" +
                "  \"products\": [\n" +
                "    {\n" +
                "      \"_id\": \"56e6a9f028c3e3314a6849ea\",\n" +
                "      \"product\": {\n" +
                "        \"_id\": \"56e6a29528c3e3314a6849e2\",\n" +
                "        \"name\": \"Milk\",\n" +
                "        \"description\": \"Put it on cereal, drink it, or take a bath. Do whatever you want with it, you baught it!\"\n" +
                "      },\n" +
                "      \"__v\": 0,\n" +
                "      \"location\": {\n" +
                "        \"x\": 250,\n" +
                "        \"y\": 18\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"_id\": \"56e6aa1b28c3e3314a6849eb\",\n" +
                "      \"product\": {\n" +
                "        \"_id\": \"56e6a29528c3e3314a6849e1\",\n" +
                "        \"name\": \"Minced Beef\",\n" +
                "        \"description\": \"Slaughtered cow cut into tiny pieces\"\n" +
                "      },\n" +
                "      \"__v\": 0,\n" +
                "      \"location\": {\n" +
                "        \"x\": 125,\n" +
                "        \"y\": 90\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"_id\": \"56efb5d1aa6ece882b08b437\",\n" +
                "      \"product\": {\n" +
                "        \"_id\": \"56efb40344a68c0606122a3c\",\n" +
                "        \"name\": \"Ost\",\n" +
                "        \"description\": \"Det lugter\",\n" +
                "        \"__v\": 0\n" +
                "      },\n" +
                "      \"__v\": 0,\n" +
                "      \"location\": {\n" +
                "        \"x\": 400,\n" +
                "        \"y\": 300\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try {
            dummyJson = new JSONObject(jsonString);
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

        search.setQuery("Milk", true);
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowTestList.populateItems();

        TextView actual = (TextView) testList.getItemAtPosition(0);
        assertThat(actual.getText().toString(), is("Milk"));

        // And then I want to be able to delete items from my list.
        swipeItem(shadowTestList);

        ShadowAlertDialog testDialog = shadowOf(ShadowAlertDialog.getLatestAlertDialog());
        assertNotNull(testDialog);

        testDialog.clickOnItem(1);
        assertNotNull(shadowTestList.findItemContainingText("Milk"));

        swipeItem(shadowTestList);

        testDialog = shadowOf(ShadowAlertDialog.getLatestAlertDialog());
        testDialog.clickOnItem(0);
        assertNull(shadowTestList.findItemContainingText("Milk"));

    }

    @Test
    public void change_from_shopping_list_to_start_screen() {
        Button testButton = (Button) testFragment.getView().findViewById(R.id.startScreenButton);
        Intent shadowIntent = shadowOf(shoppingListActivity).peekNextStartedActivity();

        testButton.performClick();
        assertThat(MainActivity.class.getCanonicalName(), is(shadowIntent.getComponent().getClassName()));
    }

    @Test
    public void change_from_shopping_list_to_storemap() {
        Button testButton = (Button) testFragment.getView().findViewById(R.id.shopStoreButton);
        Intent shadowIntent = shadowOf(shoppingListActivity).peekNextStartedActivity();

        testButton.performClick();
        assertThat(StoreMapActivity.class.getCanonicalName(), is(shadowIntent.getComponent().getClassName()));
    }

    @Test
    public void items_change_color() {
        search.setQuery("Milk", true);
        suggestionList.populateItems();
        suggestionList.performItemClick(0);
        shadowTestList.populateItems();

        testFragment.setStoreId("56e6a28a28c3e3314a6849e0");

        TextView actual = (TextView) testList.getItemAtPosition(0);
        assertThat(actual.getBackground(), is(shoppingListActivity.getResources().getDrawable(R.drawable.grayout)));
    }


    private void swipeItem(ShadowListView shadowList) {
        MotionEvent testEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 500, 200, 0);
        shadowList.onTouchEvent(testEvent);
//        testEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 400, 200, 0);
//        shadowTestList.onTouchEvent(testEvent);
        testEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 300, 200, 0);
        shadowList.onTouchEvent(testEvent);
    }

}
