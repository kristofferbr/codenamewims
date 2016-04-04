package sw805f16.codenamewims;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shopping_list);

        Bundle b = getIntent().getExtras();
        ArrayList<String> items = b.getStringArrayList("itemsList");

        listItems(items);

    }

    public void listItems(ArrayList<String> items) {

        for (int i = 0; i < items.size(); i++){

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_item, null, false);
            TextView itemsText = (TextView)layout.findViewById(R.id.itemListName);
            itemsText.setText(items.get(i));

            listItemsSupport(layout);
        }
    }

    public void listItemsSupport(View v){

        GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        gridLayout.addView(v);
    }

}
