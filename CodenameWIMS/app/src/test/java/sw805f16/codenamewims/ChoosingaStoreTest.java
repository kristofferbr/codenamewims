package sw805f16.codenamewims;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowListView;
import org.robolectric.shadows.ShadowView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import static org.robolectric.Shadows.*;

/**
 * Created by kbrod on 09/03/2016.
 * TEstclassssss
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ChoosingaStoreTest {
    MainActivity main;
    SearchView search;
    JSONArray dummyJson;

    @Before
    public void setup() {
        main = Robolectric.setupActivity(MainActivity.class);
        search = (SearchView) main.findViewById(R.id.search);

        //This is a JSON array, in string format, we want to extract information from
        String jsonString = "[\n" +
                "  {\n" +
                "    \"_id\": \"56e6a28a28c3e3314a6849df\",\n" +
                "    \"name\": \"Føtex\",\n" +
                "    \"description\": \"Føtex er sej vi gør mere for dig\",\n" +
                "    \"map\": \"56e6a32e28c3e3314a6849e3\",\n" +
                "    \"__v\": 1,\n" +
                "    \"products\": [\n" +
                "      \"56e6a9f028c3e3314a6849ea\",\n" +
                "      \"56e6aa1b28c3e3314a6849eb\"\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"56e6a28a28c3e3314a6849e0\",\n" +
                "    \"name\": \"Netto\",\n" +
                "    \"description\": \"Netto – Derfor\",\n" +
                "    \"products\": []\n" +
                "  }\n" +
                "]";
        try {
            dummyJson = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    // As a user
    // I want to be able to choose the store
    // so that I can retrieve the correct map and item list
    public void choosing_a_store_test() throws Exception {
        // Given I am a user
        // When I am at the home screen
        // And I search for the name of a store
        main.extractInformationFromJson(dummyJson);
        //We search for føtex
        search.setQuery("Føtex", false);

        // Then I retreive a list of candidates to choose from

        //We first find the listview that shows when the text in the searchbar is changed
        ListView results = (ListView) main.findViewById(R.id.query_results);
        //We also instantiate a shadow of the list view, because it has the populateItems() method
        //that we use to trigger the events and the adapter
        ShadowListView shadowResults = (ShadowListView) shadowOf(main.findViewById(R.id.query_results));
        shadowResults.populateItems();

        //First we assert whether the listview becomes visible when the text is changed
        assertThat(results.getVisibility(), is(View.VISIBLE));

        //Next we assert whether the first item in the list is føtex
        String text = (String) results.getItemAtPosition(0);
        assertThat(text, is("føtex"));

        //Next we want to see whether, when clicking the first item, that the title text is Føtex,
        //whether the listview turns invisible again and if the searchbar is empty
        shadowResults.performItemClick(0);
        TextView testText = (TextView) main.findViewById(R.id.title);
        assertThat(testText.getText().toString(), is("Føtex"));
        assertThat(results.getVisibility(), is(View.INVISIBLE));
        assertThat(search.getQuery().toString(), is(""));

        testText.setText("");
        search.setQuery("Føtex", true);
        assertThat(testText.getText().toString(), is("Føtex"));
    }

    @Test
    public void extract_information_from_json_test() throws Exception {
        //The string is passed to the method
        main.extractInformationFromJson(dummyJson);

        HashMap<String, String> testMap = main.getStores();
        //Here we assert whether it has extracted the name correctly and whether it is the right id
        assertTrue(testMap.containsKey("føtex"));
        assertTrue(testMap.get("føtex").equals("56e6a28a28c3e3314a6849df"));
    }

    @Test
    public void search_result_ranking() throws Exception {
        //We pass the json array to the extract method
        main.extractInformationFromJson(dummyJson);
        //We make an iterator to go through the stores HashMap in MainActvity
        Iterator testIt = main.getStores().entrySet().iterator();
        ArrayList<String> testValues = new ArrayList<>();
        Map.Entry pair;
        //We extract the keys and put them in the test array
        while (testIt.hasNext()) {
            pair = (Map.Entry) testIt.next();
            testValues.add((String) pair.getKey());
        }

        //Then we rank the query føtex with the list of strings
        ArrayList<String> testList = SearchRanking.rankSearchResults("føtex", testValues);
        //We assert that highest ranking result is føtex and that netto is not in the list
        assertThat(testList.get(0), is("føtex"));
        assertFalse(testList.contains("netto"));

        //Here we try it if the user dit not input the entire query
        testList = SearchRanking.rankSearchResults("fø", testValues);
        assertThat(testList.get(0), is("føtex"));

        //Here we see whether the capitalise method works
        String testString = SearchRanking.capitaliseFirstLetters("føtex");
        assertThat(testString, is("Føtex"));

        //Then we test whether the removeSpecialCharacters() method works
        testString = SearchRanking.removeSpecialCharacters("føtex - aalborg øst");
        assertThat(testString, is("føtex aalborg øst"));
        //And if the capitaliseFirstLetters() method can handle multiple words
        testString = SearchRanking.capitaliseFirstLetters(testString);
        assertThat(testString, is("Føtex Aalborg Øst"));
    }

    @Test
    public void change_from_start_to_storemap() throws Exception {
        main.extractInformationFromJson(dummyJson);
        Button testButton = (Button) main.findViewById(R.id.storemapbutton);

        //We search for føtex and submit the search
        search.setQuery("føtex", true);
        //We click the button and assert whether it starts the right activity
        testButton.performClick();
        //We peek at the next activity that starts from main
        Intent intent = shadowOf(main).peekNextStartedActivity();
        assertThat(StoreMapActivity.class.getCanonicalName(), is(intent.getComponent().getClassName()));
        //We also check whether the extra with the store id is the right id
        assertThat(intent.getStringExtra("storeId"), is("56e6a28a28c3e3314a6849df"));
    }

    @Test
    public void change_from_start_to_shopping_list() throws Exception {
        main.extractInformationFromJson(dummyJson);
        Button testButton = (Button) main.findViewById(R.id.shoppingListButton);

        //We search for føtex and submit the search
        search.setQuery("føtex", true);
        //We click the button and assert whether it starts the right activity
        testButton.performClick();
        //We peek at the next activity that starts from main
        Intent intent = shadowOf(main).peekNextStartedActivity();
        assertThat(ShoppingListActivity.class.getCanonicalName(), is(intent.getComponent().getClassName()));
        //We also check whether the extra with the store id is the right id
        assertThat(intent.getStringExtra("storeId"), is("56e6a28a28c3e3314a6849df"));
    }
}
