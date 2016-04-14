package sw805f16.codenamewims;

import android.os.Bundle;
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

    int checks = 0; // This variable is used to keep track of how many items have been checked off.
    ArrayList ticked = new ArrayList();
    ArrayList<String> texts = new ArrayList();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shopping_list);

        // Get the bundle from the intent received.
        Bundle b = getIntent().getExtras();
        final ArrayList<String> items = b.getStringArrayList("itemsList");
        final String title = b.getString("title");

        WimsButton deleteButton = new WimsButton(getApplicationContext(), getResources().getDrawable(R.drawable.delete_icon));
        deleteButton.setVisibility(View.INVISIBLE);
        deleteButton.setId(R.id.wims_action_bar_shopping_delete);
        addWimsButtonToActionBar(deleteButton, RIGHT);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GridLayout gridLayout = (GridLayout) findViewById(R.id.itemListGrid);
                for (int i = 0; i < ticked.size(); i++) {
                    gridLayout.removeView((ViewGroup) ticked.get(i));
                }

                // TODO: This method has an error. It currently removes all items with same name, if removing just 1 of them. Maybe change things to a listview with an adapter
                for (int i = 0; i < items.size(); i++) {
                    for (int j = 0; j < ticked.size(); j++)
                        if (items.get(i).equals(texts.get(j))) {
                            items.remove(i);
                        }
                }
                visibility(items);
                checks = 0;
                changeActionBar(checks, title);

            }
        });

        visibility(items);

        // Set title in actionbar.
        setActionBarTitle(title);

        listItems(items, title);

        Button addButton = (Button)findViewById(R.id.item_add_btn);
        final EditText editText = (EditText)findViewById(R.id.item_textfield);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = "";
                //EditText editText = (EditText)findViewById(R.id.item_textfield);
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
                hideKeyboard();
            }
        });



        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String s = "";
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
                    hideKeyboard();
                }

                return false;
            }
        });

    }

    // This method will list all the items from the received shopping list. Also used to list items when new is added currently.
    public void listItems(final ArrayList<String> items, final String title) {

        final GridLayout gridLayout = (GridLayout)findViewById(R.id.itemListGrid);
        gridLayout.removeAllViews();

        for (int i = 0; i < items.size(); i++){

            LayoutInflater inflater = LayoutInflater.from(this);
            final RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_item, null, false);
            final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.itemListCheckBox);
            TextView itemsText = (TextView) layout.findViewById(R.id.itemListName);
            itemsText.setText(items.get(i));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    if (isChecked){
                        checks++;
                        changeActionBar(checks, title);
                        RelativeLayout r = (RelativeLayout) ((ViewGroup) checkBox.getParent());
                        ticked.add(r);
                        TextView tw = (TextView)r.findViewById(R.id.itemListName);
                        String s = tw.getText().toString();
                        texts.add(s);
                    }
                    else {
                        checks--;
                        changeActionBar(checks, title);
                        RelativeLayout r = (RelativeLayout) ((ViewGroup) checkBox.getParent());
                        ticked.remove(r);
                        TextView tw = (TextView)r.findViewById(R.id.itemListName);
                        String s = tw.toString();
                        texts.remove(s);
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

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
