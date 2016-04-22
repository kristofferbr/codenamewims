package sw805f16.codenamewims;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class ShoppingListActivity extends WimsActivity {

    private Toolbar toolbar;
    String title = "";

    ShoppingListFragment fragment = new ShoppingListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_shopping_list);

        Bundle b = getIntent().getExtras();
        // my_child_toolbar is defined in the layout file
        title = b.getString("title");
        setActionBarTitle(title);

        WimsButton startScreenButton = new WimsButton(getApplicationContext(), getResources().getDrawable(R.drawable.no_icon));
        WimsButton storeMapButton = new WimsButton(getApplicationContext(), getResources().getDrawable(R.drawable.no_icon));
        WimsButton deleteButton = new WimsButton(getApplicationContext(), getResources().getDrawable(R.drawable.delete_icon));

        startScreenButton.setVisibility(View.INVISIBLE);
        startScreenButton.setId(R.id.wims_action_bar_transition_start);
        addWimsButtonToActionBar(startScreenButton, RIGHT);

        storeMapButton.setVisibility(View.INVISIBLE);
        storeMapButton.setId(R.id.wims_action_bar_transition_storemap);
        addWimsButtonToActionBar(storeMapButton, RIGHT);

        deleteButton.setVisibility(View.INVISIBLE);
        deleteButton.setId(R.id.wims_action_bar_shopping_delete);
        addWimsButtonToActionBar(deleteButton, RIGHT);

        //The buttons are for transitioning to the MainActivity and StoreMapActivity, respectively
        startScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                transitionToStartScreen();
            }
        });
        storeMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                transitionToStoreMap();
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (getIntent().getStringExtra("storeId") != null) {
            fragment = ShoppingListFragment.newInstance(getIntent().getStringExtra("storeId"));
        }
        if (getIntent().getParcelableExtra("state") != null) {
            fragment.setInitialSavedState((Fragment.SavedState) getIntent().getParcelableExtra("state"));
        }

        fragmentTransaction.add(R.id.shoppingListParent, fragment, "shoppingFragment");
        fragmentTransaction.commit();
    }

    /**
     * Inflate the menu
     * @param menu main_menu
     * @return true if successful
     */
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        //inflate the menu: this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method responsible for the transition between this activity and {@link MainActivity}
     */
    public void transitionToStartScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //We save the state of the fragment and put it in the intent
        Parcelable state = getFragmentManager().saveFragmentInstanceState(fragment);
        intent.putExtra("state", state);
        intent.putExtra("storeId", fragment.getStoreId());
        startActivity(intent);
    }

    /**
     * This method is responsible for the transition between this activity and {@link StoreMapActivity}
     */
    public void transitionToStoreMap() {
        Intent intent = new Intent(getApplicationContext(), StoreMapActivity.class);
        Parcelable state = getFragmentManager().saveFragmentInstanceState(fragment);
        intent.putExtra("state", state);
        intent.putExtra("storeId", fragment.getStoreId());
        startActivity(intent);
    }



}
