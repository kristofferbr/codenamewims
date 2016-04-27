package sw805f16.codenamewims;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;

import android.net.wifi.ScanResult;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.ListView;
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
import java.util.HashMap;
import java.util.List;

public class StoreMapActivity extends WimsActivity {

    //TODO: Change this to better represent distance
    private static final int maxDist = 25;

    // URL til map /api/store/ID/map
    private boolean isInFront = false;
    private boolean confident = false;
    private String store_id = "56e6a28a28c3e3314a6849df"; // The ID of føtex! :)
    private String base_url= "http://nielsema.ddns.net/sw8dev/api/store/";
    private RequestQueue rqueue;
    private float scale = 1;
    private ScaleGestureDetector Scale;
    private EditText search;
    private JSONArray products;
    private PositionOverlayFactory posfac;

    // Variables for dragging
    private FrameLayout fram;
    private float xOnStart = 0;
    private float yOnStart = 0;
    private float posX;
    private float posY;
    private ListView listResults;
    private ArrayList<String> results = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int maxDepth = 2;
    private HashMap<String, Double> marginalLikelihood = new HashMap<>();

    private ArrayList<WimsPoints> mapData = new ArrayList<>();
    private WimsPoints currentWimsPoint;

    ShoppingListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        // my_child_toolbar is defined in the layout file
        // toolbar = (Toolbar)findViewById(R.id.app_bar);
        //setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        //ActionBar ab = getSupportActionBar();

        // Enable the Up button
        // ab.setDisplayHomeAsUpEnabled(true);


        if(getIntent().getStringExtra("storeId") != null) {
            this.store_id = getIntent().getStringExtra("storeId");
        }

        //this.store_id = getIntent().getStringExtra("storeId");

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
        Scale = new ScaleGestureDetector(this, new ScaleDetector());

        // Instantiate the Volley requestProducts queue
        rqueue = Volley.newRequestQueue(this);

        requestMapData(store_id);


        String url = "http://nielsema.ddns.net/sw8dev/api/store/" + store_id + "/products/";

        JSONContainer.requestProducts(rqueue, url, getApplicationContext());

        // Adapter used for searching
        adapter = new ArrayAdapter<>(getApplicationContext(),
                                      R.layout.simple_list_view,
                                      results);

        fram = (FrameLayout) findViewById(R.id.MapFrame);

