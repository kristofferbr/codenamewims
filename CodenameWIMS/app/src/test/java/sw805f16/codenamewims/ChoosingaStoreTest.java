package sw805f16.codenamewims;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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
    public void choosing_a_store_test(){
        // As a user
        // When I am at the home screen
        // I can tap the search bar
        // Then I can enter the name of a store
        search.setQuery("Føtex", true);

        // I then retreive a list of candidates to choose from

        // As a user
        // When I am at SOME PLACE

        // I can change the store by first tapping SOME BUTTON

        // Then Search

        // Then choose another store

        // Repeat indtil det er implementeret i alle steder i applikationen

    }

    @Test
    public void extract_information_from_json_test() {
        //The string is made into an actual JSONArray and passed to the method
        main.extractInformationFromJson(dummyJson);

        HashMap<String, String> testMap = main.getStores();
        //Here we assert whether it has extracted the name correctly and whether it is the right id
        assertTrue(testMap.containsKey("føtex"));
        assertTrue(testMap.get("føtex").equals("56e6a28a28c3e3314a6849df"));
    }

    @Test
    public void search_result_ranking() {
        main.extractInformationFromJson(dummyJson);
        Iterator testIt = main.getStores().entrySet().iterator();
        ArrayList<String> testValues = new ArrayList<>();
        Map.Entry pair;
        while (testIt.hasNext()) {
            pair = (Map.Entry) testIt.next();
            testValues.add((String) pair.getKey());
        }

        ArrayList<String> testList = SearchRanking.rankSearchResults("føtex", testValues);
        assertThat(testList.get(0), is("føtex"));
        assertTrue(!testList.contains("netto"));

        testList = SearchRanking.rankSearchResults("tex", testValues);
        assertThat(testList.get(0), is("føtex"));
    }

}
