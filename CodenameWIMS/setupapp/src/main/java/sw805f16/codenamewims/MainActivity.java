package sw805f16.codenamewims;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> resultList = new ArrayList<>();
    private ArrayAdapter adapter;
    private boolean createMapDataModePoints = false;
    private boolean createMapDataModeNeighbors = false;
    private boolean fingerpriting = false;
    private boolean isScanning = false;
    private String store_id;
    private String base_url= "http://nielsema.ddns.net/sw8/api/store/";
    private RequestQueue rqueue;
    private float scale = 1;
    private ScaleGestureDetector Scale;
    private PositionOverlayFactory posfac;
    // Variables for dragging
    private FrameLayout fram;
    private float xOnStart = 0;
    private float yOnStart = 0;
    private float posX;
    private float posY;
    private HashMap<String, Double> marginalLikelihood = new HashMap<>();
    private EditText choose_store;
    private ListView suggestionList;

    private ArrayList<WimsPoints> mapData = new ArrayList<>();
    private WimsPoints currentWimsPoint;

    private int startX=0;
    private int starty=0;
    private int endX =0;
    private int endY =0;
    private boolean start = true;

    private Thread fingerthread;
    private WifiFingerprinter fingerprinter;
    private static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rqueue = Volley.newRequestQueue(getApplicationContext());
        JSONContainer.requestStores(rqueue, base_url, getApplicationContext());

        fingerprinter = new WifiFingerprinter(getApplicationContext());
        final Button commitButton = (Button) findViewById(R.id.commit);
        commitButton.setVisibility(View.INVISIBLE);
        final Button deleteButton = (Button) findViewById(R.id.delete);
        deleteButton.setVisibility(View.INVISIBLE);
        setupFingerPrintThread();

        final Button fingerprintButton = (Button) findViewById(R.id.fingerprint);
        fingerprintButton.setVisibility(View.INVISIBLE);

        fingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fingerthread.getState() == Thread.State.NEW) {
                    fingerthread.start();
                }
                isScanning = true;

                /*Deletes the fingerprint view*/
                fram.removeViewAt(2);
                fingerpriting = false;
                fingerprintButton.setVisibility(View.INVISIBLE);
                deleteButton.setVisibility(View.INVISIBLE);

            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePoint();
                deleteButton.setVisibility(View.INVISIBLE);
                fingerprintButton.setVisibility(View.INVISIBLE);
            }
        });
        // Set variables for gestures
        Scale = new ScaleGestureDetector(this,new ScaleDetector());

        // Instantiate the Volley request queue
        rqueue = Volley.newRequestQueue(this);

        fram = (FrameLayout) findViewById(R.id.MapFrame);

        fram.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(!createMapDataModePoints && !createMapDataModeNeighbors) {
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

                        Log.w("Billedeopløsning","X: "+fram.getChildAt(0).getMeasuredWidth() + "Y: " + fram.getChildAt(0).getMeasuredHeight());

                        spotX = (int) (1312 / (float) w * x);
                        spotY = (int) (2132 / (float) h * y);
                        if (fram.getChildCount() == 1) {
                            fram.addView(posfac.getPostitionOverlay(spotX, spotY));
                            addPointIfNew(spotX, spotY, mapData);

                        } else {
                            if (!isWithin(spotX, spotY, mapData) && !fingerpriting) {
                                addPointIfNew(spotX, spotY, mapData);
                                ImageView temp = (ImageView) fram.getChildAt(1);
                                fram.removeViewAt(1);
                                fram.addView(posfac.getBitMapReDrawnSpot(temp, spotX, spotY));
                            } else{
                                if(fram.getChildCount() == 2){
                                    currentWimsPoint = getWithin(spotX,spotY,mapData);
                                    fram.addView(posfac.getPositionOfFingerPrintPoint((int)currentWimsPoint.x,(int)currentWimsPoint.y));
                                    fingerpriting = true;
                                    fingerprintButton.setVisibility(View.VISIBLE);
                                    deleteButton.setVisibility(View.VISIBLE);
                                } else
                                {
                                    fram.removeViewAt(2);
                                    fingerpriting = false;
                                    fingerprintButton.setVisibility(View.INVISIBLE);
                                    deleteButton.setVisibility(View.INVISIBLE);
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

                            if(isWithin(startX, starty, mapData)) {
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
                                WimsPoints startpoint = getWithin(startX,starty, mapData);
                                WimsPoints endpoint = getWithin(endX,endY, mapData);

                                ImageView temp = (ImageView) fram.getChildAt(1);
                                fram.removeViewAt(1);
                                fram.addView(posfac.getBitMapReDrawnLine(temp,(int) startpoint.x, (int) startpoint.y, (int)endpoint.x, (int) endpoint.y));
                                start = !start;
                                setNeighbors(startX,starty,endX,endY, mapData);
                            }

                        }
                    }

                }

                return false;
            }
        });

        // Instantiate the factory for generating overlays
        posfac = new PositionOverlayFactory(this);

        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMapData(mapData);
                //prepMarginalLikelihood();
                try {
                    dumpJsonToFile(mapData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        final Button EditButton = (Button) findViewById(R.id.testbut);
        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!createMapDataModePoints && !createMapDataModeNeighbors) {
                    //DrawDataOnMap(mapData);
                    createMapDataModeNeighbors = false;
                    createMapDataModePoints = true;
                    commitButton.setVisibility(View.VISIBLE);
                    EditButton.setText("Points");
                } else if (createMapDataModePoints && !createMapDataModeNeighbors) {
                    createMapDataModePoints = false;
                    createMapDataModeNeighbors = true;
                    EditButton.setText("Neighbors");
                } else if (createMapDataModeNeighbors) {
                    createMapDataModeNeighbors = false;
                    createMapDataModePoints = false;
                    commitButton.setVisibility(View.INVISIBLE);
                    EditButton.setText("Normal");
                }

            }
        });

        // Gets the map corresponding to the store ID
        fram =(FrameLayout) findViewById(R.id.MapFrame);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(getApplicationContext(),"SCANNINGS COMPLETE", Toast.LENGTH_LONG).show();

            }
        };

        adapter = new ArrayAdapter(getApplicationContext(),
                R.layout.simple_list_view,
                resultList);

        suggestionList = (ListView) findViewById(R.id.resultView);
        suggestionList.setAdapter(adapter);
        choose_store = (EditText) findViewById(R.id.choose_store);
        choose_store.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                suggestionList.setVisibility(View.VISIBLE);
                populateSuggestionList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView) view;
                setStoreId(text.getText().toString());
                suggestionList.setVisibility(View.GONE);
            }
        });
    }

    private void removePoint() {
        mapData.remove(currentWimsPoint);
        for (WimsPoints point : mapData) {
            if (point.Neighbours.contains(currentWimsPoint)) {
                point.Neighbours.remove(currentWimsPoint);
            }
        }
        removePointFromMap();
        currentWimsPoint = new WimsPoints();
    }

    private void removePointFromMap() {
        fram.removeViewAt(2);
        fram.removeViewAt(1);
        if (!mapData.isEmpty()) {
            ImageView tmpImage = posfac.getPostitionOverlay(mapData.get(0).x, mapData.get(0).y);
            if (mapData.size() > 1) {
                for (int k = 1; k < mapData.size(); k++) {
                    tmpImage = posfac.getBitMapReDrawnSpot(tmpImage, mapData.get(k).x, mapData.get(k).y);
                }
            }
            int i = 0;
            for (WimsPoints curr : mapData) {
                for (int n = i + 1; n < mapData.size(); n++) {
                    if (curr.Neighbours.contains(mapData.get(n))) {
                        tmpImage = posfac.getBitMapReDrawnLine(tmpImage, curr.x, curr.y,
                                mapData.get(n).x, mapData.get(n).y);
                    }
                }
                i++;
            }
            fram.addView(tmpImage);
        }
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

    private void setStoreId(String key){
        if (JSONContainer.getStores().containsKey(key.toString().toLowerCase())) {
            //The search field is emptied
            choose_store.setText("");
            store_id = JSONContainer.getStores().get(key.toString());
            getMapLayout();
        }
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
                if (results.get(i) != null && results.get(i).BSSID.compareTo(tmpRes.BSSID) > 0) {
                    tmpRes = results.get(i);
                }
            }
            retList.add(tmpRes);
            results.remove(tmpRes);
        }
        return retList;
    }

    public ArrayList<String> sortStringAlphabetically(ArrayList<String> strs) {
        ArrayList<String> retList = new ArrayList<>();

        String tmpRes;
        while (!strs.isEmpty()) {
            tmpRes = strs.get(0);
            for (int i = 0; i < strs.size(); i++) {
                if (strs.get(i).compareTo(tmpRes) > 0) {
                    tmpRes = strs.get(i);
                }
            }
            retList.add(tmpRes);
            strs.remove(tmpRes);
        }
        return retList;
    }

    public void prepMarginalLikelihood() {
        computeMarginalLikelihood();
        final String url = base_url + store_id;


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject tosend;
                        try {
                            tosend = wrapLikelihoodInJson(marginalLikelihood, response);
                            sendMarginalLikelihood(tosend, url);
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

    public void sendMarginalLikelihood(JSONObject obj, String url) {
        JsonObjectRequest postObjReq = new JsonObjectRequest(Request.Method.PUT, url, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        rqueue.add(postObjReq);
    }

    public JSONObject wrapLikelihoodInJson(HashMap<String, Double> marginalLikelihood, JSONObject response) throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject obj;

        Iterator it = marginalLikelihood.entrySet().iterator();
        Map.Entry pair;

        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            obj  = new JSONObject();
            obj.put("configuration", pair.getKey());
            obj.put("likelihood", pair.getValue());
            array.put(obj);
        }
        response.put("marginalLikelihood", array);
        return response;
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

    public void dumpJsonToFile(ArrayList<WimsPoints> mapData) throws JSONException {
        JSONObject pointDump = constructJson(mapData);
        computeMarginalLikelihood();
        JSONObject likelihoodDump = wrapLikelihoodInJson(marginalLikelihood, new JSONObject(getResources().getString(R.string.shop_json)));

        try {
            for (int i = 0; i < pointDump.getJSONArray("points").length(); i++) {
                pointDump.getJSONArray("points").getJSONObject(i).put("_id", i+8);
            }
            pointDump = constructJSONaddNeighbors(pointDump.getJSONArray("points"), mapData);

            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).getAbsolutePath());
            File file = new File(directory, "json_dump.txt");
            file.getParentFile().mkdirs();
            if (file.exists()) {
                file.delete();
                file = new File(directory, "json_dump.txt");
            }
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(pointDump.toString().getBytes());
            stream.write("\n\n\n".getBytes());
            stream.write(likelihoodDump.toString().getBytes());
            stream.flush();
            stream.close();

            //Log.e("point_json", pointDump.toString());
            //Log.e("likelihood_json", likelihoodDump.toString());
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
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
        JSONArray probDist;


        try {
            for(int i = 0; i < point.size(); i++)
            {

                Jsonpointdata = new JSONObject();
                neighbors = new JSONArray();
                fingerprint = new JSONArray();
                probDist = new JSONArray();
                if(point.get(i).ID != null)
                    Jsonpointdata.put("_id",point.get(i).ID);
                Jsonpointdata.put("x",point.get(i).x);
                Jsonpointdata.put("y",point.get(i).y);


                if(point.get(i).fingerprint != null) {
                    Iterator it = point.get(i).fingerprint.entrySet().iterator();
                    if (it.hasNext()) {
                        while (it.hasNext()) {
                            HashMap.Entry pair = (HashMap.Entry) it.next();
                            JSONObject print = new JSONObject();
                            print.put("bssid", pair.getKey().toString());
                            print.put("rssi", pair.getValue());
                            fingerprint.put(print);
                        }
                    }
                }
                if(point.get(i).getProbabilityDistribution() != null) {
                    Iterator it = point.get(i).getProbabilityDistribution().entrySet().iterator();
                    if (it.hasNext()) {
                        while (it.hasNext()) {
                            HashMap.Entry pair = (HashMap.Entry) it.next();
                            JSONObject probabilities = new JSONObject();
                            probabilities.put("configuration", pair.getKey().toString());
                            probabilities.put("probability", pair.getValue());
                            probDist.put(probabilities);
                        }
                    }
                }
                Jsonpointdata.put("neighbors",neighbors);
                Jsonpointdata.put("fingerprint",fingerprint);
                Jsonpointdata.put("probabilities", probDist);
                pointArray.put(Jsonpointdata);


            }
            tosend.put("points", pointArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return tosend;

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

            if(x < pointArray.get(i).x+50 && x > pointArray.get(i).x -50
                    && y < pointArray.get(i).y+50 && y > pointArray.get(i).y - 50){
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

            if(x < pointArray.get(i).x+50 && x > pointArray.get(i).x -50
                    && y < pointArray.get(i).y+50 && y > pointArray.get(i).y - 50){
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
                //mImageView.setImageBitmap(bitmap);
                mImageView.setImageResource(R.drawable.foetexmap);
            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                        mImageView.setImageResource(R.drawable.lane_audit_floorplan_fingerprinting_version);
                        error.printStackTrace();
                    }
                });

        rqueue.add(imagereq);
    }

    public String concatBSSIDs(ArrayList<ScanResult> results) {
        String retString = "";
        for (ScanResult res : results) {
            if (res != null) {
                retString = retString + res.BSSID;
            }
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
     * Used for setting up the thread for fingerprinting
     */
    public void setupFingerPrintThread(){

        fingerthread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {
                    while (isScanning) {
                        HashMap<String, ArrayList<Float>> fingerPrintTemp = new HashMap<>();
                        HashMap<String, Float> finishedFingerprint = new HashMap<>();
                        ArrayList<String> configuration = new ArrayList<>();

                        ArrayList<ScanResult> scanres;
                        scanres = fingerprinter.getFingerPrint();

                        for (ScanResult res : scanres) {
                            if (res != null) {
                                fingerPrintTemp.put(res.BSSID, new ArrayList<Float>());
                            }
                        }
                        int measurements;
                        for (measurements = 0; measurements < 60; measurements++) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            scanres = fingerprinter.getFingerPrint();
                            for (ScanResult res : scanres) {
                                if (res != null && fingerPrintTemp.containsKey(res.BSSID)) {
                                    fingerPrintTemp.get(res.BSSID).add((float) res.level);
                                }
                            }
                            scanres = filterScanByKStrongest(scanres, 3);
                            configuration.add(concatBSSIDs(scanres));
                        }

                        Iterator it = fingerPrintTemp.entrySet().iterator();
                        while (it.hasNext()) {
                            HashMap.Entry pair = (HashMap.Entry) it.next();
                            float total = 0;
                            for (int y = 0; y < fingerPrintTemp.get(pair.getKey()).size(); y++) {
                                total = total + fingerPrintTemp.get(pair.getKey()).get(y);
                            }
                            finishedFingerprint.put((String) pair.getKey(), (total / (float) fingerPrintTemp.get(pair.getKey()).size()));
                        }

                        ArrayList<String> tmpList = new ArrayList<>();
                        while (!configuration.isEmpty()) {
                            tmpList.add(configuration.get(0));
                            for (int n = 1; n < configuration.size(); n++) {
                                if (configuration.get(n).equalsIgnoreCase(tmpList.get(0))) {
                                    tmpList.add(configuration.get(n));
                                }
                            }
                            currentWimsPoint.setProbabilityDistributions(tmpList.get(0), ((float) tmpList.size() / (float) measurements));
                            configuration.removeAll(tmpList);
                            tmpList.clear();
                        }

                        currentWimsPoint.fingerprint = finishedFingerprint;
                        Message message = mHandler.obtainMessage();
                        message.sendToTarget();
                        isScanning = false;
                    }
                }
            }
        });

    }

    public void computeMarginalLikelihood() {
        marginalLikelihood.clear();
        ArrayList<String> bssids = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.test_bssid2)));
        bssids = sortStringAlphabetically(bssids);

        ArrayList<String> configurations = new ArrayList<>();
        ArrayList<String> tmpConfig = new ArrayList<>();
        String tmpBssid;

        for (int i = 0; i < bssids.size() - 2; i++) {
            tmpConfig.add(bssids.get(i));
            for (int n = i+1; n < bssids.size() - 1; n++) {
                tmpConfig.add(bssids.get(n));
                for (int k = n+1; k < bssids.size(); k++) {
                    tmpConfig.add(bssids.get(k));
                    tmpBssid = concatStrings(tmpConfig);
                    if (!configurations.contains(tmpBssid)) {
                        configurations.add(tmpBssid);
                    }
                    tmpConfig.remove(2);
                }
                tmpConfig.remove(1);
            }
            tmpConfig.remove(0);
        }

        double likelihood = 0;
        for (String str : configurations) {
            for (int i = 0; i < mapData.size(); i++) {
                if (mapData.get(i).getProbabilityDistribution().containsKey(str)) {
                    likelihood += mapData.get(i).getProbabilityDistribution().get(str);
                }
            }
            if (likelihood != 0) {
                marginalLikelihood.put(str, (likelihood / mapData.size()));
                likelihood = 0;
            }
        }
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
}
