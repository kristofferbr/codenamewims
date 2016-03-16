package sw805f16.codenamewims;

import android.graphics.Bitmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StoreMapActivity extends AppCompatActivity {


    // URL til map /api/store/ID/map
    public boolean isInFront = false;
    public String store_id = "56e6a28a28c3e3314a6849df"; // The ID of føtex! :)
    public String base_url= "http://nielsema.ddns.net/sw8/api/store/";
    RequestQueue rqueue;
    float scale = 1;
    ScaleGestureDetector Scale;
    SearchView search;
    JSONArray products;
    PositionOverlayFactory posfac;
    // Variables for dragging
    FrameLayout fram;
    float xOnStart = 0;
    float yOnStart = 0;
    float posX;
    float posY;
    ListView listResults;
    final ArrayList<String> results = new ArrayList<>();
    ArrayAdapter<String> adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);
        // Set variables for gestures
        Scale = new ScaleGestureDetector(this,new ScaleDetector());
        // Instantiate the Volley request queue
        rqueue = Volley.newRequestQueue(this);

        // Variables used for searching
         adapter = new ArrayAdapter<>(getApplicationContext(),
                                      R.layout.simple_list_view,
                                      results);


        // Instantiate the factory for generating overlays
        posfac = new PositionOverlayFactory(this);

        //The listview in which the results from searches are submittet
        listResults = (ListView) findViewById(R.id.resultView);
        listResults.setAdapter(adapter);

        //Set onclicklisteners on the items that appears in the Listview when searching
        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tes = (TextView)view;
                search.setQuery(tes.getText(),false);
            }
        });

        // Gets the items of the chosen store
        requestItemsOfStore(store_id);

        // Set the correct listeners on the search widget
        search = (SearchView) findViewById(R.id.searchView);
        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listResults.setVisibility(View.VISIBLE);
            }
        });


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int[] res;

                // Sees if the search Query is matching any products from the store
                res = searchProductReturnCoordinates(products, query);

                // If the query matches a product, the resulting location is marked on the map
                if (res[0] != 0) {

                    DrawLocationOnMap(res[0], res[1]);

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                putSuggestionsInList(newText);
                return false;
            }
        });

       fram =(FrameLayout) findViewById(R.id.MapFrame);

        getMapLayout();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isInFront = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        isInFront = false;
    }


    @Override
    public void onBackPressed(){
        if(listResults.getVisibility() == View.VISIBLE){
            listResults.setVisibility(View.INVISIBLE);
        }else{
            super.onBackPressed();
        }

    }

    /****
     * Retrieves the layout from the server
     */
    public void getMapLayout(){

        final String url = base_url + store_id +"/map";
        final ImageView mImageView = (ImageView) findViewById(R.id.storemap);

        /*Generates the request along with a listener that is triggered when image is received*/
        ImageRequest imagereq = new ImageRequest(url, new Response.Listener<Bitmap>() {

            @Override
            public void onResponse(Bitmap bitmap) {
                mImageView.setImageBitmap(bitmap);
            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                        mImageView.setImageResource(R.drawable.prik);
                        error.printStackTrace();
                    }
                });

        rqueue.add(imagereq);
    }


    /***
     * Gets the Item along with its location on the map
     *
     * @param storeID The ID of the store to retrieve the products from
     */
    public void requestItemsOfStore(String storeID){
        String url = base_url + store_id +"/products";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                            String results = "";
                        try {
                            products = response.getJSONArray("products");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        rqueue.add(jsObjRequest);
    }

    /****
     * Function for setting up product along with its coordiantes
     * @param product JSONObject of the product
     * @return a string in the format NAME X: Y:
     */
    public String getProductAndCoordinates(JSONObject product){

        String res ="";

        try {
            JSONObject acpro = product.getJSONObject("product");
            JSONObject location = product.getJSONObject("location");

            res = acpro.getString("name") + ": x;" + location.getString("x") + " y;" + location.getString("y");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;

    }


    /***
     *
     * @param product JSON array of products belonging to the store
     * @param query The search query
     * @return Integer array where res[0] = x and res[1] = y
     */
    public int[] searchProductReturnCoordinates(JSONArray product, String query) {

        JSONObject acpro;
        JSONObject locatio;
        int[] res = {0, 0};

        for (int i = 0; i < product.length(); i++) {

            try {
                acpro = product.getJSONObject(i).getJSONObject("product");
                String productq = acpro.getString("name");

                if (query.equalsIgnoreCase(productq)) {
                    locatio = product.getJSONObject(i).getJSONObject("location");

                    res[0] = locatio.getInt("x");
                    res[1] = locatio.getInt("y");
                    return res;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return res;
    }


    // A needed override in order to be able to zooom... and drag maybe..
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Scale.onTouchEvent(event);

        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            xOnStart = event.getX();
            yOnStart = event.getY();
            posX = fram.getX();
            posY  = fram.getY();
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE)
        {

            float offsetX = xOnStart - event.getX();
            float offsetY = yOnStart - event.getY();
            fram.setX(posX - offsetX);
            fram.setY(posY - offsetY);

        }

        return true;
    }


    /***
     * This class must be in here, for some reason.
     * Can't seperate it.
     */
    public class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener{

        FrameLayout v = (FrameLayout) findViewById(R.id.MapFrame);

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


    /***
     * The function that draws the point on the map
     * @param x The X coordinate
     * @param y The Y coordniate
     */
    public void DrawLocationOnMap(int x, int y){

        FrameLayout fram = (FrameLayout) findViewById(R.id.MapFrame);

        if (fram.getChildCount() == 1) {
            fram.addView(posfac.getPostitionOverlay(x, y));

        } else {
            fram.removeViewAt(1);
            fram.addView(posfac.getPostitionOverlay(x, y));
        }

    }


    /***
     * Function for filling the resultview with suggestions based on the text in the window
     * @param newtext the string that is compared with the product list
     */
    public void putSuggestionsInList(String newtext)
    {
        results.clear();
        String tempName="";

        if(!newtext.equals("")) {

            for (int i = 0; i < products.length(); i++) {

                try {
                    tempName = products.getJSONObject(i).getJSONObject("product").getString("name");


                    if (tempName.toLowerCase().contains(newtext.toLowerCase()) || tempName.equalsIgnoreCase(newtext))
                        results.add(tempName);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        adapter.notifyDataSetChanged();
    }
}