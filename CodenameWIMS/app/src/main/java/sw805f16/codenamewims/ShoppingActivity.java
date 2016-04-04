package sw805f16.codenamewims;

/**
 * Created by Netray on 01/04/2016.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ShoppingActivity extends AppCompatActivity {

    public String[] mTitles = {"Bill", "Mary", "Harold", "John", "Deadpool"};
    public String[] mItems = {"Chicken, Beer, Pasta, Butter", "Milk", "Bread", "", "Lemonade"};
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);


        addShoppingList("Harold");
        addShoppingList("Michael");
        addShoppingList("John");
        addShoppingList("Elliot");
        addShoppingList("Harold");
        addShoppingList("Michael");
        addShoppingList("John");
        addShoppingList("Elliot");
        addShoppingList("Harold");
        addShoppingList("Michael");
        addShoppingList("John");
        addShoppingList("Elliot");

    }

    public void addShoppingList(String name){
        final ArrayList<String> items = new ArrayList<>();

        items.add("Chicken");
        items.add("Butter");
        items.add("Chili Cheese Chops");
        items.add("Milk");

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_note, null, false);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShoppingItemActivity.class);
                Bundle b = new Bundle();
                b.putStringArrayList("itemsList", items);

                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

        TextView title = (TextView)layout.findViewById(R.id.shopping_title);
        title.setText(name);

        TextView titems = (TextView)layout.findViewById(R.id.shopping_items);
        titems.setText("Butter\n" + "Chicken\n" + "Milk\n" + "Chips");

        GridLayout gridLayout = (GridLayout)findViewById(R.id.shopping_lists);
        gridLayout.addView(layout);

    }
}
