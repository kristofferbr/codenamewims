package sw805f16.codenamewims;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.Image;
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
    private ArrayList<LinearLayout> unavailableItemList;
    //This is a list of drawable ids for which mark the item should have
    private ArrayList<Integer> marks;
    private FrameLayout currentItem;

    //These variables are for controlling which items should be deleted
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

        //We initialize a GestureDetector
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
        Button startScreenButton = (Button) mView.findViewById(R.id.startScreenButton);
        Button storeMapButton = (Button) mView.findViewById(R.id.shopStoreButton);

        suggestionList = new ArrayList<>();
        suggestionListView = (ListView) mView.findViewById(R.id.suggestions);
        suggestionAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.simple_list_view,
                suggestionList);
        suggestionListView.setAdapter(suggestionAdapter);

        completeItemList = new ArrayList<>();
        markedItemList = new ArrayList<>();
        unmarkedItemList = new ArrayList<>();
        unavailableItemList = new ArrayList<>();
        marks = new ArrayList<>();
        itemListView = (ListView) mView.findViewById(R.id.itemList);
        itemAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.simple_list_view,
                completeItemList);
        itemListView.setAdapter(itemAdapter);
        currentItem = (FrameLayout) mView.findViewById(R.id.currentItem);

        SearchView searchView = (SearchView) mView.findViewById(R.id.shopSearch);

        //If this method was called with the arguments from before we re-assign the views
        if (savedInstanceState != null) {

            if (getArguments() == null) {
                setStoreId(savedInstanceState.getString("storeId"));
            }

            ArrayList<String> stringItemList = savedInstanceState.getStringArrayList("unmarkedItemList");
            TextView savedTextView = new TextView(getActivity().getApplicationContext());
            //Here we pull the the list of unmarked items and refill the item list
            for (String text : stringItemList) {
                unmarkedItemList.clear();
                savedTextView.setText(text);

                //Here we inflate a LinearLayout with a custom layout
                LinearLayout tmpLayout = (LinearLayout) LinearLayout.inflate(getActivity().getApplicationContext(), R.layout.item_layout, (ViewGroup) itemListView.getEmptyView());
                tmpLayout.addView(savedTextView, 0);
                //Then we add the newly made layout to the item list
                unmarkedItemList.add(tmpLayout);
            }
            //Then we pull the list of marked items
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

            //Here we take the drawable ids for the marked items
            //These correspond to the items in the marked item list
            marks = savedInstanceState.getIntegerArrayList("markImages");

            //Lastly we pull the list of products at place it in the products variable
            products = savedInstanceState.getParcelableArrayList("products");
        }
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

                //Then we add the layout to the item list, notify the adapter and sort the list
                unmarkedItemList.add(tmpLayout);
                // TODO: When we have positions, change this to that
                sortItemList(new WimsPoints(0, 0));
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
                    markUnmarkItem(position, false);
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

        //We populate the complete item list
        populateCompleteList();

        //If the list of unmarked items is not empty and we are in StoreMapActivity we place the first
        //item FrameLayout
        if (!unmarkedItemList.isEmpty() && getActivity() instanceof StoreMapActivity) {
            currentItem.addView(unmarkedItemList.get(0));
            unmarkedItemList.get(0).setVisibility(View.GONE);
        }

        //When clicking the item in the FrameLayout it should mark it like the item list
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
        currentItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                itemToDelete = 0;
                detector.onTouchEvent(event);
                return false;
            }
        });

        //If this fragment was called with an ID then the storeId is set
        if (getArguments() != null) {
            setStoreId(getArguments().getString("storeId"));
        }

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

    /**
     * This method is used for marking and unmarking the items in the item list
     * @param position The position of the item in the list
     * @param longClick True if the method is called from a long click, False if it should be marked as put in the basket
     */
    public void markUnmarkItem(int position, boolean longClick) {
        LinearLayout item;
        ImageView mark;
        //Since the list view uses the complete item list we need to know which sublist corresponds to the position
        if (position <= unmarkedItemList.size() - 1) {
            item = unmarkedItemList.get(position);
            mark = (ImageView) item.getChildAt(1);
            //If longClick is true we place the longClick drawable next to the item.
            if (longClick) {
                mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.skip));
                marks.add(R.drawable.skip);

            //Otherwise, we place the checkmark drawable
            } else {
                mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.checkmark));
                marks.add(R.drawable.checkmark);
            }
            //Now that the item is marked we remove it from the unmarked list and add it to the marked list
            unmarkedItemList.remove(item);
            markedItemList.add(item);
            //We then re-populate the complete list
            populateCompleteList();
        } else {
            //If the unmarked item list is not empty we update the the position to correspond to the marked item index
            if (!unmarkedItemList.isEmpty()) {
                position = position - unmarkedItemList.size();
            }
            item = markedItemList.get(position);
            mark = (ImageView) item.getChildAt(1);
            if (!longClick) {
                //We look the drawable resource up in the marks list and check if it is the checkmark
                if (marks.get(position) == R.drawable.checkmark) {
                    //Because this is called with a normal click the checkmark is removed
                    mark.setImageDrawable(null);
                    markedItemList.remove(item);
                    //We also remove the drawable reference
                    marks.remove(position);
                    unmarkedItemList.add(item);
                    //After adding it the item to the unmarked list we sort the list
                    // TODO: When we have positions, change this to that
                    sortItemList(new WimsPoints(0, 0));
                } else if (marks.get(position) == R.drawable.skip) {
                    //When the user clicks a skipped item with a normal click, the drawable is replaced with a checkmark
                    mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.checkmark));
                    marks.set(position, R.drawable.checkmark);
                }
                //Lastly, we populate the complete list
                populateCompleteList();

            //The else if clause is similar to the if clause, just reversed
            } else if (longClick) {
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
                populateCompleteList();
            }
        }
        //Then we notify the adapter
        itemAdapter.notifyDataSetChanged();
    }

    /**
     * This method is similar to the {@link ShoppingListFragment#markUnmarkItem(int, boolean)},
     * however it is called from the currentItem FrameLayout
     * @param longClick Whether this method is called from with a longclick
     */
    public void markCurrentItem(boolean longClick) {
        LinearLayout item = (LinearLayout) currentItem.getChildAt(0);
        ImageView mark = (ImageView) item.getChildAt(1);
        //If longclick is false the drawable is a checkmark, otherwise the drawable is skip
        if (!longClick) {
            mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.checkmark));
            marks.add(R.drawable.checkmark);
        } else if (longClick) {
            mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.skip));
            marks.add(R.drawable.skip);
        }
        //Because the content of the currentItem is the first item in the unmarked list, it is removed and added to the marked list
        markedItemList.add(item);
        unmarkedItemList.remove(0);
        currentItem.removeAllViewsInLayout();
        //If there are any items left in the unmarked list it is placed in the currentItem layout
        if (!unmarkedItemList.isEmpty()) {
            currentItem.addView(unmarkedItemList.get(0));
            unmarkedItemList.get(0).setVisibility(View.GONE);
        }
        //Then we populate the complete list and notify the adapter
        populateCompleteList();
        itemAdapter.notifyDataSetChanged();
    }

    /**
     * This method sorts the unmarked item list by nearest neighbor to a starting point
     * @param start The starting point for the sorting
     */
    public void sortItemList(WimsPoints start) {
        currentItem.removeAllViewsInLayout();
        unmarkedItemList.get(0).setVisibility(View.VISIBLE);
        ArrayList<WimsPoints> tmpSet = new ArrayList<>();
        String text;
        int index;
        //The first loop through the unmarked item list is to pull the text from the LinearLayouts
        for (int i = 0; i < unmarkedItemList.size(); i++) {
            text = ((TextView) unmarkedItemList.get(i).getChildAt(0)).getText().toString();
            //We then find the index of the product with that name, in the products list
            index = indexOfProductWithName(text);
            tmpSet.add(products.get(index));
        }
        WimsPoints point;
        LinearLayout tmpLayout;
        //In the second loop we sort the list
        for (int i = 0; i < unmarkedItemList.size(); i++) {
            //First we find the nearest neighbor between the start and the WimsPoints in the tmpSet
            point = nearestNeightbor(start, tmpSet);
            //When the nearest neighbor is found the start is updated
            start = point;
            //Then we remove the point from the tmpSet
            tmpSet.remove(point);
            //In the third loop we do the actual sorting, using point
            for (int n = 0; n < unmarkedItemList.size(); n++) {
                text = ((TextView) unmarkedItemList.get(n).getChildAt(0)).getText().toString();
                //We check if the item has the same name as point
                if (text.equalsIgnoreCase(point.getProductName())) {
                    //If we have a match we remove the item and add it on the position i from the enclosing loop
                    tmpLayout = unmarkedItemList.get(n);
                    unmarkedItemList.remove(n);
                    unmarkedItemList.add(i, tmpLayout);
                    //When the match is found we break the inner loop
                    break;
                }
            }
        }
        //We then set the visibility of the top item to GONE and add it to the currentItem layout
        unmarkedItemList.get(0).setVisibility(View.GONE);
        currentItem.addView(unmarkedItemList.get(0));
        //Then we populate the complete list
        populateCompleteList();
    }

    /**
     * A nearest neighbor algorithm. It finds the point in a set that is nearest the start point
     * @param start The start point
     * @param set The set of points for comparison
     * @return The WimsPoint closest to the start point
     */
    public WimsPoints nearestNeightbor(WimsPoints start, ArrayList<WimsPoints> set) {
        WimsPoints closestPoint = null;
        //We initialize a closest distance to the max value of a float
        float closestDistance = Float.MAX_VALUE;
        //We iterate over the set to find the nearest neighbor
        for (int i = 0; i < set.size(); i++) {
            //If the Euclidean distance between start and this point is lower than closestDistance we update it
            if (start.distance(set.get(i).x, set.get(i).y) < closestDistance) {
                //We set the closest point to be this point and update the closest distance
                closestPoint = set.get(i);
                closestDistance = start.distance(set.get(i).x, set.get(i).y);
            }
        }
        //When we have found the nearest neighbor we return it
        return closestPoint;
    }

    /**
     * A small method we use to populate the complete list. This is purely for readability sake
     */
    public void populateCompleteList() {
        completeItemList.clear();
        //We fill the complete list with the three sublists
        completeItemList.addAll(unmarkedItemList);
        completeItemList.addAll(markedItemList);
        completeItemList.addAll(unavailableItemList);
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
     * it checks whether the items in the list is in the new store, if not they are grayed out
     * @param id The newe store id
     */
    public void setStoreId(String id) {
        //If the new store id is not the same as the old, we pull a new products list from the server
        if (!id.equalsIgnoreCase(storeId)) {
            String url = "http://nielsema.ddns.net/sw8/api/store/" + id + "/products/";
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

            request(queue, url);

            LinearLayout item;
            TextView tmpText;
            //We run through the three sub lists to see if they are available
            if (!unmarkedItemList.isEmpty()) {
                //We start by looping through the unmarked item list
                for (int i = 0; i < unmarkedItemList.size(); i++) {
                    item = unmarkedItemList.get(i);
                    tmpText = (TextView) item.getChildAt(0);
                    //If there is no item with that name we gray the item out
                    if (!productsContainString(tmpText.toString())) {
                        item.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grayout));
                        tmpText.setTextColor(Color.DKGRAY);
                        //We remove the item from the unmarked list
                        unmarkedItemList.remove(item);
                        //We set its clickable state to false
                        item.setClickable(false);
                        //And we add it to the unavailable item list
                        unavailableItemList.add(item);
                    }
                }
                //Because there is a chance that some item in the unavailable item list is available in this store
                //we loop through it
                for (int i = 0; i < unavailableItemList.size(); i++) {
                    item = unavailableItemList.get(i);
                    tmpText = (TextView) item.getChildAt(0);
                    //If we find a match we remove the grayout image
                    if (productsContainString(tmpText.toString())) {
                        //TODO: If we find another background for the items this should change
                        item.setBackgroundDrawable(null);
                        tmpText.setTextColor(Color.BLACK);
                        //We remove the item from the unavailable item list and add it to the unmarked item list
                        unavailableItemList.remove(item);
                        //We re-enable its clickable state
                        item.setClickable(true);
                        unmarkedItemList.add(item);
                    }
                }
                //TODO: When we have positioning, change this to that
                sortItemList(new WimsPoints(0, 0));
            }
            //We do the same thing for marked items
            if (!markedItemList.isEmpty()) {
                for (int i = 0; i < markedItemList.size(); i++) {
                    item = markedItemList.get(i);
                    tmpText = (TextView) item.getChildAt(0);
                    if (!productsContainString(tmpText.toString())) {
                        item.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grayout));
                        tmpText.setTextColor(Color.DKGRAY);
                        ((ImageView) item.getChildAt(1)).setImageDrawable(null);
                        markedItemList.remove(item);
                        item.setClickable(false);
                        unavailableItemList.add(item);
                    }
                }
            }
            populateCompleteList();
            //Then we notify the adapter
            itemAdapter.notifyDataSetChanged();
        }

        //Regardless we set set the id
        storeId = id;
    }

    /**
     * This method checks if there is a product in the products list that contains the input string
     * @param str The product name
     * @return True if there is a match, false otherwise
     */
    public boolean productsContainString(String str) {
        //We loop through the products list to find a match
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductName().equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method works largely the same as {@link ShoppingListFragment#productsContainString(String)}.
     * This returns the index of the product
     * @param str The input string
     * @return The index of the product, null if it does not exist
     */
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

    /**
     * A getter for the store ID field
     * @return Store ID
     */
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

        //We also save the list of products for this store
        outState.putParcelableArrayList("products", products);
    }

    /**
     * This method is used to remove an item from the item list
     */
    public void removeItemFromList() {
        //When the user flings an item they are met with an AlertDialog
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.alert_message)
                .setPositiveButton(R.string.alert_pos_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //If the position for the item is within the unmarked item list we remove it from there
                        if (itemToDelete < unmarkedItemList.size()) {
                            //If it is the first item, i.e. the currentItem layout we also clear the currentItem
                            if (itemToDelete == 0) {
                                unmarkedItemList.remove(0);
                                currentItem.removeAllViewsInLayout();
                                currentItem.addView(unmarkedItemList.get(0));
                                unmarkedItemList.get(0).setVisibility(View.GONE);
                            //If the item was not the first we simply remove it
                            } else {
                                unmarkedItemList.remove(itemToDelete);
                            }
                            //If the position is within the marked items we remove the item from that list
                        } else if (itemToDelete > unmarkedItemList.size()) {
                            //If the unmarked item list is not empty we have to update the index
                            if (unmarkedItemList.size() != 0) {
                                itemToDelete = itemToDelete - unmarkedItemList.size();
                            }
                            markedItemList.remove(itemToDelete);
                        }
                        //Lastly we populate the complete list and notify the adapter
                        populateCompleteList();
                        itemAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.alert_neg_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //If the user clicks on the "No" button the dialog is dismissed
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    //Here we make a custom gesture detector for the fling event
    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            //We call the remove method
            removeItemFromList();
            return false;
        }
    }
}
