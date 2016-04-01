package sw805f16.codenamewims;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM2 = "storeId";

    private String storeId = "";
    private ArrayList<WimsPoints> products = new ArrayList<>();

    //These variables are for the suggestion list in the searchview
    private ArrayAdapter suggestionAdapter;
    private ListView suggestionListView;
    private ArrayList<TextView> suggestionList;

    //These variables are for the items in the shopping list
    private ArrayAdapter itemAdapter;
    private ListView itemListView;
    private ArrayList<LinearLayout> completeItemList;
    private ArrayList<LinearLayout> unmarkedItemList;
    private ArrayList<LinearLayout> markedItemList;
    private ArrayList<Integer> marks;
    private FrameLayout currentItem;

    private SearchView searchView;

    private Button startScreenButton;
    private Button storeMapButton;

    //These variables are for controlling which items should be deleted and which are marked
    private int itemToDelete = 0;

    //A gesture detector we use to detect fling gestures
    protected GestureDetectorCompat detector;

    public ShoppingListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id The id of the store.
     * @return A new instance of fragment ShoppingListFragment.
     */
    public static ShoppingListFragment newInstance(String id) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM2, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            storeId = getArguments().getString(ARG_PARAM2);
        }
        //We then initialize a GestureDetector
        detector = new GestureDetectorCompat(getActivity().getApplicationContext(), new GestureListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);


        // Inflate the layout for this fragment
        //We put the inflated view in a local variable
        View mView = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        //Then we initialize the view widgets
        startScreenButton = (Button) mView.findViewById(R.id.startScreenButton);
        storeMapButton = (Button) mView.findViewById(R.id.shopStoreButton);

        suggestionList = new ArrayList<>();
        suggestionListView = (ListView) mView.findViewById(R.id.suggestions);
        suggestionAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.simple_list_view,
                suggestionList);
        suggestionListView.setAdapter(suggestionAdapter);

        completeItemList = new ArrayList<>();
        markedItemList = new ArrayList<>();
        unmarkedItemList = new ArrayList<>();
        marks = new ArrayList<>();
        itemListView = (ListView) mView.findViewById(R.id.itemList);
        itemAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.simple_list_view,
                completeItemList);
        itemListView.setAdapter(itemAdapter);
        currentItem = (FrameLayout) mView.findViewById(R.id.currentItem);

        searchView = (SearchView) mView.findViewById(R.id.shopSearch);

        //If this method was called with the arguments from before we re-assign the views
        if (savedInstanceState != null) {
            setStoreId(savedInstanceState.getString("storeId"));


            ArrayList<String> stringItemList = savedInstanceState.getStringArrayList("unmarkedItemList");
            TextView savedTextView = new TextView(getActivity().getApplicationContext());
            //Here we pull the the list of items and refill the item list
            for (String text : stringItemList) {
                unmarkedItemList.clear();
                savedTextView.setText(text);

                //Here we inflate a LinearLayout with a custom layout
                LinearLayout tmpLayout = (LinearLayout) LinearLayout.inflate(getActivity().getApplicationContext(), R.layout.item_layout, (ViewGroup) itemListView.getEmptyView());
                tmpLayout.addView(savedTextView, 0);
                //Then we add the newly made layout to the item list
                unmarkedItemList.add(tmpLayout);
            }
            stringItemList = savedInstanceState.getStringArrayList("markedItemList");
            for (String text : stringItemList) {
                markedItemList.clear();
                savedTextView.setText(text);

                //Here we inflate a LinearLayout with a custom layout
                LinearLayout tmpLayout = (LinearLayout) LinearLayout.inflate(getActivity().getApplicationContext(), R.layout.item_layout, (ViewGroup) itemListView.getEmptyView());
                tmpLayout.addView(savedTextView, 0);
                //Then we add the newly made layout to the item list
                markedItemList.add(tmpLayout);
            }

            marks = savedInstanceState.getIntegerArrayList("markImages");

            ArrayList<WimsPoints> productPoints = savedInstanceState.getParcelableArrayList("products");
            products = productPoints;
            //If the method was not called with a saved instance we set all the listeners
        } else {
            //The buttons are for transitioning to the MainActivity and StoreMapActivity, respectively
            startScreenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShoppingListActivity parent = (ShoppingListActivity) getActivity();

                    //The actual transition is left to the activity
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

            //The on click listener for the suggestion list places the suggestion in the item list
            suggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //We pull the text view from the suggestion list and resize it
                    TextView tmpText = suggestionList.get(position);
                    tmpText.setWidth(R.dimen.list_item_width);
                    tmpText.setHeight(R.dimen.list_item_height);
                    LinearLayout tmpLayout = (LinearLayout) LinearLayout.inflate(getActivity().getApplicationContext(), R.layout.item_layout, (ViewGroup) itemListView.getEmptyView());
                    tmpLayout.addView(tmpText, 0);

                    //Then we add the layout to the item list, notify the adapter and remove the suggestion list
                    unmarkedItemList.add(tmpLayout);
                    // TODO: When we have positions, change this to that
                    sortItemList(new WimsPoints(0, 0));
                    completeItemList.clear();
                    completeItemList.addAll(unmarkedItemList);
                    completeItemList.addAll(markedItemList);
                    itemAdapter.notifyDataSetChanged();
                    suggestionListView.setVisibility(View.GONE);
                }
            });

            //When clicking an item in the item list view it should mark the items, if the user is in StoreMapActivity
            itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (getActivity() instanceof StoreMapActivity) {
                        markUnmarkItem(position, false);
                    }
                }
            });
            //When the user performs a long press they indicate the item is skipped and receives an X next to it
            itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (getActivity() instanceof StoreMapActivity) {
                        markUnmarkItem(position, true);
                    }
                    return false;
                }
            });
            //Here we set the onTouchListener for the fling event. This is for removing items
            itemListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    itemToDelete = completeItemList.indexOf(v);
                    detector.onTouchEvent(event);
                    return false;
                }
            });
        }
        if (!unmarkedItemList.isEmpty()) {
            currentItem.addView(unmarkedItemList.get(0));
            unmarkedItemList.get(0).setVisibility(View.GONE);
        }

        completeItemList.addAll(unmarkedItemList);
        completeItemList.addAll(markedItemList);

        currentItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markCurrentItem(false);
            }
        });
        currentItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                markCurrentItem(true);
                return false;
            }
        });

        //If we are in the ShoppingListActivity we set the buttons to visible, otherwise they are gone
        if (getActivity() instanceof ShoppingListActivity) {
            startScreenButton.setVisibility(View.VISIBLE);
            storeMapButton.setVisibility(View.VISIBLE);
        }

        //The search view is only responsible for showing the suggestion list
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

        //Then we return the view
        return mView;
    }

    public void markUnmarkItem(int position, boolean skip) {
        LinearLayout item;
        ImageView mark;
        if (position <= unmarkedItemList.size() - 1) {
            item = unmarkedItemList.get(position);
            mark = (ImageView) item.getChildAt(1);
            if (skip) {
                mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.skip));
                marks.add(R.drawable.skip);
            } else {
                mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.checkmark));
                marks.add(R.drawable.checkmark);
            }
            unmarkedItemList.remove(item);
            markedItemList.add(item);
            completeItemList.clear();
            completeItemList.addAll(unmarkedItemList);
            completeItemList.addAll(markedItemList);
            itemAdapter.notifyDataSetChanged();
        } else {
            if (unmarkedItemList.size() != 0) {
                position = position - unmarkedItemList.size();
            }
            item = markedItemList.get(position);
            mark = (ImageView) item.getChildAt(1);
            if (!skip) {
                if (marks.get(position) == R.drawable.checkmark) {
                    mark.setImageDrawable(null);
                    markedItemList.remove(item);
                    marks.remove(position);
                    unmarkedItemList.add(item);
                    // TODO: When we have positions, change this to that
                    sortItemList(new WimsPoints(0, 0));
                } else if (marks.get(position) == R.drawable.skip) {
                    mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.checkmark));
                    marks.set(position, R.drawable.checkmark);
                }
                completeItemList.clear();
                completeItemList.addAll(unmarkedItemList);
                completeItemList.addAll(markedItemList);
                itemAdapter.notifyDataSetChanged();
            } else if (skip) {
                if (marks.get(position) == R.drawable.checkmark) {
                    mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.skip));
                    marks.set(position, R.drawable.skip);
                } else if (marks.get(position) == R.drawable.skip) {
                    mark.setImageDrawable(null);
                    markedItemList.remove(item);
                    marks.remove(position);
                    unmarkedItemList.add(item);
                    // TODO: When we have positions, change this to that
                    sortItemList(new WimsPoints(0, 0));
                }
                completeItemList.clear();
                completeItemList.addAll(unmarkedItemList);
                completeItemList.addAll(markedItemList);
                itemAdapter.notifyDataSetChanged();
            }
        }
    }

    public void markCurrentItem(boolean skip) {
        LinearLayout item = (LinearLayout) currentItem.getChildAt(0);
        ImageView mark = (ImageView) item.getChildAt(1);
        if (!skip) {
            mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.checkmark));
            marks.add(R.drawable.checkmark);
        } else if (skip) {
            mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.skip));
            marks.add(R.drawable.skip);
        }
        markedItemList.add(item);
        unmarkedItemList.remove(0);
        currentItem.removeAllViewsInLayout();
        if (!unmarkedItemList.isEmpty()) {
            currentItem.addView(unmarkedItemList.get(0));
            unmarkedItemList.get(0).setVisibility(View.GONE);
        }
        completeItemList.clear();
        completeItemList.addAll(unmarkedItemList);
        completeItemList.addAll(markedItemList);
        itemAdapter.notifyDataSetChanged();
    }

    public void sortItemList(WimsPoints start) {
        completeItemList.removeAll(unmarkedItemList);
        currentItem.removeAllViewsInLayout();
        unmarkedItemList.get(0).setVisibility(View.VISIBLE);
        ArrayList<WimsPoints> tmpSet = new ArrayList<>();
        String text;
        int index;
        for (int i = 0; i < unmarkedItemList.size(); i++) {
            text = ((TextView) unmarkedItemList.get(i).getChildAt(0)).getText().toString();
            index = indexOfProductWithName(text);
            tmpSet.add(products.get(index));
        }
        WimsPoints point;
        LinearLayout tmpLayout;
        for (int i = 0; i < unmarkedItemList.size(); i++) {
            point = nearestNeightbor(start, tmpSet);
            start = point;
            tmpSet.remove(point);
            for (int n = 0; n < unmarkedItemList.size(); n++) {
                text = ((TextView) unmarkedItemList.get(n).getChildAt(0)).getText().toString();
                if (text.equalsIgnoreCase(point.getProductName())) {
                    tmpLayout = unmarkedItemList.get(n);
                    unmarkedItemList.remove(n);
                    unmarkedItemList.add(i, tmpLayout);
                }
            }
        }
        unmarkedItemList.get(0).setVisibility(View.GONE);
        currentItem.addView(unmarkedItemList.get(0));
        completeItemList.addAll(0, unmarkedItemList);
        itemAdapter.notifyDataSetChanged();
    }

    public WimsPoints nearestNeightbor(WimsPoints start, ArrayList<WimsPoints> set) {
        WimsPoints closestPoint = null;
        float closestDistance = Float.MAX_VALUE;
        for (int i = 0; i < set.size(); i++) {
            if (start.distance(set.get(i).x, set.get(i).y) < closestDistance) {
                closestPoint = set.get(i);
                closestDistance = start.distance(set.get(i).x, set.get(i).y);
            }
        }
        return closestPoint;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Here we take the storeId and ping the server for the list of products in that store
        String url = "http://nielsema.ddns.net/sw8/api/store/" + storeId + "/products/";
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        request(queue, url);
    }

    /**
     * A method for setting the store id. If it is different from the id already defined
     * it checks whether the items in the list is the new store, if not they are grayed out
     * @param id The newe store id
     */
    public void setStoreId(String id) {
        //If the new store id is not the same as the old, we pull a new products list from the server
        if (!id.equalsIgnoreCase(storeId)) {
            String url = "http://nielsema.ddns.net/sw8/api/store/" + id + "/products/";
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

            request(queue, url);

            TextView tmpText;
            //We iterate over the item list and check if the products list contains the item
            for (int i = 0; i < completeItemList.size(); i++) {
                tmpText = (TextView) completeItemList.get(i).getChildAt(0);
                //If it does not contain the item we set the background to a gray color and the text to gray
                if (!productsContainString(tmpText.toString())) {
                    completeItemList.get(i).setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grayout));
                    tmpText.setTextColor(Color.DKGRAY);
                }
            }
            //Then we notify the adapter
            itemAdapter.notifyDataSetChanged();
        }

        //Regardless we set set the id
        storeId = id;
    }

    public boolean productsContainString(String str) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductName().equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public Integer indexOfProductWithName(String str) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductName().equalsIgnoreCase(str)) {
                return i;
            }
        }
        return null;
    }

    /**
     * This method populates the listview with suggestions
     * @param query The query from which to populate after
     */
    private void populateSuggestionList(String query) {
        String key = "";
        //We need to clear the list, otherwise the suggestion list explodes
        suggestionList.clear();
        //We create a text view to add to the suggestion list
        TextView tmpView = new TextView(getActivity().getApplicationContext());
        ArrayList<String> tmpList = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            tmpList.add(products.get(i).getProductName());
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
            //Because this method is only called when we have a new JSON object we clear products
            products.clear();
            String key = "";
            WimsPoints wimsPoints;
            int locX, locY;

            tmpArray = jsonObject.getJSONArray("products");
            for (int i = 0; i < tmpArray.length(); i++) {
                //We pull the product name and the location from the JSONObject
                key = tmpArray.getJSONObject(i).getJSONObject("product").getString("name");
                locX = tmpArray.getJSONObject(i).getJSONObject("location").getInt("x");
                locY = tmpArray.getJSONObject(i).getJSONObject("location").getInt("y");
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

    public String getStoreId() {
        return storeId;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("storeId", storeId);

        ArrayList<String> stringItemList = new ArrayList<>();
        TextView textView;
        String text;
        //We save the text in the unmarked item list
        for (int i = 0; i < unmarkedItemList.size(); i++) {
            textView = (TextView) unmarkedItemList.get(i).getChildAt(0);
            text = textView.getText().toString();
            stringItemList.add(text);
        }
        outState.putStringArrayList("unmarkedItemList", stringItemList);

        stringItemList = new ArrayList<>();
        //We save the text in the marked item list
        for (int i = 0; i < markedItemList.size(); i++) {
            textView = (TextView) markedItemList.get(i).getChildAt(0);
            text = textView.getText().toString();
            stringItemList.add(text);
        }
        outState.putStringArrayList("markedItemList", stringItemList);

        outState.putIntegerArrayList("markImages", marks);

        outState.putParcelableArrayList("products", products);
    }

    //Here we make a custom gesture detector for the fling event
    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            //When the user flings an item they are met with an AlertDialog
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.alert_message)
                    .setPositiveButton(R.string.alert_pos_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //If they click Yes we delete the item and notify the adapter
                            completeItemList.remove(itemToDelete);
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
            return false;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }
    }
}
