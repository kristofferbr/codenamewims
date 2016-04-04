package sw805f16.codenamewims;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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
    public boolean createMapDataModePoints = false;
    public boolean createMapDataModeNeighbors = false;
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

    MapData mapdata = new MapData();
    ArrayList<WimsPoints> TESTMAPDATA = new ArrayList<>();


    /*For drawing neighbors*/
    int startX=0;
    int starty=0;
    int endX =0;
    int endY =0;
    boolean start = true;
    private Toolbar toolbar;


    ShoppingListFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);

        // my_child_toolbar is defined in the layout file
        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

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

        // Set variables for gestures
        Scale = new ScaleGestureDetector(this,new ScaleDetector());
        // Instantiate the Volley request queue
        rqueue = Volley.newRequestQueue(this);

        // Adapter used for searching
        adapter = new ArrayAdapter<>(getApplicationContext(),
                                      R.layout.simple_list_view,
                                      results);

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

                        spotX = (int) (500 / (float) w * x);
                        spotY = (int) (750 / (float) h * y);
                        if (fram.getChildCount() == 1) {
                            fram.addView(posfac.getPostitionOverlay(spotX, spotY));
                            addPointIfNew(spotX, spotY,TESTMAPDATA);

                        } else {
                            if (addPointIfNew(spotX, spotY, TESTMAPDATA)) {
                                ImageView temp = (ImageView) fram.getChildAt(1);
                                fram.removeViewAt(1);
                                fram.addView(posfac.getBitMapReDrawnSpot(temp, spotX, spotY));
                            }
                        }

                    }

                } else if (createMapDataModeNeighbors) {


                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (start) {
                            float x = event.getX() - v.getLeft();
                            float y = event.getY() - v.getTop();

                            int w = v.getMeasuredWidth();
                            int h = v.getMeasuredHeight();

                            startX = (int) (500 / (float) w * x);
                            starty = (int) (750 / (float) h * y);

                            if(isWithin(startX, starty,TESTMAPDATA)) {
                                start = !start;
                            }
                        } else {
                            float x = event.getX() - v.getLeft();
                            float y = event.getY() - v.getTop();

                            int w = v.getMeasuredWidth();
                            int h = v.getMeasuredHeight();

                            endX = (int) (500 / (float) w * x);
                            endY = (int) (750 / (float) h * y);


                            if (isWithin(endX, endY,TESTMAPDATA)) {
                                WimsPoints startpoint = getWithin(startX,starty,TESTMAPDATA);
                                WimsPoints endpoint = getWithin(endX,endY,TESTMAPDATA);

                                ImageView temp = (ImageView) fram.getChildAt(1);
                                fram.removeViewAt(1);
                                fram.addView(posfac.getBitMapReDrawnLine(temp,(int) startpoint.x, (int) startpoint.y, (int)endpoint.x, (int) endpoint.y));
                                start = !start;
                                setNeighbors(startX,starty,endX,endY,TESTMAPDATA);
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
        search = (SearchView) findViewById(R.id.searchView);
        search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listResults.setVisibility(View.VISIBLE);
            }
        });


        final Button EditButton = (Button) findViewById(R.id.testbut);
        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONArray res = constructJson(mapdata.Data);
                Log.w("WIMS",res.toString());
                JSONArray withneighbor = constructJSONaddNeighbors(res,mapdata.Data);
                Log.w("WIMS",withneighbor.toString());

                ArrayList<WimsPoints> mapdata = deConstructJSON(withneighbor);


                Log.w("WIMS",mapdata.toString()
                );

                if(!createMapDataModePoints && !createMapDataModeNeighbors)
                {
                    createMapDataModeNeighbors = false;
                    createMapDataModePoints = true;
                    EditButton.setText("Points");
                } else if(createMapDataModePoints && !createMapDataModeNeighbors){
                    createMapDataModePoints = false;
                    createMapDataModeNeighbors = true;
                    EditButton.setText("Neighbors");
                } else if(createMapDataModeNeighbors){
                    createMapDataModeNeighbors = false;
                    createMapDataModePoints = false;
                    EditButton.setText("Normal");
                }

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


    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        //inflate the menu: this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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


    // A needed override in order to be able to zooom... and drag maybe..
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(!createMapDataModePoints && !createMapDataModeNeighbors) {
            Scale.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                xOnStart = event.getX();
                yOnStart = event.getY();
                posX = fram.getX();
                posY = fram.getY();
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {

                float offsetX = xOnStart - event.getX();
                float offsetY = yOnStart - event.getY();
                fram.setX(posX - offsetX);
                fram.setY(posY - offsetY);

            }
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

    /****
     * Function used to add an item to the MapData for use of PathDrawing
     * @param itemToAdd The point to add in the data
     */
    public void addItemToMapDataAndDrawRoute(WimsPoints itemToAdd){

        int indexOfNeighbor = 0;
        float distance = itemToAdd.distance(TESTMAPDATA.get(0).x, TESTMAPDATA.get(0).y);
        float tempDist;



        for(int i = 1; i < TESTMAPDATA.size(); i++){
            tempDist = itemToAdd.distance(TESTMAPDATA.get(i).x, TESTMAPDATA.get(i).y);

            if(tempDist < distance){
                indexOfNeighbor = i;
                distance = tempDist;
            }
        }

        itemToAdd.Neighbours.add(TESTMAPDATA.get(indexOfNeighbor));
        TESTMAPDATA.get(indexOfNeighbor).Neighbours.add(itemToAdd);
        TESTMAPDATA.add(itemToAdd);


        if (fram.getChildCount() == 1) {
            fram.addView(posfac.getRouteBetweenTwoPoints(TESTMAPDATA.get(0), itemToAdd));

        } else {
            fram.removeViewAt(1);
            fram.addView(posfac.getRouteBetweenTwoPoints(TESTMAPDATA.get(0), itemToAdd));
        }

        itemToAdd.Neighbours.remove(TESTMAPDATA.get(indexOfNeighbor));
        TESTMAPDATA.get(indexOfNeighbor).Neighbours.remove(itemToAdd);
        TESTMAPDATA.remove(itemToAdd);
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
            TESTMAPDATA.add(new WimsPoints(x, y));
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
    public JSONArray constructJson(ArrayList<WimsPoints> point){

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
                Jsonpointdata.put("_id",i);
                Jsonpointdata.put("x",point.get(i).x);
                Jsonpointdata.put("y",point.get(i).y);
                Jsonpointdata.put("neighbors",neighbors);
                Jsonpointdata.put("fingerprint",fingerprint);
                pointArray.put(Jsonpointdata);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return pointArray;

    }


    /***
     * Function that adds neighbor relations to the JSON object after ID's have been added to each
     * point
     * @param points the Array that contains the point data with ID's and no neighbor relations
     * @param mapData The mapdata represented in Arraylist that has neighbors represented
     * @return the modified JSON array containing neighbors
     */
    public JSONArray constructJSONaddNeighbors(JSONArray points, ArrayList<WimsPoints> mapData){

        JSONArray res = new JSONArray();

        JSONArray neighbors;
        JSONObject workingPoint;
        WimsPoints WIMSpoint;

        try {

            for (int i = 0; points.length() > i; i++) {
                workingPoint = points.getJSONObject(i);
                WIMSpoint = getWithin(workingPoint.getInt("x"), workingPoint.getInt("y"),mapData);
                neighbors = new JSONArray();

                    for(int y = 0; y < WIMSpoint.Neighbours.size(); y++) {
                        String res_id = getIDfromJSON(points, WIMSpoint.Neighbours.get(y));
                        neighbors.put(res_id);
                    }
                    workingPoint.put("neighbors",neighbors);
                    res.put(workingPoint);
                }



        } catch (JSONException e){
            e.printStackTrace();
        }

        return res;

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
}