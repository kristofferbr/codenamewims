package sw805f16.codenamewims;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Netray on 07/04/2016.
 */
public class StoreActivity extends WimsActivity {

    // URL til map /api/store/ID/map
    public boolean isInFront = false;
    public boolean createMapDataModePoints = false;
    public boolean createMapDataModeNeighbors = false;
    public boolean fingerpriting = false;
    public boolean isScanning = false;
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

    ArrayList<WimsPoints> mapData = new ArrayList<>();
    WimsPoints currentWimsPoint;


    /*For drawing neighbors*/
    int startX=0;
    int starty=0;
    int endX =0;
    int endY =0;
    boolean start = true;
    private Toolbar toolbar;


    ShoppingListFragment fragment;

    /* Used for fingerprinting*/
    Thread fingerthread;
    HashMap fingerPrint=new HashMap(3);
    WifiFingerprinter fingerprinter;
    static Handler mHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store); // When refactoring, change to activity_start_screen layoutet.

        this.store_id = getIntent().getStringExtra("storeId");
        fragment = ShoppingListFragment.newInstance(store_id);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (getIntent().getParcelableExtra("state") != null) {
            Fragment.SavedState state = getIntent().getParcelableExtra("state");
            fragment.setInitialSavedState(state);
        }
        transaction.add(R.id.storeShoppingList, fragment, "shoppingFragment");
        transaction.commit();

        fingerprinter = new WifiFingerprinter(getApplicationContext());

        // Set variables for gestures
        Scale = new ScaleGestureDetector(this,new ScaleDetector());

        // Instantiate the Volley request queue
        rqueue = Volley.newRequestQueue(this);

        // Adapter used for searching
        adapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.simple_list_view,
                results);

        fram = (FrameLayout) findViewById(R.id.MapFrame);

        fram.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!createMapDataModePoints && !createMapDataModeNeighbors) {
                    Scale.onTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        xOnStart = event.getRawX();
                        yOnStart = event.getRawY();
                        posX = fram.getX();
                        posY = fram.getY();
                    }

                    if (event.getAction() == MotionEvent.ACTION_MOVE) {

                        float offsetX = xOnStart - event.getRawX();
                        float offsetY = yOnStart - event.getRawY();
                        fram.setX(posX - offsetX);
                        fram.setY(posY - offsetY);

                    }
                    return true;
                }

                return false;
            }
        });

        //The listview in which the results from searches are submitted
        listResults = (ListView) findViewById(R.id.resultView);
        listResults.setAdapter(adapter);
        //Set onclicklisteners on the items that appears in the Listview when searching
        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tes = (TextView) view;
                search.setQuery(tes.getText(), false);
            }
        });

        ImageView mImageView = (ImageView) findViewById(R.id.storemap);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (createMapDataModePoints) {

                    int spotX;
                    int spotY;

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        float x = event.getX() - v.getLeft();
                        float y = event.getY() - v.getTop();

                        int w = v.getMeasuredWidth();
                        int h = v.getMeasuredHeight();

                        Log.w("Billedeopløsning", "X: " + fram.getChildAt(0).getMeasuredWidth() + "Y: " + fram.getChildAt(0).getMeasuredHeight());

                        spotX = (int) (1312 / (float) w * x);
                        spotY = (int) (2132 / (float) h * y);
                        if (fram.getChildCount() == 1) {
                            fram.addView(posfac.getPostitionOverlay(spotX, spotY));
                            addPointIfNew(spotX, spotY, mapData);

                        } else {
                            if (addPointIfNew(spotX, spotY, mapData) && !fingerpriting) {
                                ImageView temp = (ImageView) fram.getChildAt(1);
                                fram.removeViewAt(1);
                                fram.addView(posfac.getBitMapReDrawnSpot(temp, spotX, spotY));
                            } else {
                                if (fram.getChildCount() == 2) {
                                    currentWimsPoint = getWithin(spotX, spotY, mapData);
                                    fram.addView(posfac.getPositionOfFingerPrintPoint((int) currentWimsPoint.x, (int) currentWimsPoint.y));
                                    fingerpriting = true;
                                    //FingerPrintPutton.setVisibility(View.VISIBLE);
                                } else {
                                    fram.removeViewAt(2);
                                    fingerpriting = false;
                                    //FingerPrintPutton.setVisibility(View.INVISIBLE);
                                }

                            }


                        }

                    }

                } else if (createMapDataModeNeighbors && !fingerpriting) {


                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (start) {
                            float x = event.getX() - v.getLeft();
                            float y = event.getY() - v.getTop();

                            int w = v.getMeasuredWidth();
                            int h = v.getMeasuredHeight();

                            startX = (int) (1312 / (float) w * x);
                            starty = (int) (2132 / (float) h * y);

                            if (isWithin(startX, starty, mapData)) {
                                start = !start;
                            }
                        } else {
                            float x = event.getX() - v.getLeft();
                            float y = event.getY() - v.getTop();

                            int w = v.getMeasuredWidth();
                            int h = v.getMeasuredHeight();

                            endX = (int) (1312 / (float) w * x);
                            endY = (int) (2132 / (float) h * y);


                            if (isWithin(endX, endY, mapData)) {
                                WimsPoints startpoint = getWithin(startX, starty, mapData);
                                WimsPoints endpoint = getWithin(endX, endY, mapData);

                                ImageView temp = (ImageView) fram.getChildAt(1);
                                fram.removeViewAt(1);
                                fram.addView(posfac.getBitMapReDrawnLine(temp, (int) startpoint.x, (int) startpoint.y, (int) endpoint.x, (int) endpoint.y));
                                start = !start;
                                setNeighbors(startX, starty, endX, endY, mapData);
                            }

                        }
                    }

                }

                return false;
            }
        });

        // Instantiate the factory for generating overlays
        posfac = new PositionOverlayFactory(this);

        // Gets the items of the chosen store
        requestItemsOfStore(store_id);

        // Set the correct listeners on the search widget
        search = (SearchView) findViewById(R.id.store_search_field);
        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listResults.setVisibility(View.VISIBLE);
            }
        });

        //Listeners on the search widget
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int[] res;

                // Sees if the search Query is matching any products from the store
                res = searchProductReturnCoordinates(products, query);

                // If the query matches a product, the resulting location is marked on the map
                if (res[0] != 0) {

                    //DrawLocationOnMap(res[0], res[1]);
                    drawRoute(res);

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                putSuggestionsInList(newText);
                return false;
            }
        });


        // Gets the map corresponding to the store ID
        fram =(FrameLayout) findViewById(R.id.MapFrame);

        getMapLayout();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(getApplicationContext(), "SCANNINGS COMPLETE", Toast.LENGTH_LONG).show();

            }
        };


        WimsButton findMe = new WimsButton(getApplicationContext(), getResources().getDrawable(R.drawable.no_icon));
        addWimsButtonToActionBar(findMe, RIGHT);
        findMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = fram.getChildCount() - 1;
                for (int i = count; i > 0; i--) {
                    fram.removeViewAt(i);
                }

                WimsPoints location = new WimsPoints();
                location = findNearestNeighbor(mapData);

                if (location != null)
                    fram.addView(posfac.getPositionOfFingerPrintPoint((int) location.x, (int) location.y));
                else
                    Toast.makeText(getApplicationContext(), "No locatioj found", Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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


    public void sendMapData(ArrayList<WimsPoints> mapData)
    {

        JSONObject tosend = constructJson(mapData);
        Log.w("WIMS", tosend.toString());
        String url = "http://nielsema.ddns.net/sw8/api/point/bulk";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, tosend, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONArray points = response.getJSONArray("ops");
                            Log.w("WIMS",points.toString());
                            sendMapDataWithNeighbors(points);

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

    public void sendMapDataWithNeighbors(JSONArray res){

        JSONObject tosend;
        tosend = constructJSONaddNeighbors(res,mapData);


        String url = "http://nielsema.ddns.net/sw8/api/point/bulk";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, tosend, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        addReferencesToStore(store_id);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        rqueue.add(jsObjRequest);
    }

    public void addReferencesToStore(String StoreID){

        String url = base_url + StoreID;

        JSONObject references = new JSONObject();
        JSONArray ids = new JSONArray();

        for(int i = 0; i < mapData.size(); i++){
            ids.put(mapData.get(i).ID);
        }

        try {
            references.put("points", ids);
        } catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, references, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        rqueue.add(jsObjRequest);
    }


    public void requestMapData(String storeID){

        String url = base_url + storeID +"/products";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String results = "";
                        try {

                            JSONArray points = response.getJSONArray("ops");
                            mapData = deConstructJSON(points);

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
        String url = base_url + storeID +"/products";
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



    /***
     * This class must be in here, for some reason.
     * Can't seperate it.
     */
    public class ScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener{

        FrameLayout v = (FrameLayout) findViewById(R.id.MapFrame);

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float tempscale;

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

        if(products != null) {

            if (!newtext.equals("")) {

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
        } else {
            search.setQueryHint("No items found..");
        }
        adapter.notifyDataSetChanged();
    }

    /***TODO
     * Make mapdata as a parameter
     */
    /****
     * Function used to add an item to the MapData for use of PathDrawing
     * @param itemToAdd The point to add in the data
     */
    public void addItemToMapDataAndDrawRoute(WimsPoints itemToAdd){

        int indexOfNeighbor = 0;
        float distance = itemToAdd.distance(mapData.get(0).x, mapData.get(0).y);
        float tempDist;



        for(int i = 1; i < mapData.size(); i++){
            tempDist = itemToAdd.distance(mapData.get(i).x, mapData.get(i).y);

            if(tempDist < distance){
                indexOfNeighbor = i;
                distance = tempDist;
            }
        }

        itemToAdd.Neighbours.add(mapData.get(indexOfNeighbor));
        mapData.get(indexOfNeighbor).Neighbours.add(itemToAdd);
        mapData.add(itemToAdd);


        if (fram.getChildCount() == 1) {
            fram.addView(posfac.getRouteBetweenTwoPoints(mapData.get(0), itemToAdd));

        } else {
            fram.removeViewAt(1);
            fram.addView(posfac.getRouteBetweenTwoPoints(mapData.get(0), itemToAdd));
        }

        itemToAdd.Neighbours.remove(mapData.get(indexOfNeighbor));
        mapData.get(indexOfNeighbor).Neighbours.remove(itemToAdd);
        mapData.remove(itemToAdd);
    }


    /***
     * The main function used when drawing a route
     * As of now the route is drawn from the entrance of the store
     * i.e. the first point in the dataset
     * @param location int[0] = x, int[1] ) y
     */
    public void drawRoute(int[] location){

        WimsPoints ItemToReach = new WimsPoints(location[0],location[1]);

        addItemToMapDataAndDrawRoute(ItemToReach);
    }

    /****
     * Function used when drawing the map data. This function adds the new point to the mapdata
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param pointarray The array to add the point to
     * @return
     */
    public boolean addPointIfNew(int x, int y, ArrayList<WimsPoints> pointarray){

        if(!isWithin(x,y,pointarray)){
            mapData.add(new WimsPoints(x, y));
            return true;
        }

        return false;
    }

    /****
     * Function that checks whether or not two coordinates belong to a point already
     * @param x X coordinate
     * @param y Y coordinate
     * @param pointArray The arrary of points to check against
     * @return true if point is within, false if not.
     */
    private boolean isWithin(int x, int y,ArrayList<WimsPoints> pointArray){

        boolean within = false;
        for(int i = 0; i<pointArray.size();i++){

            if(x < pointArray.get(i).x+25 && x > pointArray.get(i).x -25
                    && y < pointArray.get(i).y+25 && y > pointArray.get(i).y - 25){
                within = true;
            }

        }

        return within;
    }

    /***
     * returns the point that is within the two coordinates
     * @param x coordinate
     * @param y coordinate
     * @param pointArray The array to retreive the point from
     * @return
     */
    private WimsPoints getWithin(int x, int y, ArrayList<WimsPoints> pointArray){

        for(int i = 0; i<pointArray.size();i++){

            if(x < pointArray.get(i).x+25 && x > pointArray.get(i).x -25
                    && y < pointArray.get(i).y+25 && y > pointArray.get(i).y - 25){
                return pointArray.get(i);
            }

        }

        return pointArray.get(0);
    }


    /***
     * Function used to set the neighbor relationsship between two points
     * @param point1x x Coordinate of point1
     * @param point1y y coordinate of point1
     * @param point2x x coordinate of point2
     * @param point2y y coordinate of point2
     * @param pointlist The array of points
     * @return true if neighborhood is set, false if not
     */
    public boolean setNeighbors(int point1x, int point1y, int point2x, int point2y, ArrayList<WimsPoints> pointlist){

        if(getWithin(point1x,point1y,pointlist) != getWithin(point2x,point2y,pointlist)) {
            getWithin(point1x, point1y,pointlist).Neighbours.add(getWithin(point2x, point2y,pointlist));
            getWithin(point2x, point2y,pointlist).Neighbours.add(getWithin(point1x, point1y,pointlist));
            return true;
        } else
            return false;
    }


    /***
     * Function that constructs a JSON array of points to insert into the server database
     * @param point The array of points representing the map data.
     * @return The JSON Array
     */
    public JSONObject constructJson(ArrayList<WimsPoints> point){

        JSONObject tosend = new JSONObject();
        JSONArray pointArray = new JSONArray();
        JSONObject Jsonpointdata;
        JSONArray neighbors;
        JSONArray fingerprint;


        try {
            for(int i = 0; i < point.size(); i++)
            {

                Jsonpointdata = new JSONObject();
                neighbors = new JSONArray();
                fingerprint = new JSONArray();
                //Jsonpointdata.put("_id",i);
                Jsonpointdata.put("x",point.get(i).x);
                Jsonpointdata.put("y",point.get(i).y);


                if(point.get(i).fingerprint != null) {
                    Iterator it = point.get(i).fingerprint.entrySet().iterator();
                    if (it.hasNext()) {
                        while (it.hasNext()) {
                            HashMap.Entry pair = (HashMap.Entry) it.next();
                            JSONObject print = new JSONObject();
                            print.put(pair.getKey().toString(), pair.getValue());
                            fingerprint.put(print);
                        }
                    }
                }
                Jsonpointdata.put("neighbors",neighbors);
                Jsonpointdata.put("fingerprint",fingerprint);
                pointArray.put(Jsonpointdata);


            }
            tosend.put("points", pointArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return tosend;

    }


    /***
     * Function that adds neighbor relations to the JSON object after ID's have been added to each
     * point
     * @param points the Array that contains the point data with ID's and no neighbor relations
     * @param mapData The mapdata represented in Arraylist that has neighbors represented
     * @return the modified JSON array containing neighbors
     */
    public JSONObject constructJSONaddNeighbors(JSONArray points, ArrayList<WimsPoints> mapData){

        JSONObject tosend = new JSONObject();
        JSONArray res = new JSONArray();

        JSONArray neighbors;
        JSONObject workingPoint;
        WimsPoints WIMSpoint;

        try {

            for (int i = 0; points.length() > i; i++) {
                workingPoint = points.getJSONObject(i);
                WIMSpoint = getWithin(workingPoint.getInt("x"), workingPoint.getInt("y"), mapData);
                WIMSpoint.ID = workingPoint.getString("_id");
                neighbors = new JSONArray();

                for(int y = 0; y < WIMSpoint.Neighbours.size(); y++) {
                    String res_id = getIDfromJSON(points, WIMSpoint.Neighbours.get(y));
                    neighbors.put(res_id);
                }
                workingPoint.put("neighbors",neighbors);
                res.put(workingPoint);
            }

            tosend.put("points", res);

        } catch (JSONException e){
            e.printStackTrace();
        }

        return tosend;

    }

    /***
     * Function that gets the ID of the point represented in the JSON array
     * @param pointarray The JSONArrayof points
     * @param point The Point that the ID should be returned from
     * @return The ID
     */
    public String getIDfromJSON(JSONArray pointarray, WimsPoints point){

        try {
            for (int i = 0; i < pointarray.length(); i++) {

                if (point.x == pointarray.getJSONObject(i).getInt("x") && point.y == pointarray.getJSONObject(i).getInt("y")){
                    return pointarray.getJSONObject(i).getString("_id");
                }


            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return "";
    }


    /***
     * Function the takes the JSON object and deconstructs into a WimsPoints Array that are used
     * For pathfinding
     * @param array The array retrieved from the server
     * @return The arraylist of data
     */
    public ArrayList<WimsPoints> deConstructJSON(JSONArray array){

        ArrayList<WimsPoints> mapdata = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                mapdata.add(new WimsPoints(array.getJSONObject(i).getInt("x"), array.getJSONObject(i).getInt("y")));
            }
            for(int i = 0;i<array.length();i++ ){
                // Get Neighbor data
                JSONArray neighbors;
                neighbors = array.getJSONObject(i).getJSONArray("neighbors");

                for(int x = 0; x<neighbors.length();x++){
                    mapdata.get(i).Neighbours.add(mapdata.get(indexOfNeighbor(mapdata,array,neighbors.getString(x))));
                }

            }


        }
        catch (JSONException e){
            e.printStackTrace();
        }
        return mapdata;
    }


    /***
     * returns the index of the neighbor used for deconstructing the
     * @param mapdata The mapdata without Neighbor data
     * @param jsonArray The Array
     * @param id The ID of the point in the JSON array
     * @return the index of the neighbor in the arraylist
     */
    public int indexOfNeighbor(ArrayList<WimsPoints> mapdata,JSONArray jsonArray, String id ){

        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                if (id.equals(jsonArray.getJSONObject(i).getString("_id"))) {

                    for(int x = 0; x<mapdata.size(); x++){

                        if(jsonArray.getJSONObject(i).getInt("x") == mapdata.get(x).x
                                && jsonArray.getJSONObject(i).getInt("y") == mapdata.get(x).y ){
                            return x;
                        }

                    }


                }

            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    /***
     * Used for setting up the thread for fingerprinting
     */
    public void setupFingerPrintThread(){

        fingerthread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {
                    while (isScanning) {
                        HashMap<String, ArrayList<Integer>> fingerPrintTemp = new HashMap<>(3);
                        HashMap<String, Integer> finishedHashMap = new HashMap<>(3);
                        ArrayList<Integer> temparray0 = new ArrayList<>();
                        ArrayList<Integer> temparray1 = new ArrayList<>();
                        ArrayList<Integer> temparray2 = new ArrayList<>();

                        ScanResult[] scanres;
                        scanres = fingerprinter.getFingerPrint();

                        fingerPrintTemp.put(scanres[0].BSSID, temparray0);
                        fingerPrintTemp.put(scanres[1].BSSID, temparray1);
                        fingerPrintTemp.put(scanres[2].BSSID, temparray2);

                        fingerPrintTemp.get(scanres[0].BSSID).add(scanres[0].level);
                        fingerPrintTemp.get(scanres[1].BSSID).add(scanres[1].level);
                        fingerPrintTemp.get(scanres[2].BSSID).add(scanres[2].level);

                        for (int i = 0; i < 5; i++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            scanres = fingerprinter.getFingerPrint();

                            for (int x = 0; x < scanres.length; x++) {
                                if (fingerPrintTemp.containsKey(scanres[x].BSSID)) {
                                    fingerPrintTemp.get(scanres[x].BSSID).add(scanres[x].level);
                                }
                            }
                        }

                        Iterator it = fingerPrintTemp.entrySet().iterator();
                        while (it.hasNext()) {
                            int total = 0;
                            HashMap.Entry pair = (HashMap.Entry) it.next();
                            for (int y = 0; y < fingerPrintTemp.get(pair.getKey()).size(); y++) {
                                total = total + fingerPrintTemp.get(pair.getKey()).get(y);
                            }

                            finishedHashMap.put(pair.getKey().toString(), total / fingerPrintTemp.get(pair.getKey()).size());
                        }

                        fingerPrint = finishedHashMap;
                        currentWimsPoint.fingerprint = finishedHashMap;
                        Message message = mHandler.obtainMessage();
                        message.sendToTarget();
                        isScanning = false;
                    }
                }
            }
        });

    }


    /***
     * Used for drawing the acquired data from the server on the screen, so that it can be worked on further
     * @param data The array of wimspoints
     * @return The imageview with a drawable that represents the points and neighborships
     */
    public ImageView DrawDataOnMap(ArrayList<WimsPoints> data){

        ImageView ViewToAddDrawing = posfac.getPostitionOverlay((int) data.get(0).x, (int) data.get(0).y);
        for(int i = 1; i < data.size();i++){
            ImageView temp = ViewToAddDrawing;
            ViewToAddDrawing = posfac.getBitMapReDrawnSpot(temp,(int)data.get(i).x,(int)data.get(i).y);

            for(int n = 0; n < data.get(i).Neighbours.size(); n++) {
                temp = ViewToAddDrawing;
                ViewToAddDrawing = posfac.getBitMapReDrawnLine(temp, (int)data.get(i).x, (int)data.get(i).y,
                        (int)data.get(i).Neighbours.get(n).x, (int)data.get(i).Neighbours.get(n).y);
            }

        }



        return ViewToAddDrawing;



    }

    /***
     * A simple nearest neighbor algorithm for positioning
     * @param mapData The data of the map
     * @return The point that the scan indicates is the nearest.
     */
    public WimsPoints findNearestNeighbor(ArrayList<WimsPoints> mapData){

        ScanResult[] scanresult = fingerprinter.getFingerPrint();

        WimsPoints three_hit_nearestLocation = null;
        WimsPoints two_hit_nearestLocation = null;
        WimsPoints one_hit_nearestLocation = null;
        double three_hit_distance = Double.POSITIVE_INFINITY;
        double two_hit_distance = Double.POSITIVE_INFINITY;;
        double one_hit_distnace = Double.POSITIVE_INFINITY;;
        double three_hit_tempdistance = 0;
        double two_hit_tempdistance = 0;
        double one_hit_tempdistance = 0;

        for(int i = 0; i < mapData.size(); i++){
            if(mapData.get(i).fingerprint != null){

                if(mapData.get(i).fingerprint.containsKey(scanresult[0].BSSID) && mapData.get(i).fingerprint.containsKey(scanresult[1].BSSID) &&mapData.get(i).fingerprint.containsKey(scanresult[2].BSSID))
                {
                    three_hit_tempdistance = DistanceBetweenScanAndPoint(mapData.get(i),scanresult);
                    if(three_hit_tempdistance < three_hit_distance){
                        three_hit_distance = three_hit_tempdistance;
                        three_hit_nearestLocation = mapData.get(i);
                    }
                } else if(mapData.get(i).fingerprint.containsKey(scanresult[0].BSSID) && mapData.get(i).fingerprint.containsKey(scanresult[1].BSSID) ||
                        mapData.get(i).fingerprint.containsKey(scanresult[1].BSSID) && mapData.get(i).fingerprint.containsKey(scanresult[2].BSSID) ||
                        mapData.get(i).fingerprint.containsKey(scanresult[0].BSSID) && mapData.get(i).fingerprint.containsKey(scanresult[2].BSSID))
                {
                    two_hit_tempdistance = DistanceBetweenScanAndPoint(mapData.get(i),scanresult);
                    if(two_hit_tempdistance < two_hit_distance)
                    {
                        two_hit_distance = two_hit_tempdistance;
                        two_hit_nearestLocation = mapData.get(i);
                    }
                } else if (mapData.get(i).fingerprint.containsKey(scanresult[0].BSSID) || mapData.get(i).fingerprint.containsKey(scanresult[1].BSSID) || mapData.get(i).fingerprint.containsKey(scanresult[2].BSSID)){

                    one_hit_tempdistance = DistanceBetweenScanAndPoint(mapData.get(i),scanresult);

                    if(one_hit_tempdistance < one_hit_distnace){
                        one_hit_distnace = one_hit_tempdistance;
                        one_hit_nearestLocation = mapData.get(i);
                    }
                }
            }

        }

        if(three_hit_nearestLocation != null)
        {
            return three_hit_nearestLocation;
        }
        else if(two_hit_nearestLocation != null)
        {
            return two_hit_nearestLocation;
        }
        else if( one_hit_nearestLocation != null)
        {
            return one_hit_nearestLocation;
        }
        else return null;


    }

    /***
     *
     * @param point The point to check distance up against
     * @param scanresult The scan that has been received
     * @return the distance between scan and point
     */
    public double DistanceBetweenScanAndPoint(WimsPoints point, ScanResult[] scanresult){

        double[] vec = {0,0,0};
        double distance = 0;


        for(int i = 0; i <scanresult.length; i++){
            if(point.fingerprint.containsKey(scanresult[i].BSSID))
                vec[i] = Math.abs(scanresult[i].level - point.fingerprint.get(scanresult[i].BSSID));
            else vec[i] = 0;
        }

        distance = Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2] * vec[2]);

        return distance;
    }
}

