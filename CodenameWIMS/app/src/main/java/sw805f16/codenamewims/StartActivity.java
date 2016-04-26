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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    EditText searchView;
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
        String url = "http://nielsema.ddns.net/sw8dev/api/store/";

        JSONContainer.requestStores(queue, url, getApplicationContext());

        initializeViews();

        Button storemapButton = (Button) findViewById(R.id.startStoreBtn);
        Button shoppingButton = (Button) findViewById(R.id.startShoppingBtn);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.startSettingsBtn);
        Button exitButton = (Button) findViewById(R.id.startExitBtn);
        Button chooseStoreButton = (Button) findViewById(R.id.startChooseStoreBtn);


        chooseStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open(v);
                
                //Replace with code
                storeId = "1";

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

    public void open(View view){
            Dialog dialog=new Dialog(this);
        final LayoutInflater inflater=new LayoutInflater(this){
        @Override
        public LayoutInflater cloneInContext(Context newContext){


        return null;
        }
        };
        dialog.setContentView(R.layout.dialog_choose_store);

        dialog.show();
        }

    /**
     * This method is responsible for initializing the views on the
     * user interface
     */
    public void initializeViews() {
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_choose_store, null, false);

        searchView = (EditText) layout.findViewById(R.id.dialog_search);
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
                searchView.setText(text.getText().toString());
                //searchResults.setVisibility(View.INVISIBLE);
            }
        });

        //When searching we display a listview of suggestions
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                searchResults.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                populateSuggestionList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (pickedSuggestion || stores.containsKey(s.toString().toLowerCase())) {
                    TextView titleText = (TextView) findViewById(R.id.title);
                    titleText.setText(SearchRanking.capitaliseFirstLetters(s.toString()));
                    //The search field is emptied
                    searchView.setText("");
                    storeId = stores.get(s.toString());
                } else {
                    Toast.makeText(StartActivity.this, "No match for: " + s + ". Please pick a suggestion or search for another store", Toast.LENGTH_SHORT).show();
                    populateSuggestionList(s.toString());
                }
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
        Iterator it = JSONContainer.getStores().entrySet().iterator();
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

    private void displayAlertDialog() {

        Context context = StartActivity.this;
        String title = "Choose Store";
        String message = "You are likely to be eaten by a Grue.";
        String button1String = "Choose";
        String button2String = "Cancel";

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        //ad.setMessage(message);
        LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_choose_store, null);
        ad.setView(v);

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
