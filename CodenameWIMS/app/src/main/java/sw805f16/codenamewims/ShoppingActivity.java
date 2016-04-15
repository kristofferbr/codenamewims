package sw805f16.codenamewims;

/**
 * Created by Netray on 01/04/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ShoppingActivity extends WimsActivity {

    // Global ArrayList containing all shopping lists.
    public final ArrayList<ShoppingListClass> shoppingArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        Button addButton = (Button)findViewById(R.id.shopping_add_btn);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = (EditText) findViewById(R.id.shopping_textfield);
                String shoppingListTextField = editText.getText().toString();

                //Ignore empty text fields
                if (shoppingListTextField.equalsIgnoreCase("")) {
                    Toast.makeText(ShoppingActivity.this, R.string.please_enter_a_name, Toast.LENGTH_SHORT).show();
                } else {
                    editText.setText("");
                    addShoppingList(shoppingListTextField);
                }
            }
        });
        displayShoppingList();
    }

    /**
     * Update shopping list upon return from ShoppingItemActivity
     */
    @Override
    public void onResume() {
        super.onResume();
        displayShoppingList();
    }

    /**
     * Class of shoppingArrayList.
     */
    public class ShoppingListClass {
        String name;
        ArrayList items;
        public ShoppingListClass(String name, ArrayList items){
            this.name = name;
            this.items = items;
        }
    }

    /**
     * Save method for saving shopping list. Saves locally in shared preference.
     * @return Commits changes.
     */
    public boolean saveShoppingList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Shopping_List", shoppingArrayList.size());

        for (int i = 0; i < shoppingArrayList.size(); i++) {
            mEdit1.remove("Shopping_" + i);
            mEdit1.putString("Shopping_" + i, shoppingArrayList.get(i).name);
        }
        return mEdit1.commit();
    }

    /**
     * Save new shopping list
     * @param shoppingListName Name of shopping list to be saved
     */
    public void saveShoppingList(String shoppingListName)
    {
        ShoppingListClass shoppingList = new ShoppingListClass(shoppingListName, null);
        shoppingArrayList.add(shoppingArrayList.size(), shoppingList);
        saveShoppingList();
    }

    /**
     * Loads shopping list from shared preference into ShoppingArrayList
     * @param mContext Activity context.
     */
    public void loadShoppingList(Context mContext)
    {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        shoppingArrayList.clear();
        int size = mSharedPreference1.getInt("Shopping_List", 0);

        for(int i=0;i<size;i++)
        {
            String shoppingListName = mSharedPreference1.getString("Shopping_" + i, null);
            ShoppingListClass mItems = loadItemList(shoppingListName, mContext);
            shoppingArrayList.add(i, mItems);
        }
    }

    /**
     * Loads items from local storage into a ShoppingListClass
     * @param name Name of the shopping list
     * @param mContext
     * @return ShoppingListClass with shopping list name and ArrayList of items in that shopping list
     */
    private ShoppingListClass loadItemList(String name, Context mContext) {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        int size = mSharedPreference1.getInt("Item_List_" + name, 0);
        ArrayList mItems = new ArrayList();

        for(int i=0;i<size;i++) {
            String mItemName = mSharedPreference1.getString("Item_List_" + name + i, null);
            mItems.add(i,mItemName);
        }
        ShoppingListClass shoppingList = new ShoppingListClass(name, mItems);
        return shoppingList;
    }

    /**
     * Method called for creating new shopping list and starting ShoppingItemActivity
     * @param shoppingListName Name of the new shopping list
     * @return true
     */
    public boolean addShoppingList(final String shoppingListName){
        final ArrayList itemList = new ArrayList();
        Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
        Bundle b = new Bundle();
        b.putStringArrayList("itemsList", itemList);
        b.putString("title", shoppingListName);
        intent.putExtras(b);
        hideKeyboard();
        saveShoppingList(shoppingListName);
        startActivity(intent);
        return true;
    }

    /**
     * Deletes a shopping list.
     * @param shoppingListAddress index location in ShoppingListArray of shopping list to be deleted
     */
   public void deleteShoppingList(final int shoppingListAddress) {

        shoppingArrayList.remove(shoppingListAddress);
        saveShoppingList();
    }

    // This method adds takes a name for a shopping list, and an ArrayList of items on that list.
    // It will then display it.

    /**
     * This method populates the GridView with shopping list cards.
     */
    public void displayShoppingList(){

        loadShoppingList(getApplicationContext());

        GridLayout gridLayout = (GridLayout) findViewById(R.id.shopping_lists);

        //Start from a clear screen.
        gridLayout.removeAllViews();

        //Increment through ShoppingArrayList and create a note for each
        for(int i=0;i<shoppingArrayList.size();i++) {
            final int currentI = i;
            final String name = shoppingArrayList.get(i).name;
            final ArrayList mItems = shoppingArrayList.get(i).items;

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_note, null, false);

            // OnClickListener for accessing the individual shopping lists.
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ShoppingListActivity.class);
                    Bundle b = new Bundle();
                    b.putStringArrayList("itemsList", mItems);
                    b.putString("title", name);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });

            // onLongClickListener for deleting individual shopping lists.
            layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteShoppingList(currentI);
                    displayShoppingList();
                    return true;
                }
            });

            // Setting the title/name of the shopping list.
            TextView title = (TextView) layout.findViewById(R.id.shopping_title);
            title.setText(name);

            // This for loop adds a number of textviews to a gridlayout to display.
            // These are the items in the shopping list
            // No more than 5 items for each note is displayed in this view
            for (int n = 0; n < mItems.size() && n < 6; n++) {
                TextView textViewLayout = (TextView) inflater.inflate(R.layout.text_view, null, false);
                TextView textView = (TextView) textViewLayout.findViewById(R.id.shopping_items);
                if (n==5)
                    textView.setText("...");
                else
                    textView.setText(mItems.get(n).toString());

                GridLayout gridLayoutTV = (GridLayout) layout.findViewById(R.id.shopping_textViews_container);
                gridLayoutTV.addView(textViewLayout);

            }
            // Adding the layout we just created to the shopping list layout for complete display.
            gridLayout.addView(layout);
        }
    }

    /**
     * Hide keyboard when clicking outside of text field.
     */
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}