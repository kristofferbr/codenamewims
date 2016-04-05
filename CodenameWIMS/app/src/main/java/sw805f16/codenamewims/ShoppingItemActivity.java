package sw805f16.codenamewims;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Netray on 04/04/2016.
 */
public class ShoppingItemActivity extends AppCompatActivity {

    private Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shopping_list);

        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the bundle from the intent received.
        Bundle b = getIntent().getExtras();
        ArrayList<String> items = b.getStringArrayList("itemsList");

        // Retrieve the title & set title in actionbar.
        String title = b.getString("title");
        setTitle(title);

        listItems(items);

    }

    // This method will list all the items from the received shopping list.
    public void listItems(ArrayList<String> items) {

        for (int i = 0; i < items.size(); i++){

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_item, null, false);
            TextView itemsText = (TextView)layout.findViewById(R.id.itemListName);
            itemsText.setText(items.get(i));

            listItemsSupport(layout);
        }
    }

    // Just a support method, it could, and probably should be put together with previously method. Was some difficulties when creating this.
    public void listItemsSupport(View v){

        GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        gridLayout.addView(v);
    }

}
