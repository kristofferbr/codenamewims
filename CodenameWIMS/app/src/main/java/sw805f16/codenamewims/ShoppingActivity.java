package sw805f16.codenamewims;

/**
 * Created by Netray on 01/04/2016.
 */

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ShoppingActivity extends WimsActivity {

    public final ArrayList mItems = new ArrayList();
    public String shoppingListName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        Button addButton = (Button)findViewById(R.id.shopping_add_btn);
        final EditText editText = (EditText)findViewById(R.id.shopping_textfield);

        //WimsButton test = new WimsButton(this, this.getResources().getDrawable(R.drawable.no_icon));
        //addWimsButtonToActionBar(test, RIGHT);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shoppingListName = editText.getText().toString();
                final ArrayList itemList = new ArrayList();
                if (shoppingListName.equalsIgnoreCase("")) {
                    Toast.makeText(ShoppingActivity.this, "Please enter a name.", Toast.LENGTH_SHORT).show();
                }
                else {
                    addShoppingList(shoppingListName, itemList);
                    editText.setText("");

                    Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
                    Bundle b = new Bundle();
                    b.putStringArrayList("itemsList", itemList);
                    b.putString("title", shoppingListName);

                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        });

        loadArray(this);
        // This is testing data, it should be removed when ready.
        // TODO: Remove this bit of code when ready to input more.
        mItems.add("Chicken");
        mItems.add("Beer");
        mItems.add("Lemonade");
        mItems.add("Chocolate");
        mItems.add("supercalifrigeratorInthebutt");

        addShoppingList("Harold", mItems);

        addShoppingList("Tony Tony Chop", mItems);
        saveArray();
    }

    public boolean saveArray()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();
    /* mItems is an array */
        mEdit1.putInt("Status_size", mItems.size());

        for(int i=0;i<mItems.size();i++)
        {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, mItems.get(i).toString());
        }

        return mEdit1.commit();
    }

    public void loadArray(Context mContext)
    {
        SharedPreferences mSharedPreference1 =   PreferenceManager.getDefaultSharedPreferences(mContext);
        mItems.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);

        for(int i=0;i<size;i++)
        {
            mItems.add(mSharedPreference1.getString("Status_" + i, null));
        }

    }
    // This method adds takes a name for a shopping list, and an ArrayList of items on that list. It will then display it.
    public void addShoppingList(final String name, final ArrayList<String> items){

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_note, null, false);

        // OnClickListener for accessing the individual shopping lists.
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("itemsList", items);
                b.putString("title", name);

                intent.putExtras(b);
                startActivity(intent);
            }
        });

        // Setting the title/name of the shopping list.
        TextView title = (TextView)layout.findViewById(R.id.shopping_title);
        title.setText(name);

        // This for loop adds a number of textviews to a gridlayout to display.
        // This is done to ensure string length isn't an issue.
        for (int i = 0; i < items.size(); i++) {

            TextView textViewLayout = (TextView) inflater.inflate(R.layout.text_view, null, false);
            TextView textView = (TextView)textViewLayout.findViewById(R.id.shopping_items);
            textView.setText(items.get(i));

            GridLayout gridLayoutTV = (GridLayout)layout.findViewById(R.id.shopping_textViews_container);
            gridLayoutTV.addView(textViewLayout);
        }

        // Adding the layout we just created to the shopping list layout for complete display.
        GridLayout gridLayout = (GridLayout)findViewById(R.id.shopping_lists);
        gridLayout.addView(layout);
    }
}
