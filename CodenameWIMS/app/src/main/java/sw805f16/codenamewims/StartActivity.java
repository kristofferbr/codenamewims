package sw805f16.codenamewims;

/**
 * Created by Netray on 01/04/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.Inflater;


public class StartActivity extends Activity {

    HashMap<String, String> stores = new HashMap<>();
    JSONArray json;
    SearchView searchView;
    ListView searchResults;
    ArrayList<String> resultList;
    ArrayAdapter adapter;
    String storeId;
    boolean pickedSuggestion = false;
    Parcelable fragmentState;

    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Removes action bar for start screen.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen); // When refactoring, change to activity_start_screen layoutet.

        if (getIntent().getParcelableExtra("state") != null) {
            fragmentState = getIntent().getParcelableExtra("state");
        }

        //We make a request to the server and receives the list of stores
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://nielsema.ddns.net/sw8/api/store/";

        request(queue, url);

        initializeViews();

        Button storemapButton = (Button) findViewById(R.id.startStoreBtn);
        Button shoppingButton = (Button) findViewById(R.id.startShoppingBtn);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.startSettingsBtn);
        Button exitButton = (Button) findViewById(R.id.startExitBtn);
        Button chooseStoreButton = (Button) findViewById(R.id.startChooseStoreBtn);


        chooseStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAlertDialog();
            }
        });

        storemapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                startActivity(intent);

            }
        });

        shoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShoppingActivity.class);
                startActivity(intent);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    /**
     * This method is responsible for initializing the views on the
     * user interface
     */
    public void initializeViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_choose_store, null, false);

        searchView = (SearchView) layout.findViewById(R.id.dialog_search);
        searchResults = (ListView) layout.findViewById(R.id.dialog_query_results);
        resultList = new ArrayList<>();
        //Here we set up the adapter for the results listview
        adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.simple_list_view,
                resultList);
        searchResults.setAdapter(adapter);

        //When clicking the items the searchview searches for the contents of the item
        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view;
                //If the user has clicked the suggestion item this bool is flipped
                pickedSuggestion = true;
                //We set the query to the text in the item and submit it
                searchView.setQuery(text.getText().toString(), true);
                //searchResults.setVisibility(View.INVISIBLE);
            }
        });

        //When searching we display a listview of suggestions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //If the submit butten is pressed using the listview's onItemClick() method the title is set
                //If it is not the listview is populated like with onQueryTextChange()
                if (pickedSuggestion || stores.containsKey(query.toLowerCase())) {
                    TextView titleText = (TextView) findViewById(R.id.title);
                    titleText.setText(SearchRanking.capitaliseFirstLetters(query));
                    //The search field is emptied
                    searchView.setQuery("", false);
                    storeId = stores.get(query);
                } else {
                    Toast.makeText(StartActivity.this, "No match for: " + query + ". Please pick a suggestion or search for another store", Toast.LENGTH_SHORT).show();
                    populateSuggestionList(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Each time the query text is modified the list of suggestions is updated
                populateSuggestionList(newText);
                searchResults.setVisibility(View.VISIBLE);
                return false;
            }
        });

    }

        /**
         * This method populates the listview with suggestions
         * @param query The query from which to populate after
         */
    private void populateSuggestionList(String query) {
        String key = "";
        //We need to clear the list, otherwise the suggestion list explodes
        resultList.clear();
        //We make an iterator and iterate over the stores hashmap
        Iterator it = stores.entrySet().iterator();
        Map.Entry pair;

        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            key = (String) pair.getKey();

            resultList.add(key);

        }

        //After the immediate matches are added to the list it is sorted by rank
        resultList = SearchRanking.rankSearchResults(query, resultList);

        //Then we notify the adapter that the list is modified
        adapter.notifyDataSetChanged();
    }

    /**
     * This method is used by the test classes to evaluate the stores hashmap
     * @return The stores HashMap of this instance
     */
    public HashMap<String, String> getStores() {
        return stores;
    }


    /**
     * A method that extracts information from a json array and puts it in a hashmap
     * @param jsonArray The json array from the server
     */
    public void extractStoreInformationFromJson(JSONArray jsonArray) {
        try {
            JSONObject tmpObject;
            //Because this method is only called when we have a new JSON array we clear stores
            stores.clear();
            String key = "";

            //Here we loop over the json objects in the json array
            for (int i = 0; i < jsonArray.length(); i++) {
                tmpObject = jsonArray.getJSONObject(i);
                //We extract the storename and the id and place them in a HasMap
                key = SearchRanking.removeSpecialCharacters(tmpObject.getString("name")).toLowerCase();
                stores.put(key, tmpObject.getString("_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /****
     * The function that performs a request against our server
     * @param req The queue to add the request
     * @param url The url to request
     */
    private void request(RequestQueue req, String url){

        JsonArrayRequest jsonRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                extractStoreInformationFromJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(StartActivity.this, "Could not retrieve data from the server", Toast.LENGTH_SHORT).show();
            }
        });

        req.add(jsonRequest);
    }

    private void displayAlertDialog() {

        Context context = StartActivity.this;
        String title = "Choose Store";
        String message = "You are likely to be eaten by a Grue.";
        String button1String = "Choose";
        String button2String = "Cancel";

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        //ad.setMessage(message);
        ad.setView(R.layout.dialog_choose_store); // This only support API 21 and up.


        ad.setPositiveButton(
                button1String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Toast.makeText(StartActivity.this, "Positive Click", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        ad.setNegativeButton(
                button2String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        //Toast.makeText(StartActivity.this, "Negative Click!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        //
        ad.show();

    }
}
