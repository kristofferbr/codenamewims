package sw805f16.codenamewims;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class StoreMapActivity extends AppCompatActivity {


    // URL til map /api/store/ID/map
    public boolean isInFront = false;
    public String store_id = "someID";
    RequestQueue rqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);
        rqueue = Volley.newRequestQueue(this);
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

    public void getMapLayout(){

        String url = "http://nielsema.ddns.net:3000/api/store/" + store_id +"/map";
        StringRequest stringreq = new StringRequest(Request.Method.GET,url,
                new Response.Listener<String>(){

                    @Override
                    public void onResponse(String response){
                        /*Set this response to a textview of sorts*/
                    }
                },
                new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error){
                        /*Do something with the error*/
                    }
                }
        );

        rqueue.add(stringreq);
    }



}
