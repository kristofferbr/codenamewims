package sw805f16.codenamewims;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private String storeId = "";
    private HashMap<String, Integer[]> products = new HashMap<>();
    private JSONObject json;

    private ArrayAdapter suggestionAdapter;
    private ListView suggestionListView;
    private ArrayList<TextView> suggestionList;

    private ArrayAdapter itemAdapter;
    private ListView itemListView;
    private ArrayList<LinearLayout> itemList;

    private SearchView searchView;

    private Button startScreenButton;
    private Button storeMapButton;

    private int itemToDelete;
    private HashMap<Integer, Boolean> markedItems = new HashMap<>();

    protected GestureDetectorCompat detector;

    public ShoppingListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param state The state of the fragment from another activity.
     * @return A new instance of fragment ShoppingListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShoppingListFragment newInstance(Bundle state) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putBundle(ARG_PARAM1, state);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
            RelativeLayout layout = new RelativeLayout(getActivity().getApplicationContext());
            if (getActivity() instanceof StoreMapActivity) {
                layout = (RelativeLayout) getActivity().findViewById(R.id.storeParent);
            } else if (getActivity() instanceof ShoppingListActivity) {
                layout = (RelativeLayout) getActivity().findViewById(R.id.shoppingParent);
            }
            this.onCreateView(inflater, layout, savedInstanceState);
        }

        detector = new GestureDetectorCompat(getActivity().getApplicationContext(), new GestureListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);


        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        startScreenButton = (Button) mView.findViewById(R.id.startScreenButton);
        storeMapButton = (Button) mView.findViewById(R.id.shopStoreButton);

        suggestionList = new ArrayList<>();
        suggestionListView = (ListView) mView.findViewById(R.id.suggestions);

        itemList = new ArrayList<>();
        itemListView = (ListView) mView.findViewById(R.id.itemList);

        searchView = (SearchView) mView.findViewById(R.id.shopSearch);

        if (savedInstanceState != null) {
            storeId = savedInstanceState.getString("storeId");
            startScreenButton.onRestoreInstanceState(savedInstanceState.getParcelable("startScreenButton"));
            storeMapButton.onRestoreInstanceState(savedInstanceState.getParcelable("storeMapButton"));
            suggestionListView.onRestoreInstanceState(savedInstanceState.getParcelable("suggestionListView"));
            itemListView.onRestoreInstanceState(savedInstanceState.getParcelable("itemListView"));
            try {
                json = new JSONObject(savedInstanceState.getString("json"));
                extractInformationFromJson(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int[] tmpInt = savedInstanceState.getIntArray("markedPositions");
            boolean[] tmpBoolean = savedInstanceState.getBooleanArray("marks");
            for (int i = 0; i < tmpInt.length; i++) {
                markedItems.clear();
                markedItems.put(tmpInt[i], tmpBoolean[i]);
            }
            ArrayList<Parcelable> tmpList = savedInstanceState.getParcelableArrayList("itemList");
            TextView tmpText = new TextView(getActivity().getApplicationContext());
            int i = 0;
            for (Parcelable view : tmpList) {
                itemList.clear();
                tmpText.onRestoreInstanceState(view);

                LinearLayout tmpLayout = (LinearLayout) LinearLayout.inflate(getActivity().getApplicationContext(), R.layout.item_layout, new LinearLayout(getActivity().getApplicationContext()));
                tmpLayout.addView(tmpText, 0);
                if (markedItems.get(i)) {
                    ImageView tmpImage = (ImageView) tmpLayout.getChildAt(1);
                    tmpImage.setImageDrawable(getResources().getDrawable(R.drawable.checkmark));
                } else if (!markedItems.get(i)) {
                    ImageView tmpImage = (ImageView) tmpLayout.getChildAt(1);
                    tmpImage.setImageDrawable(getResources().getDrawable(R.drawable.skip));
                }

                itemList.add(tmpLayout);
                i++;
            }

        } else {
            startScreenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShoppingListActivity parent = (ShoppingListActivity) getActivity();

                    parent.transitionToStartScreen();
                }
            });
            storeMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShoppingListActivity parent = (ShoppingListActivity) getActivity();

                    parent.transitionToStoreMap();
                }
            });

            suggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView tmpText = suggestionList.get(position);
                    tmpText.setWidth(R.dimen.list_item_width);
                    tmpText.setHeight(R.dimen.list_item_height);
                    LinearLayout tmpLayout = (LinearLayout) LinearLayout.inflate(getActivity().getApplicationContext(), R.layout.item_layout, new LinearLayout(getActivity().getApplicationContext()));
                    tmpLayout.addView(tmpText, 0);

                    itemList.add(tmpLayout);
                    itemAdapter.notifyDataSetChanged();
                    suggestionListView.setVisibility(View.GONE);
                }
            });

            itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (getActivity() instanceof StoreMapActivity) {
                        if (markedItems.get(position)) {
                            ImageView tmpImage = (ImageView) itemList.get(position).getChildAt(1);
                            tmpImage.setImageDrawable(null);

                            markedItems.remove(position);
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            ImageView tmpImage = (ImageView) itemList.get(position).getChildAt(1);
                            tmpImage.setImageDrawable(getResources().getDrawable(R.drawable.checkmark));

                            markedItems.put(position, true);
                            itemAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
            itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (getActivity() instanceof StoreMapActivity) {
                        if (!markedItems.get(position)) {
                            ImageView tmpImage = (ImageView) itemList.get(position).getChildAt(1);
                            tmpImage.setImageDrawable(null);

                            markedItems.remove(position);
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            ImageView tmpImage = (ImageView) itemList.get(position).getChildAt(1);
                            tmpImage.setImageDrawable(getResources().getDrawable(R.drawable.skip));

                            markedItems.put(position, false);
                            itemAdapter.notifyDataSetChanged();
                        }
                    }
                    return true;
                }
            });
            itemListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    itemToDelete = itemList.indexOf(v);
                    detector.onTouchEvent(event);
                    return true;
                }
            });
        }

        if (getActivity() instanceof ShoppingListActivity) {
            startScreenButton.setVisibility(View.VISIBLE);
            storeMapButton.setVisibility(View.VISIBLE);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                populateSuggestionList(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                suggestionListView.setVisibility(View.VISIBLE);
                populateSuggestionList(newText);
                return true;
            }
        });

        itemAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.simple_list_view,
                itemList);
        itemListView.setAdapter(itemAdapter);

        suggestionAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.simple_list_view,
                suggestionList);
        suggestionListView.setAdapter(suggestionAdapter);

        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        String url = "http://nielsema.ddns.net/sw8/api/store/" + storeId + "/products/";
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        request(queue, url);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setStoreId(String id) {
        if (!id.equalsIgnoreCase(storeId)) {
            String url = "http://nielsema.ddns.net/sw8/api/store/" + id + "/products/";
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

            request(queue, url);

            TextView tmpText;
            for (int i = 0; i < itemList.size(); i++) {
                tmpText = (TextView) itemList.get(i).getChildAt(0);
                if (!products.containsKey(tmpText.toString())) {
                    itemList.get(i).setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grayout));
                    tmpText.setTextColor(Color.DKGRAY);
                }
            }
            itemAdapter.notifyDataSetChanged();
        }

        storeId = id;
    }

    /**
     * This method populates the listview with suggestions
     * @param query The query from which to populate after
     */
    private void populateSuggestionList(String query) {
        String key = "";
        //We need to clear the list, otherwise the suggestion list explodes
        suggestionAdapter.clear();
        //We make an iterator and iterate over the products hashmap
        Iterator it = products.entrySet().iterator();
        Map.Entry pair;
        //We create a text view to add to the suggestion list
        TextView tmpView = new TextView(getActivity().getApplicationContext());
        ArrayList<String> tmpList = new ArrayList<>();

        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            key = (String) pair.getKey();

            //We add the string keys to the temporary list
            tmpList.add(key);
        }

        //We sort the tmpList
        tmpList = SearchRanking.rankSearchResults(query, tmpList);

        for (String str : tmpList) {
            //We set the text for the textviews to the strings in tmpList
            tmpView.setText(str);

            //We add the textviews to the suggestion list
            suggestionList.add(tmpView);
        }

        //Then we notify the adapter that the list is modified
        suggestionAdapter.notifyDataSetChanged();
    }

    /**
     * A method that extracts information from a json array and puts it in a hashmap
     * @param jsonObject The json array from the server
     */
    public void extractInformationFromJson(JSONObject jsonObject) {
        try {
            JSONArray tmpArray;
            //Because this method is only called when we have a new JSON array we clear stores
            products.clear();
            String key = "";
            Integer[] location = new Integer[2];

            tmpArray = jsonObject.getJSONArray("products");
            for (int i = 0; i < tmpArray.length(); i++) {
                key = tmpArray.getJSONObject(i).getJSONObject("product").getString("name");
                location[0] = tmpArray.getJSONObject(i).getJSONObject("location").getInt("x");
                location[1] = tmpArray.getJSONObject(i).getJSONObject("location").getInt("y");

                products.put(key, location);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /****
     * The function that performs a request against our server
     * @param req The queue to add the request
     * @param url The url to request
     */
    private void request(RequestQueue req, String url){

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                extractInformationFromJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        req.add(jsonRequest);
    }

    public Bundle saveState() {

        Bundle outState = new Bundle();
        outState.putString("storeId", storeId);
        outState.putParcelable("startScreenButton", startScreenButton.onSaveInstanceState());
        outState.putParcelable("storeMapButton", storeMapButton.onSaveInstanceState());
        outState.putParcelable("suggestionListView", suggestionListView.onSaveInstanceState());
        outState.putParcelable("itemListView", itemListView.onSaveInstanceState());
        outState.putString("json", json.toString());
        ArrayList<Parcelable> tmpList = new ArrayList<>();
        TextView tmpText;
        for (int i = 0; i < itemList.size(); i++) {
            tmpText = (TextView) itemList.get(i).getChildAt(0);
            tmpList.add(tmpText.onSaveInstanceState());
        }
        outState.putParcelableArrayList("itemList", tmpList);
        int[] markedPositions = new int[0];
        boolean[] marks = new boolean[0];
        Iterator it = markedItems.entrySet().iterator();
        Map.Entry pair;
        int i = 0;
        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            markedPositions[i] = (Integer) pair.getKey();
            marks[i] = (Boolean) pair.getValue();
        }
        outState.putIntArray("markedPositions", markedPositions);
        outState.putBooleanArray("marks", marks);

        return outState;
    }

    public void setJson(JSONObject tmpJsonObject) {
        json = tmpJsonObject;
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.alert_message)
                    .setPositiveButton(R.string.alert_pos_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            itemList.remove(itemToDelete);
                            itemAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.alert_neg_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }
    }
}
