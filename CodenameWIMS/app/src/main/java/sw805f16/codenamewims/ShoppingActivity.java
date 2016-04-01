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
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ShoppingActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);


        addShoppingList("Harold");
        addShoppingList("Michael");
        addShoppingList("John");
        addShoppingList("Elliot");

    }

    public void addShoppingList(String name){
        List<String> items = new ArrayList<String>();

        items.add("Chicken");
        items.add("Butter");
        items.add("Chili Cheese Chops");
        items.add("Milk");

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_note, null, false);

        TextView title = (TextView)layout.findViewById(R.id.shopping_title);
        title.setText(name);

        TextView titems = (TextView)layout.findViewById(R.id.shopping_items);
        titems.setText("Butter");

        LinearLayout linear = (LinearLayout)findViewById(R.id.shopping_lists);
        linear.addView(layout);


    }
}
