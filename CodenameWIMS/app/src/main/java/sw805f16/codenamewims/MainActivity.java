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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
 * Adresss for server: "nielsema.ddns.net:3000"
 *
 * */

public class MainActivity extends AppCompatActivity {

    HashMap<String, String> stores = new HashMap<>();
    ArrayList<String> rankedResults = new ArrayList<>();
    JSONArray json;

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

    }

    /**
     * This method is used by the test classes to evaluate the stores hashmap
     * @return The stores HashMap of this instance
     */
    public HashMap<String, String> getStores() {
        return stores;
    }

    /**
     * This method is used by the test classes to evaluate the ranked search results list
     * @return The rankedResults ArrayList of this instance
     */
    public ArrayList<String> getRankedSearchResults() {
        return rankedResults;
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

    /**
     * This method ranks the stores after similarity between the query and the keys in the stores HashMap
     * @param query The search query
     */
    public void rankSearchResults(String query) {
        String key = "";
        //We have a local HashMap of storenames and associated rank
        HashMap<String, Double> ranks = new HashMap<>();
        //We use the normalized levenshtein similarity between the strings
        NormalizedLevenshtein similarity = new NormalizedLevenshtein();
        Iterator iterator;

        //We instantiate the iterator with the stores HasMap iterator
        iterator = stores.entrySet().iterator();
        Map.Entry pair;

        //Here we iterate through the stores HashMap
        while(iterator.hasNext()) {
            //We extract the pairs
            pair = (Map.Entry) iterator.next();
            key = (String) pair.getKey();
            //We add the key and the similarity score with the query to the ranks HashMap
            ranks.put(key, similarity.similarity(query, key));
        }

        double highest;

        //Here we use the global store HashMap instead of the local ranks, because they initially are the same size
        for (int i = 0; i < stores.size(); i++) {
            //We re-instantiate the iterator to iterate over ranks HashMap and set the highest value to 0
            iterator = ranks.entrySet().iterator();
            highest = 0;

            //Here we iterate over the ranks HashMap
            while (iterator.hasNext()) {
                pair = (Map.Entry) iterator.next();

                //If the value in the extracted pair is greater than or equal to the highest value and above a threshold
                //The string key is set the pair key and the highest value is updated
                if (((Double) pair.getValue()) >= highest && ((Double) pair.getValue()) > 0.5) {
                    key = (String) pair.getKey();
                    highest = (Double) pair.getValue();
                }
            }
            //If the highest value has not been set and there are more than one entry in the ranks HashMap
            //Then that means there were no meaningful matches for the query
            if (highest == 0 && ranks.size() > 1) {
                Toast.makeText(MainActivity.this, "There was no match to the search", Toast.LENGTH_SHORT).show();
                break;
            }
            //When the storename with the highest similarity with query is found it is added to the list
            rankedResults.add(key);
            //When we are done with the storename it is removed from the ranks HashMap
            ranks.remove(key);
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



    /*Code for Scaling with pinch gestures*/
    /**
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Scale.onTouchEvent(event);
        return true;
    }

    public class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener{


        ImageView v = (ImageView) findViewById(R.id.billede);

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float tempscale = 1;

            tempscale = detector.getScaleFactor() * (scale);


            v.setScaleX(tempscale);
            v.setScaleY(tempscale);

            return super.onScale(detector);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            v.setScaleX(scale);
            v.setScaleY(scale);

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            scale = v.getScaleX();

            super.onScaleEnd(detector);
        }
    }
*/

}
