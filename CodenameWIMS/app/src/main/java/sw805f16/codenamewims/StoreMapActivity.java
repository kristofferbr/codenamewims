package sw805f16.codenamewims;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StoreMapActivity extends AppCompatActivity {


    // URL til map /api/store/ID/map
    public boolean isInFront = false;
    public String store_id = "56d81f53b50334c9534d0729"; // The ID of føtex! :)
    RequestQueue rqueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);
        rqueue = Volley.newRequestQueue(this);
        Button but = (Button)findViewById(R.id.button);
        final PositionOverlayFactory posfac = new PositionOverlayFactory(this);

        getMapLayout();

        FrameLayout fram = (FrameLayout) findViewById(R.id.MapFrame);

        fram.addView(posfac.getPostitionOverlay(20,20));


        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestItemsOfStore(store_id);

                /*
                FrameLayout fram = (FrameLayout) findViewById(R.id.MapFrame);


                if(fram.getChildCount() == 1){
                    fram.addView(posfac.getPostitionOverlay(20,20));

                } else
                {
                    fram.removeViewAt(1);
                    fram.addView(posfac.getPostitionOverlay(20,50));
                }

                */
            }
        });
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


    /****
     * Retrieves the layout from the server
     */
    public void getMapLayout(){

        final String url = "http://nielsema.ddns.net:3000/api/store/" + store_id +"/map";
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
                    }
                });

        rqueue.add(imagereq);
    }


    public void requestItemsOfStore(String storeID){

        String url = "http://nielsema.ddns.net:3000/api/store/56d81f53b50334c9534d0729/products";
        final TextView tex = (TextView) findViewById(R.id.textView);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                            String results = "";
                        try {
                            JSONArray products = response.getJSONArray("products");

                            // Loops through all the products that the store has and writes them out
                            for(int counter = 0; counter <products.length(); counter++){
                                JSONObject pro = products.getJSONObject(counter);
                                results = results + getProductAndCoordinates(pro);
                            }

                            tex.setText(results);
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

            res = acpro.getString("name") + ": x:" + location.getString("x") + " y:" + location.getString("y");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;

    }

}


