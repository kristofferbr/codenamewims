package sw805f16.codenamewims;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Netray on 04/04/2016.
 */
public class ShoppingItemActivity extends WimsActivity {

    int checks = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shopping_list);


        WimsButton delete = new WimsButton(this, this.getResources().getDrawable(R.drawable.delete_icon));

        addWimsButtonToActionBar(delete, RIGHT);


        // Get the bundle from the intent received.
        Bundle b = getIntent().getExtras();
        final ArrayList<String> items = b.getStringArrayList("itemsList");


        visibility(items);

        // Retrieve the title & set title in actionbar.
        final String title = b.getString("title");
        setActionBarTitle(title);
        //setTitle(title);

        listItems(items, title);

        Button addButton = (Button)findViewById(R.id.item_add_btn);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "";
                EditText editText = (EditText)findViewById(R.id.item_textfield);
                s = editText.getText().toString();

                if (s.equalsIgnoreCase("")) {
                    Toast.makeText(ShoppingItemActivity.this, R.string.Please_enter_an_item_name, Toast.LENGTH_SHORT).show();
                }
                else {
                    editText.setText("");
                    items.add(s);
                    visibility(items);
                    listItems(items, title);
                }
            }
        });

        //CheckBox checkBox = (CheckBox)findViewById(R.id.itemListCheckBox);


        /*checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isChecked){

                    relativeLayout.setBackgroundResource(R.color.Indigo200);
                }
                else {
                    relativeLayout.setBackgroundResource(R.color.colorPrimary);
                }
            }
        });*/

    }

    // This method will list all the items from the received shopping list.
    public void listItems(ArrayList<String> items, final String title) {

        GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        gridLayout.removeAllViews();

        for (int i = 0; i < items.size(); i++){

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_item, null, false);
            CheckBox checkBox = (CheckBox) layout.findViewById(R.id.itemListCheckBox);
            TextView itemsText = (TextView) layout.findViewById(R.id.itemListName);
            itemsText.setText(items.get(i));

            TextView amountText = (TextView)layout.findViewById(R.id.itemListAmount);
            amountText.setText("x" + 100);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if (isChecked){
                        checks++;
                        changeTitle(checks, title);
                        changeColour(checks);
                    }
                    else {
                        checks--;
                        changeTitle(checks, title);
                        changeColour(checks);
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

    public void visibility(ArrayList<String> items){
        if (!items.isEmpty()){
            GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
            gridLayout.setVisibility(View.VISIBLE);
        }
    }

    public void changeTitle(int checks, String title) {
        TextView textView = (TextView)findViewById(R.id.wims_action_bar_title);

        if (checks != 0) {
            if (checks == 1){
                textView.setText(checks + " Valgt");
            } else {
                textView.setText(checks + " Valgte");
            }
        } else {
            textView.setText(title);
        }
    }

    public void changeColour(int checks){
        final RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.wims_action_bar_primary_view);

        if (checks != 0){
            relativeLayout.setBackgroundResource(R.color.RoyalBlue);
        }
        else {
            relativeLayout.setBackgroundResource(R.color.colorPrimary);
        }
    }

    public void changeButtons(int checks) {


        if (checks != 0) {


        } else {

        }
    }
}
