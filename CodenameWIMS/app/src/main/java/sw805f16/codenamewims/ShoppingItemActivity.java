package sw805f16.codenamewims;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Netray on 04/04/2016.
 */
public class ShoppingItemActivity extends WimsActivity {

    ArrayList itemArrayList = new ArrayList(); // This arrayList is used to keep the items in.
    int checks = 0; // This variable is used to keep track of how many items have been checked off.
    ArrayList<Integer> ticked = new ArrayList<>(); // This keeps track of the items being ticked off.
    String title = ""; // String variable used to control the title in the action bar.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shopping_list);

        // Get the bundle from the intent received.
        /*Bundle b = getIntent().getExtras();

        // Fetch the title of a shopping list and set the action bar title.
        title = b.getString("title");
        setActionBarTitle(title);

        // Create a delete button and hide it, for use when items are ticked off.
        WimsButton deleteButton = new WimsButton(getApplicationContext(), getResources().getDrawable(R.drawable.delete_icon));
        deleteButton.setVisibility(View.INVISIBLE);
        deleteButton.setId(R.id.wims_action_bar_shopping_delete);
        addWimsButtonToActionBar(deleteButton, RIGHT);

        // Add button used to add new items.
        Button addButton = (Button)findViewById(R.id.item_add_btn);

        // The text field for adding new items.
        final EditText editText = (EditText)findViewById(R.id.item_textfield);

        // Delete button functionality
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemArrayList.size() == ticked.size()) {
                    itemArrayList.clear();
                }
                else{
                    while (ticked.size() != 0) {
                        int maxIndex = getMaxIntIndex();
                        itemArrayList.remove(ticked.get(maxIndex).intValue());
                        ticked.remove(maxIndex);
                    }
                }
                saveItemList();
                listItems();
                ticked.clear();
                checks = 0;
                changeActionBar(checks, title);
            }
        });

        // Add button functionality
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        // This overrides the "done" button on the keyboard.
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addItem();
                }

                return false;
            }
        });

        // List all items when first activity is first created.
        listItems();
    }

    /**
     * Get the index value of the element with the highest number in the ArrayList Ticked
     * @return interger value of the index with the highest value
     */
    private int getMaxIntIndex() {
        int max = Integer.MIN_VALUE;
        int maxIndex = Integer.MIN_VALUE;
        for(int i=0; i<ticked.size(); i++){
            if(ticked.get(i) > max){
                max = ticked.get(i);
                maxIndex = i;
            }
        }
        return maxIndex;
    }


    // Method for adding an item to the list.
    public void addItem(){
        final EditText editText = (EditText)findViewById(R.id.item_textfield);
        String s = "";
        s = editText.getText().toString();

        // Ignoring of empty string.
        if (s.equalsIgnoreCase("")) {
            Toast.makeText(ShoppingItemActivity.this, R.string.Please_enter_an_item_name, Toast.LENGTH_SHORT).show();
        }
        else {
            editText.setText("");
            saveItemList(s);
            listItems();
        }
        hideKeyboard();*/
    }

    // Method for saving an item to local memory.
    public boolean saveItemList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Item_List_" + title, itemArrayList.size());

        for (int i = 0; i < itemArrayList.size(); i++) {
            mEdit1.remove("Item_List_" + title + i);
            mEdit1.putString("Item_List_" + title + i, itemArrayList.get(i).toString());
        }
        return mEdit1.commit();
    }

    // Method for saving an item and adding a name.
    public void saveItemList(String name) {

        itemArrayList.add(itemArrayList.size(), name);
        saveItemList();
    }

    // Method to load items from shopping list.
    public void loadItemList(Context mContext)
    {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        itemArrayList.clear();
        int size = mSharedPreference1.getInt("Item_List_" + title, 0);

        for(int i=0;i<size;i++)
        {
            itemArrayList.add(mSharedPreference1.getString("Item_List_" + title + i, null));
        }
    }

    // This method will list all the items from the received shopping list. Also used to list items when new is added currently.
    public void listItems() {
        loadItemList(getApplicationContext());

        visibility();

        final GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        gridLayout.removeAllViews(); // Removes all views to not create conflicts.

        for (int i = 0; i < itemArrayList.size(); i++){

            final int currentIteration = i;
            LayoutInflater inflater = LayoutInflater.from(this);
            final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_item, null, false);
            final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.itemListCheckBox);
            TextView itemsText = (TextView) layout.findViewById(R.id.itemListName);
            itemsText.setText(itemArrayList.get(i).toString());

            // Listener to keep track of items being ticked off and on.
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if (isChecked){
                        checks++;
                        changeActionBar(checks, title);
                        ticked.add(currentIteration);
                    }
                    else {
                        checks--;
                        changeActionBar(checks, title);
                        for (int j = 0; j < ticked.size(); j++) {
                            if (ticked.get(j).equals(currentIteration)) {
                                ticked.remove(j);
                            }
                        }
                    }
                }
            });
            listItemsSupport(layout);
        }
    }

    // Just a support method, it could, and probably should be put together with previously method. Was some difficulties when creating this.
    public void listItemsSupport(View v){
        GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        gridLayout.addView(v);
    }

    // This method is to ensure that when the list is empty, the screen is clear.
    public void visibility(){
        GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        if (!itemArrayList.isEmpty()){
            gridLayout.setVisibility(View.VISIBLE);
        } else {
            gridLayout.setVisibility(View.INVISIBLE);
        }
    }

    // This method changes the title of the action bar to the amount of items checked.
    public void changeTitle(int checks, String title) {
        if (checks != 0) {
            if (checks == 1) {
                setActionBarTitle(checks + getString(R.string.marked_singulare));
            } else {
                setActionBarTitle(checks + getString(R.string.marked_plural));
            }
        } else {
            setActionBarTitle(title);
        }
    }

    // This method changes the colour of the action bar when items are checked.
    public void changeColour(int checks){
        final RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.wims_action_bar_primary_view);

        if (checks != 0){
            relativeLayout.setBackgroundResource(R.color.RoyalBlue);
        }
        else {
            relativeLayout.setBackgroundResource(R.color.colorPrimary);
        }
    }

    // This method changed the visibility of the buttons in the action bar - This is only feasible because no other buttons exist when all are unchecked.
    public void changeButtons(int checks) {

        WimsButton deleteButton = (WimsButton)findViewById(R.id.wims_action_bar_shopping_delete);

        if (checks != 0) {
            deleteButton.setVisibility(View.VISIBLE);

        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }
    }

    // This method simply call the change methods respectively.
    public void changeActionBar(int checks, String title) {
        changeTitle(checks, title);
        changeColour(checks);
        changeButtons(checks);
    }

    // Method to hide keyboard.
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
