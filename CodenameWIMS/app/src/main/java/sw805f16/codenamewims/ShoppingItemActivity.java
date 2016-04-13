package sw805f16.codenamewims;

import android.os.Bundle;
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

import java.util.ArrayList;

/**
 * Created by Netray on 04/04/2016.
 */
public class ShoppingItemActivity extends WimsActivity {

    int checks = 0; // This variable is used to keep track of how many items have been checked off.

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shopping_list);

        // Get the bundle from the intent received.
        Bundle b = getIntent().getExtras();
        final ArrayList<String> items = b.getStringArrayList("itemsList");

        WimsButton deleteButton = new WimsButton(getApplicationContext(), getResources().getDrawable(R.drawable.delete_icon));
        deleteButton.setVisibility(View.INVISIBLE);
        deleteButton.setId(R.id.wims_action_bar_shopping_delete);
        addWimsButtonToActionBar(deleteButton, RIGHT);

        visibility(items);

        // Retrieve the title & set title in actionbar.
        final String title = b.getString("title");
        setActionBarTitle(title);

        listItems(items, title);

        Button addButton = (Button)findViewById(R.id.item_add_btn);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "";
                EditText editText = (EditText)findViewById(R.id.item_textfield);
                s = editText.getText().toString();

                // Just a check so it's not possible to add an empty string.
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

    }

    // This method will list all the items from the received shopping list. Also used to list items when new is added currently.
    public void listItems(ArrayList<String> items, final String title) {

        GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        gridLayout.removeAllViews();

        for (int i = 0; i < items.size(); i++){

            LayoutInflater inflater = LayoutInflater.from(this);
            RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_item, null, false);
            CheckBox checkBox = (CheckBox) layout.findViewById(R.id.itemListCheckBox);
            TextView itemsText = (TextView) layout.findViewById(R.id.itemListName);
            itemsText.setText(items.get(i));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if (isChecked){
                        checks++;
                        changeActionBar(checks, title);
                    }
                    else {
                        checks--;
                        changeActionBar(checks, title);
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
    public void visibility(ArrayList<String> items){
        GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        if (!items.isEmpty()){
            gridLayout.setVisibility(View.VISIBLE);
        } else {
            gridLayout.setVisibility(View.INVISIBLE);
        }
    }

    // This method changes the title of the action bar to the amount of items checked
    // TODO: Remove hardcoded values.
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
}
