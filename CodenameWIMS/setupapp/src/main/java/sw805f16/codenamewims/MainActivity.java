package sw805f16.codenamewims;

import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    public boolean createMapDataModePoints = false;
    public boolean createMapDataModeNeighbors = false;
    public boolean fingerpriting = false;
    public boolean isScanning = false;
    public String store_id = "56e6a28a28c3e3314a6849df"; // The ID of føtex! :)
    public String base_url= "http://nielsema.ddns.net/sw8/api/store/";
    RequestQueue rqueue;
    float scale = 1;
    ScaleGestureDetector Scale;
    PositionOverlayFactory posfac;
    // Variables for dragging
    FrameLayout fram;
    float xOnStart = 0;
    float yOnStart = 0;
    float posX;
    float posY;

    ArrayList<WimsPoints> mapData = new ArrayList<>();
    WimsPoints currentWimsPoint;

    int startX=0;
    int starty=0;
    int endX =0;
    int endY =0;
    boolean start = true;

    Thread fingerthread;
    HashMap fingerPrint=new HashMap(3);
    WifiFingerprinter fingerprinter;
    static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fingerprinter = new WifiFingerprinter(getApplicationContext());
        final Button CommitButton = (Button) findViewById(R.id.commit);
        CommitButton.setVisibility(View.INVISIBLE);
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
                            if (addPointIfNew(spotX, spotY, mapData) && !fingerpriting) {
                                ImageView temp = (ImageView) fram.getChildAt(1);
                                fram.removeViewAt(1);
                                fram.addView(posfac.getBitMapReDrawnSpot(temp, spotX, spotY));
                            } else{
                                if(fram.getChildCount() == 2){
                                    currentWimsPoint = getWithin(spotX,spotY,mapData);
                                    fram.addView(posfac.getPositionOfFingerPrintPoint((int)currentWimsPoint.x,(int)currentWimsPoint.y));
                                    fingerpriting = true;
                                    fingerprintButton.setVisibility(View.VISIBLE);
                                } else
                                {
                                    fram.removeViewAt(2);
                                    fingerpriting = false;
                                    fingerprintButton.setVisibility(View.INVISIBLE);
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

        CommitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMapData(mapData);
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
                    CommitButton.setVisibility(View.VISIBLE);
                    EditButton.setText("Points");
                } else if (createMapDataModePoints && !createMapDataModeNeighbors) {
                    createMapDataModePoints = false;
                    createMapDataModeNeighbors = true;
                    EditButton.setText("Neighbors");
                } else if (createMapDataModeNeighbors) {
                    createMapDataModeNeighbors = false;
                    createMapDataModePoints = false;
                    CommitButton.setVisibility(View.INVISIBLE);
                    EditButton.setText("Normal");
                }

            }
        });

        // Gets the map corresponding to the store ID
        fram =(FrameLayout) findViewById(R.id.MapFrame);

        getMapLayout();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(getApplicationContext(),"SCANNINGS COMPLETE", Toast.LENGTH_LONG).show();

            }
        };
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
