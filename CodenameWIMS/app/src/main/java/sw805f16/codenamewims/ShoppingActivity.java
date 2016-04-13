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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ShoppingActivity extends WimsActivity {

    public final ArrayList<ShoppingListClass> shoppingArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        Button addButton = (Button)findViewById(R.id.shopping_add_btn);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = (EditText)findViewById(R.id.shopping_textfield);
                String shoppingListTextField = editText.getText().toString();
                if (shoppingListTextField.equalsIgnoreCase("")) {
                    Toast.makeText(ShoppingActivity.this, R.string.please_enter_a_name, Toast.LENGTH_SHORT).show();
                }
                else {
                    editText.setText("");
                    AddShoppingList(shoppingListTextField);
                }
            }
        });

        LoadShoppingList(this);
        DisplayShoppingList();
        saveShoppingList();
    }

    public class ShoppingListClass {
        String name;
        ArrayList items;
        public ShoppingListClass(String name, ArrayList items){
            this.name = name;
            this.items = items;
        }
    }

    public boolean saveShoppingList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();
    /* mItems is an array */
        int shoppingListSize = shoppingArrayList.size();
        mEdit1.putInt("Shopping_List", shoppingListSize);

        for (int i = 0; i < shoppingListSize; i++) {
            mEdit1.remove("Shopping_" + i);
            mEdit1.putString("Shopping_" + i, shoppingArrayList.get(i).name);
        }
        return mEdit1.commit();
    }

    public boolean saveShoppingList(String shoppingListName)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();
    /* mItems is an array */
        int shoppingListSize = shoppingArrayList.size();
        mEdit1.putInt("Shopping_List", shoppingListSize + 1);

        for(int i=0;i< shoppingListSize;i++)
        {
            mEdit1.remove("Shopping_" + i);
            mEdit1.putString("Shopping_" + i, shoppingArrayList.get(i).name);
        }

        mEdit1.remove("Shopping_" + shoppingListSize);
        mEdit1.putString("Shopping_" + shoppingListSize, shoppingListName);

        return mEdit1.commit();
    }

    public void LoadShoppingList(Context mContext)
    {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        shoppingArrayList.clear();
        int size = mSharedPreference1.getInt("Shopping_List", 0);

        for(int i=0;i<size;i++)
        {
            String shoppingListName = mSharedPreference1.getString("Shopping_" + i, null);
            ShoppingListClass mItems = LoadItemList(shoppingListName, mContext);
            shoppingArrayList.add(i, mItems);
        }
    }

    private ShoppingListClass LoadItemList(String name, Context mContext) {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
        int size = mSharedPreference1.getInt("Item_List", 0);
        ArrayList mItems = new ArrayList();

        for(int i=0;i<size;i++) {
            String mItemName = mSharedPreference1.getString("Item_" + i, null);
            mItems.add(i,mItemName);
        }
        ShoppingListClass shoppingList = new ShoppingListClass(name, mItems);
        return shoppingList;
    }

    public boolean AddShoppingList(final String shoppingListName){
        final ArrayList itemList = new ArrayList();
        Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
        Bundle b = new Bundle();
        b.putStringArrayList("itemsList", itemList);
        b.putString("title", shoppingListName);
        intent.putExtras(b);

        saveShoppingList(shoppingListName);

        startActivity(intent);

        return true;
    }

    // This method adds takes a name for a shopping list, and an ArrayList of items on that list.
    // It will then display it.
    public void DisplayShoppingList(){
GridLayout layout2 = (GridLayout) findViewById(R.id.shopping_lists);
        layout2.removeAllViews();
        int size = shoppingArrayList.size();
        for(int i=0;i<size;i++) {

            final String name = shoppingArrayList.get(i).name;
            final ArrayList mItems = shoppingArrayList.get(i).items;

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_note, null, false);

            // OnClickListener for accessing the individual shopping lists.
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
                    Bundle b = new Bundle();
                    b.putStringArrayList("itemsList", mItems);
                    b.putString("title", name);
                    saveShoppingList();

                    intent.putExtras(b);
                    startActivity(intent);
                }
            });

            // Setting the title/name of the shopping list.
            TextView title = (TextView) layout.findViewById(R.id.shopping_title);
            title.setText(name);

            // This for loop adds a number of textviews to a gridlayout to display.
            // This is done to ensure string length isn't an issue.
            for (int n = 0; n < mItems.size() && n < 5; n++) {

                TextView textViewLayout = (TextView) inflater.inflate(R.layout.text_view, null, false);
                TextView textView = (TextView) textViewLayout.findViewById(R.id.shopping_items);
                textView.setText(mItems.get(n).toString());

                GridLayout gridLayoutTV = (GridLayout) layout.findViewById(R.id.shopping_textViews_container);
                gridLayoutTV.addView(textViewLayout);
            }
            if (6 < mItems.size()) {
                TextView textViewLayout = (TextView) inflater.inflate(R.layout.text_view, null, false);
                TextView textView = (TextView) textViewLayout.findViewById(R.id.shopping_items);
                textView.setText("...");

                GridLayout gridLayoutTV = (GridLayout) layout.findViewById(R.id.shopping_textViews_container);
                gridLayoutTV.addView(textViewLayout);
            }

            // Adding the layout we just created to the shopping list layout for complete display.
            GridLayout gridLayout = (GridLayout) findViewById(R.id.shopping_lists);
            gridLayout.addView(layout);
        }
    }
}
