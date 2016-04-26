package sw805f16.codenamewims;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kogni on 11-Apr-16.
 */
public final class JSONContainer {
    static ArrayList<WimsPoints> products = new ArrayList<>();
    static HashMap<String, String> stores = new HashMap<>();
    static JSONArray dummyJson;

    public static void populateWithGlobalInformation(Context context){
        String jsonString = context.getResources().getString(R.string.global_json);
        String key = "";
        WimsPoints wimsPoints;
        int locX, locY;
        try {
            dummyJson = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(dummyJson != null){
            try {
                for (int i = 0; i < dummyJson.length(); i++) {
                    //We pull the product name and the location from the JSONObject
                    key = dummyJson.getJSONObject(i).getJSONObject("product").getString("name");
                    locX = dummyJson.getJSONObject(i).getJSONObject("location").getInt("x");
                    locY = dummyJson.getJSONObject(i).getJSONObject("location").getInt("y");
                    //We create a new WimsPoint and set the product name
                    wimsPoints = new WimsPoints(locX, locY);
                    wimsPoints.setProductName(key);

                    products.add(wimsPoints);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A method that extracts information from a json array and puts it in a hashmap
     * @param jsonObject The json array from the server
     */
    public static void extractProductInformationFromJson(JSONObject jsonObject, Context context) {
        JSONArray tmpArray;
        //Because this method is only called when we have a new JSON object we clear products
        products.clear();
        populateWithGlobalInformation(context);
        try {
            String key = "";
            WimsPoints wimsPoints;
            int locX, locY;

            tmpArray = jsonObject.getJSONArray("products");
            for (int i = 0; i < tmpArray.length(); i++) {
                //We pull the product name and the location from the JSONObject
                key = tmpArray.getJSONObject(i).getJSONObject("product").getString("name");
                locX = tmpArray.getJSONObject(i).getJSONObject("location").getInt("x");
                locY = tmpArray.getJSONObject(i).getJSONObject("location").getInt("y");
                //We create a new WimsPoint and set the product name
                wimsPoints = new WimsPoints(locX, locY);
                wimsPoints.setProductName(key);

                products.add(wimsPoints);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public static void getGlobalStoreInformationFromJson(Context context) {
        String jsonString = context.getResources().getString(R.string.store_json);
        JSONObject tmpObject;
        String key = "";
        JSONArray storeJson = new JSONArray();

        try {
            storeJson = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {

            //Here we loop over the json objects in the json array
            for (int i = 0; i < storeJson.length(); i++) {
                tmpObject = storeJson.getJSONObject(i);
                //We extract the storename and the id and place them in a HasMap
                key = SearchRanking.removeSpecialCharacters(tmpObject.getString("name")).toLowerCase();
                stores.put(key, tmpObject.getString("_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method that extracts information from a json array and puts it in a hashmap
     * @param jsonArray The json array from the server
     */
    public static void extractStoreInformationFromJson(JSONArray jsonArray, Context context) {
        //Because this method is only called when we have a new JSON array we clear stores
        stores.clear();

        getGlobalStoreInformationFromJson(context);

        try {
            JSONObject tmpObject;

            String key = "";

            //Here we loop over the json objects in the json array
            for (int i = 0; i < jsonArray.length(); i++) {
                tmpObject = jsonArray.getJSONObject(i);
                //We extract the storename and the id and place them in a HasMap
                key = SearchRanking.removeSpecialCharacters(tmpObject.getString("name")).toLowerCase();
                stores.put(key, tmpObject.getString("_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void requestProducts(RequestQueue req, String url, final Context context){

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                extractProductInformationFromJson(response, context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        req.add(jsonRequest);
    }

    /****
     * The function that performs a requestProducts against our server
     * @param req The queue to add the requestProducts
     * @param url The url to requestProducts
     */
    public static void requestStores(RequestQueue req, String url, final Context context){

        JsonArrayRequest jsonRequest = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                extractStoreInformationFromJson(response, context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Could not retrieve data from the server", Toast.LENGTH_SHORT).show();
            }
        });

        req.add(jsonRequest);
    }

    /**
     * This method checks if there is a product in the products list that contains the input string
     * @param str The product name
     * @return True if there is a match, false otherwise
     */
    public static boolean productsContainString(String str) {
        //We loop through the products list to find a match
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductName().equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method works largely the same as {@link JSONContainer #productsContainString(String)}.
     * This returns the index of the product
     * @param str The input string
     * @return The index of the product, null if it does not exist
     */
    public static Integer indexOfProductWithName(String str) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductName().equalsIgnoreCase(str)) {
                return i;
            }
        }
        return null;
    }

    public static ArrayList<WimsPoints> getProducts() {
        return products;
    }

    public static void setProducts(ArrayList<WimsPoints> products) {
        JSONContainer.products = products;
    }

    /**
     * This method is used by the test classes to evaluate the stores hashmap
     * @return The stores HashMap of this instance
     */
    public static HashMap<String, String> getStores() {
        return stores;
    }
}
