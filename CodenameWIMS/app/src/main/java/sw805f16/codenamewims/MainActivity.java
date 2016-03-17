package sw805f16.codenamewims;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;


/**
 * Adresss for server: "nielsema.ddns.net/sw8"
 *
 * */

public class MainActivity extends AppCompatActivity {

    HashMap<String, String> stores = new HashMap<>();
    JSONArray json;
    SearchView searchView;
    ListView searchResults;
    ArrayList<String> resultList;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        try {
//            RequestQueue queue = Volley.newRequestQueue(this);
//            String url = "http://nielsema.ddns.net/sw8/api/store";
//
//            request(queue, url);
//            extractInformationFromJson(json);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        searchView = (SearchView) findViewById(R.id.search);
        searchResults = (ListView) findViewById(R.id.query_results);
        resultList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getApplicationContext(),
                                     R.layout.simple_list_view,
                                     resultList);
        searchResults.setAdapter(adapter);

        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view;
                searchView.setQuery(SearchRanking.capitaliseFirstLetter(text.getText().toString()), true);
                searchResults.setVisibility(View.INVISIBLE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                populateSuggestionList(newText);
                searchResults.setVisibility(View.VISIBLE);
                return false;
            }
        });

    }

    private void populateSuggestionList(String query) {
        String key = "";
        resultList.clear();
        Iterator it = stores.entrySet().iterator();
        Map.Entry pair;

        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            key = (String) pair.getKey();
            if (key.contains(query.toLowerCase())) {
                resultList.add(key);
            }
        }

        resultList = SearchRanking.rankSearchResults(query, resultList);

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
     * @throws JSONException Every JSONObject and JSONArray method throws this, therefore This throws it as well
     */
    public void extractInformationFromJson(JSONArray jsonArray) {
        try {
            JSONObject tmpObject;
            //Because this method is only called when we have a new JSON array we clear stores
            stores.clear();

            //Here we loop over the json objects in the json array
            for (int i = 0; i < jsonArray.length(); i++) {
                tmpObject = jsonArray.getJSONObject(i);
                //We extract the storename and the id and place them in a HasMap
                stores.put(tmpObject.getString("name").toLowerCase(), tmpObject.getString("_id"));
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
                json = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Could not retrieve data from the server", Toast.LENGTH_SHORT).show();
            }
        });

        req.add(jsonRequest);
    }

}