        fram.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

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
        });


        //The listview in which the results from searches are submitted
        listResults = (ListView) findViewById(R.id.resultView);
        listResults.setAdapter(adapter);
        //Set onclicklisteners on the items that appears in the Listview when searching
        listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tes = (TextView) view;
                search.setText(tes.getText());
            }
        });

        // Instantiate the factory for generating overlays
        posfac = new PositionOverlayFactory(this);

        // Gets the items of the chosen store
        requestItemsOfStore(store_id);

        // Set the correct listeners on the search widget
        search = (EditText) findViewById(R.id.store_search_field);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                listResults.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                putSuggestionsInList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

                float[] res;

                // Sees if the search Query is matching any products from the store
                res = searchProductReturnCoordinates(JSONContainer.getProducts(), s.toString());

                // If the query matches a product, the resulting location is marked on the map
                if (res[0] != 0) {

                    //DrawLocationOnMap(res[0], res[1]);
                    drawRoute(res);

                }

                listResults.setVisibility(View.INVISIBLE);
            }
        });

        /*CommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMapData(mapData);
                sendMarginalLikelihood();

            }*/

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

    public String getStore_id() {
        return store_id;
    }

    public ArrayList<ScanResult> filterScanByKStrongest(ArrayList<ScanResult> results, int k) {
        ArrayList<ScanResult> retArray = new ArrayList<>();
        int highestLevel;
        ScanResult highestResult = null;

        for (int i = 0; i < k; i++) {
            highestLevel = -100;
            for (ScanResult res : results) {
                if (res != null && res.level > highestLevel) {
                    highestResult = res;
                    highestLevel = res.level;
                }
            }
            retArray.add(highestResult);
            results.remove(highestResult);
        }
        return sortScanAlphabetically(retArray);
    }

    public ArrayList<ScanResult> sortScanAlphabetically(ArrayList<ScanResult> results) {
        ArrayList<ScanResult> retList = new ArrayList<>();

        ScanResult tmpRes;
        while (!results.isEmpty()) {
            tmpRes = results.get(0);
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).BSSID.compareTo(tmpRes.BSSID) > 0) {
                    tmpRes = results.get(i);
                }
            }
            retList.add(tmpRes);
            results.remove(tmpRes);
        }
        return retList;
    }

    public void requestMapData(String storeID){

        String url = base_url + storeID +"/products";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
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

        /*Generates the requestProducts along with a listener that is triggered when image is received*/
        ImageRequest imagereq = new ImageRequest(url,
            new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    mImageView.setImageResource(R.drawable.foetexmap);
                    //mImageView.setImageBitmap(bitmap);
                }
            }, 0, 0, null,
            new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {

                    mImageView.setImageResource(R.drawable.foetexmap);
                    error.printStackTrace();
                }
            }
        );

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

    /***
     *
     * @param product JSON array of products belonging to the store
     * @param query The search query
     * @return Integer array where res[0] = x and res[1] = y
     */
    public float[] searchProductReturnCoordinates(List<WimsPoints> product, String query) {

        float[] res = {0, 0};

        for (int i = 0; i < product.size(); i++) {

            String productq = JSONContainer.getProducts().get(i).getProductName();

            if (query.equalsIgnoreCase(productq)) {

                res[0] = JSONContainer.getProducts().get(i).x;
                res[1] = JSONContainer.getProducts().get(i).y;
                return res;
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
     * Function for filling the resultview with suggestions based on the text in the window
     * @param newtext the string that is compared with the product list
     */
    public void putSuggestionsInList(String newtext){
        results.clear();
        String tempName="";

        if(JSONContainer.getProducts() != null) {

            if (!newtext.equals("")) {

                for (int i = 0; i < JSONContainer.getProducts().size(); i++) {

                    tempName = JSONContainer.getProducts().get(i).getProductName();

                    if (tempName.toLowerCase().contains(newtext.toLowerCase()) || tempName.equalsIgnoreCase(newtext))
                        results.add(tempName);
                }
            }
        } else {
            search.setHint("No items found..");
        }
        adapter.notifyDataSetChanged();
    }

    /**TODO
     * Make mapdata as a parameter
     */
    /****
     * Function used to add an item to the MapData for use of PathDrawing
     * @param itemToAdd The point to add in the data
     */
    public void addItemToMapDataAndDrawRoute(WimsPoints itemToAdd){

        int indexOfNeighbor = 0;
        if(!mapData.isEmpty()) {
            float distance = itemToAdd.distance(mapData.get(0).x, mapData.get(0).y);
            float tempDist;


            for (int i = 1; i < mapData.size(); i++) {
                tempDist = itemToAdd.distance(mapData.get(i).x, mapData.get(i).y);

                if (tempDist < distance) {
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
    }

    /***
     * The main function used when drawing a route
     * As of now the route is drawn from the entrance of the store
     * i.e. the first point in the dataset
     * @param location int[0] = x, int[1] ) y
     */
    public void drawRoute(float[] location){

        WimsPoints ItemToReach = new WimsPoints(location[0],location[1]);

        addItemToMapDataAndDrawRoute(ItemToReach);
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
            WimsPoints point;
            for (int i = 0; i < array.length(); i++) {
                point = new WimsPoints(array.getJSONObject(i).getInt("x"), array.getJSONObject(i).getInt("y"));
                JSONArray probability = array.getJSONObject(i).getJSONArray("probabilities");
                for (int n = 0; n < probability.length(); n++) {
                    point.setProbabilityDistributions(probability.getJSONObject(n).getString("configuration"),
                                                     (float) probability.getJSONObject(n).getDouble("probability"));
                }
                mapdata.add(point);
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

    public String concatBSSIDs(ArrayList<ScanResult> results) {
        String retString = "";
        for (ScanResult res : results) {
            retString = retString + res.BSSID;
        }
        return retString;
    }

    public String concatStrings(List<String> strs) {
        String retString = "";
        for (String res : strs) {
            retString = retString + res;
        }
        return retString;
    }

    /***
     * A simple nearest neighbor algorithm for positioning
     * @param mapData The data of the map
     * @param k The number of neighbors we want to return
     * @param scanResult The scan from where we want the nearest fingerprints
     * @return The points that the scan indicates is the nearest.
     */
    public ArrayList<WimsPoints> kNearestNeighbor(ArrayList<WimsPoints> mapData, int k, ArrayList<ScanResult> scanResult){
        ArrayList<WimsPoints> nearestNeighbors = new ArrayList<>();

        int i = 0;
        for (WimsPoints point : mapData) {
            if (!point.fingerprint.isEmpty() && distanceBetweenScanAndPoint(point, scanResult) <= maxDist) {
                nearestNeighbors.add(point);
                i++;
            }
            if (i > k - 1) {
                break;
            }
        }
        return nearestNeighbors;
    }

    /***
     *
     * @param point The point to check distance up against
     * @param scanresult The scan that has been received
     * @return the distance between scan and point
     */
    public double distanceBetweenScanAndPoint(WimsPoints point, ArrayList<ScanResult> scanresult){

        ArrayList<Double> vec = new ArrayList<>();
        double distance = 0;


        for(int i = 0; i <scanresult.size(); i++){
            if(scanresult.get(i) != null && point.fingerprint.containsKey(scanresult.get(i).BSSID))
            vec.add((double) Math.abs(scanresult.get(i).level - point.fingerprint.get(scanresult.get(i).BSSID)));
            else vec.add(0.0);
        }

        for (double d : vec) {
            distance = distance + Math.pow(d, 2);
        }

        distance = Math.sqrt(distance);

        point.setPriori(1/(distance+1));

        return distance;
    }

    public boolean findPointInNeighborChain(WimsPoints goal, WimsPoints current, int layer,
                                            int maxDepth, ArrayList<WimsPoints> pointsInCurrentLayer,
                                            ArrayList<WimsPoints> visited, ArrayList<WimsPoints> pointsInNextLayer) {
        if (layer <= maxDepth) {
            ArrayList<WimsPoints> neighbors = current.Neighbours;

            if (!neighbors.isEmpty()) {
                for (WimsPoints neighbor : neighbors) {
                    if (visited.contains(neighbor)) {
                        continue;
                    }
                    if (neighbor.equals(goal)) {
                        return true;
                    }
                    pointsInNextLayer.add(neighbor);
                    visited.add(neighbor);
                }
                if (!pointsInCurrentLayer.isEmpty()) {
                    current = pointsInCurrentLayer.get(0);
                    pointsInCurrentLayer.remove(0);
                    return findPointInNeighborChain(goal, current, layer, maxDepth, pointsInCurrentLayer, visited, pointsInNextLayer);
                } else {
                    return findPointInNeighborChain(goal, current, layer + 1, maxDepth, pointsInNextLayer, visited, new ArrayList<WimsPoints>());
                }
            } else if (!pointsInCurrentLayer.isEmpty()){
                current = pointsInCurrentLayer.get(0);
                pointsInCurrentLayer.remove(0);
                return findPointInNeighborChain(goal, current, layer, maxDepth, pointsInCurrentLayer, visited, pointsInNextLayer);
            } else if (!pointsInNextLayer.isEmpty()){
                current = pointsInNextLayer.get(0);
                pointsInNextLayer.remove(0);
                return findPointInNeighborChain(goal, current, layer + 1, maxDepth, pointsInNextLayer, visited, new ArrayList<WimsPoints>());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This algorithm returns the WimsPoint that the user is most likely closest to
     * @param scanResults The last scan result
     * @param mapData The list of WimsPoints in the map
     * @return The point the user is most likely closest to
     */
    public WimsPoints positioningUser(ArrayList<ScanResult> scanResults, ArrayList<WimsPoints> mapData) {
        ArrayList<WimsPoints> candidates = kNearestNeighbor(mapData, 10, scanResults);

        ArrayList<Float> candidateLikelihood = new ArrayList<>();

        String tmpKey = concatBSSIDs(scanResults);
        double normaliser = marginalLikelihood.get(tmpKey) != 0 ? marginalLikelihood.get(tmpKey) : 1;
        for (int i = 0; i < candidates.size(); i++) {
            candidateLikelihood.add((float) 0);
            if (candidates.get(i).getProbabilityDistribution().containsKey(tmpKey)) {
                candidateLikelihood.set(i, candidates.get(i).getProbabilityDistribution().get(tmpKey));
            }
        }

        double posterior;
        double highestPosterior = 0;
        WimsPoints returnPoint = new WimsPoints();

        for (int i = 0; i < candidates.size(); i++) {
            posterior = ((candidateLikelihood.get(i) * candidates.get(i).getPriori()) / normaliser);
            if (posterior > highestPosterior) {
                returnPoint = candidates.get(i);
                highestPosterior = posterior;
            }
        }

        if (highestPosterior != 0) {
            if (highestPosterior > 0.8) {
                confident = true;
                return returnPoint;
            } else {
                return returnPoint;
            }
        } else {
            return null;
        }
    }
}