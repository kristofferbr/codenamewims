package sw805f16.codenamewims;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
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
    private WimsPoints currentPosition = new WimsPoints(0,0);

    //These variables are for the suggestion list in the searchview
    private ArrayAdapter suggestionAdapter;
    private ListView suggestionListView;
    private ArrayList<String> suggestionList;

    //These variables are for the items in the shopping list
    private ShoppingListAdapter itemAdapter;
    private ListView itemListView;
    private ArrayList<LinearItemLayout> ItemList;
    //This is a list of drawable ids for which mark the item should have
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


        View mView = inflater.inflate(R.layout.fragment_shopping_list, container, false);
        // Inflate the layout for this fragment
        //We put the inflated view in a local variable
        ItemList = new ArrayList<>();
        itemAdapter = new ShoppingListAdapter(getActivity(),
                R.layout.simple_list_view,
                ItemList);
        suggestionList = new ArrayList<>();
        suggestionAdapter = new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.simple_list_view,
                suggestionList);

        if (savedInstanceState != null) {
            loadFromSavedState(savedInstanceState);
        }

        initInterface(mView);

        //If the list of unmarked items is not empty and we are in StoreMapActivity we place the first
        //item FrameLayout
        if (getActivity() instanceof StoreMapActivity) {
            sortItemListInAdapter(new WimsPoints(0, 0));
        }

        //If this fragment was called with an ID then the storeId is set
        if (getArguments() != null) {
            String id = getArguments().getString("storeId");
            if(id != null || !id.isEmpty()) {
                setStoreId(id);
            }
            else
                setStoreId("56e6a28a28c3e3314a6849df");
        }
        else
            setStoreId("56e6a28a28c3e3314a6849df");

        String url = "http://nielsema.ddns.net/sw8/api/store/" + storeId + "/products/";
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        JSONContainer.request(queue, url);

        //Then we return the view
        return mView;
    }

    public void initInterface(View mView){
        //Then we initialize the view widgets
        currentItem = (FrameLayout) mView.findViewById(R.id.currentItem);

        EditText searchView = (EditText) mView.findViewById(R.id.item_textfield);

        itemListView = (ListView) mView.findViewById(R.id.itemList);
        itemListView.setAdapter(itemAdapter);

        suggestionListView = (ListView) mView.findViewById(R.id.suggestions);
        suggestionListView.setAdapter(suggestionAdapter);

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

        //The search view is only responsible for showing the suggestion list
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                suggestionListView.setVisibility(View.VISIBLE);
                suggestionListView.bringToFront();
                populateSuggestionList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                populateSuggestionList(s.toString());
            }
        });

        //The on click listener for the suggestion list places the suggestion in the item list


        //When clicking an item in the item list view it should mark the items, if the user is in StoreMapActivity
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity() instanceof StoreMapActivity) {
                    markUnmarkItemInAdapter(position, false);
                }
            }
        });
        //When the user performs a long press they indicate the item is skipped and receives an X next to it
        itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (getActivity() instanceof StoreMapActivity) {
                    markUnmarkItemInAdapter(position, true);
                }
                return false;
            }
        });
        //Here we set the onTouchListener for the fling event. This is for removing items
        itemListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(v instanceof LinearItemLayout) {
                    itemToDelete = itemAdapter.getPosition((LinearItemLayout)v);
                }
                detector.onTouchEvent(event);
                return false;
            }
        });
        suggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LinearItemLayout tmpLayout = (LinearItemLayout) LinearItemLayout.inflate(getActivity().getApplicationContext(), R.layout.item_layout, (ViewGroup) itemListView.getEmptyView());

                //We pull the text view from the suggestion list and resize it
                TextView tmpText;
                tmpText = (TextView) tmpLayout.getChildAt(0);
                tmpText.setText(suggestionList.get(position));

                tmpLayout.setStatus(ItemEnum.UNMARKED);
                tmpLayout.setImageId(0);
                //Then we add the layout to the item list, notify the adapter and sort the list
                itemAdapter.add(tmpLayout);
                // TODO: When we have positions, change this to that
                sortItemListInAdapter(new WimsPoints(0, 0));
                itemAdapter.notifyDataSetChanged();
                suggestionListView.setVisibility(View.GONE);
            }
        });
        suggestionListView.bringToFront();

    }

    public void loadFromSavedState(Bundle savedInstanceState){
        if (getArguments() == null) {
            setStoreId(savedInstanceState.getString("storeId"));
        }

        ArrayList<String> stringItemList = savedInstanceState.getStringArrayList("itemList");
        TextView savedTextView;
        Integer id;
        ItemEnum tmpEnum;
        ArrayList<Integer> itemImageIdList = savedInstanceState.getIntegerArrayList("markImages");

        ItemList.clear();

        //Here we pull the the list of unmarked items and refill the item list
        for (int i = 0; i < stringItemList.size();i++){

            //Here we inflate a LinearLayout with a custom layout
            LinearItemLayout tmpLayout = new LinearItemLayout(getActivity().getApplicationContext(),
                    (ViewGroup) itemListView.getEmptyView());

            savedTextView = (TextView) tmpLayout.findViewById(R.id.label);
            savedTextView.setText(stringItemList.get(i));

            id = itemImageIdList.get(i);
            tmpLayout.setImageId(id);
            tmpEnum = (ItemEnum) savedInstanceState.getSerializable(stringItemList.get(i));
            tmpLayout.setStatus(tmpEnum);
            //Then we add the newly made layout to the item list
            ItemList.add(tmpLayout);
        }

        //Lastly we pull the list of products at place it in the products variable
        products = savedInstanceState.getParcelableArrayList("products");
        JSONContainer.setProducts(products);
    }

    /**
     * This method is similar to the {@link ShoppingListAdapter#markUnmarkItem(int, boolean)},
     * however it is called from the currentItem FrameLayout
     * @param longClick Whether this method is called from with a longclick
     */
    public void markCurrentItem(boolean longClick) {
        LinearItemLayout item = (LinearItemLayout) currentItem.getChildAt(0);
        ImageView mark = (ImageView) item.getChildAt(1);
        //If longclick is false the drawable is a checkmark, otherwise the drawable is skip
        if (!longClick) {
            mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.checkmark));
            item.setImageId(R.drawable.checkmark);
        } else if (longClick) {
            mark.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.skip));
            item.setImageId(R.drawable.skip);
        }
        //Because the content of the currentItem is the first item in the unmarked list, it is removed and added to the marked list
        itemAdapter.editItemEnum(0, ItemEnum.MARKED);
        sortItemListInAdapter(new WimsPoints(0, 0));
        itemAdapter.notifyDataSetChanged();
    }

    /**
     * This method sorts the unmarked item list by nearest neighbor to a starting point
     * @param start The starting point for the sorting
     */
    public void sortItemListInAdapter(WimsPoints start) {
        currentItem.removeAllViewsInLayout();
        if(itemAdapter.getCount() != 0) {
            itemAdapter.getItem(0).setVisibility(View.VISIBLE);
            itemAdapter.sortItemList(start);
            itemAdapter.getItem(0).setVisibility(View.GONE);
            currentItem.addView(itemAdapter.getItem(0));
        }
    }

    public void markUnmarkItemInAdapter(int position, boolean longC){
        currentItem.removeAllViewsInLayout();
        itemAdapter.getItem(0).setVisibility(View.VISIBLE);
        itemAdapter.markUnmarkItem(position, longC);
        itemAdapter.getItem(0).setVisibility(View.GONE);
        currentItem.addView(itemAdapter.getItem(0));
        itemAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Here we take the storeId and ping the server for the list of products in that store
        String url = "http://nielsema.ddns.net/sw8/api/store/" + storeId + "/products/";
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        JSONContainer.request(queue, url);
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

            JSONContainer.request(queue, url);

            LinearItemLayout item;
            TextView tmpText;
            //We run through the three sub lists to see if they are available

            //We start by looping through the unmarked item list
            for (int i = 0; i < itemAdapter.getCount(); i++) {
                if (itemAdapter.getItem(i).getStatus() == ItemEnum.UNMARKED) {
                    item = itemAdapter.getItem(i);
                    tmpText = (TextView) item.getChildAt(0);
                    //If there is no item with that name we gray the item out
                    if (!JSONContainer.productsContainString(tmpText.toString())) {
                        item.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grayout));
                        tmpText.setTextColor(Color.DKGRAY);
                        //We remove the item from the unmarked list
                        //We set its clickable state to false
                        item.setClickable(false);
                        item.setStatus(ItemEnum.UNAVAILABLE);
                        item.setImageId(R.drawable.grayout);
                        itemAdapter.sortItemList(new WimsPoints(0, 0));
                    }
                }
                else if (itemAdapter.getItem(i).getStatus() == ItemEnum.UNAVAILABLE){
                    item = itemAdapter.getItem(i);
                    tmpText = (TextView) item.getChildAt(0);
                    //If we find a match we remove the grayout image
                    if (JSONContainer.productsContainString(tmpText.toString())) {
                        //TODO: If we find another background for the items this should change
                        item.setBackgroundDrawable(null);
                        tmpText.setTextColor(Color.BLACK);

                        item.setStatus(ItemEnum.UNMARKED);
                        item.setImageId(0);
                        //We re-enable its clickable state
                        item.setClickable(true);
                    }
                }
                else {
                    item = itemAdapter.getItem(i);
                    tmpText = (TextView) item.getChildAt(0);
                    if (!JSONContainer.productsContainString(tmpText.toString())) {
                        item.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.grayout));
                        //tjek om ContextCompat.getDrawable(getContext(),R.drawable.grayout) kan erstate deprecated funciton
                        tmpText.setTextColor(Color.DKGRAY);
                        ((ImageView) item.getChildAt(1)).setImageDrawable(null);
                        item.setStatus(ItemEnum.UNAVAILABLE);
                        item.setImageId(R.drawable.grayout);
                        item.setClickable(false);
                    }
                }
            }
            sortItemListInAdapter(currentPosition);
            //Then we notify the adapter
            itemAdapter.notifyDataSetChanged();
        }

        //Regardless we set set the id
        storeId = id;
    }

    /**
     * This method populates the listview with suggestions
     * @param query The query from which to populate after
     */
    private void populateSuggestionList(String query) {
        //We need to clear the list, otherwise the suggestion list explodes
        suggestionList.clear();
        //We create a text view to add to the suggestion list
        TextView tmpView = new TextView(getActivity().getApplicationContext());
        ArrayList<String> tmpList = new ArrayList<>();

        for (int i = 0; i < JSONContainer.getProducts().size(); i++) {
            tmpList.add(JSONContainer.getProducts().get(i).getProductName());
        }

        //We sort the tmpList
        tmpList = SearchRanking.rankSearchResults(query, tmpList);

        for (String str : tmpList) {
            //We add the string to the suggestion list
            suggestionList.add(str);
        }

        //Then we notify the adapter that the list is modified
        suggestionAdapter.notifyDataSetChanged();
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
        ArrayList<Integer> ItemIdList = new ArrayList<>();
        ArrayList<ItemEnum> enumList = new ArrayList<>();
        Integer id;
        TextView textView;
        String text;
        //We save the text in the unmarked item list
        for (int i = 0; i < itemAdapter.getCount(); i++) {
            textView = (TextView) itemAdapter.getItem(i).getChildAt(0);
            text = textView.getText().toString();
            stringItemList.add(text);

            id = itemAdapter.getItem(i).getImageId();
            ItemIdList.add(id);

            outState.putSerializable(text, itemAdapter.getItem(i).getStatus());
        }
        outState.putStringArrayList("itemList", stringItemList);

        outState.putIntegerArrayList("markImages", ItemIdList);

        //We also save the list of products for this store
        outState.putParcelableArrayList("products", JSONContainer.getProducts());
    }

    /*public boolean saveShoppingList() {
        FileOutputStream fos = getContext().openFileOutput("itemList", Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Shopping_List", shoppingArrayList.size());

        for (int i = 0; i < shoppingArrayList.size(); i++) {
            mEdit1.remove("Shopping_" + i);
            mEdit1.putString("Shopping_" + i, shoppingArrayList.get(i).name);
        }
        return mEdit1.commit();
    }*/

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
                        LinearItemLayout deletee = itemAdapter.getItem(itemToDelete);

                        //If it is the first item, i.e. the currentItem layout we also clear the currentItem
                        if (itemToDelete == 0 && deletee.getStatus() == ItemEnum.UNMARKED) {
                            itemAdapter.remove(deletee);
                            currentItem.removeAllViewsInLayout();
                            currentItem.addView(itemAdapter.getItem(0));
                            itemAdapter.getItem(0).setVisibility(View.GONE);
                        //If the item was not the first we simply remove it
                        } else {
                            itemAdapter.remove(deletee);
                        }
                        //Lastly we notify the adapter
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
