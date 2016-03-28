package sw805f16.codenamewims;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ShoppingListActivity extends AppCompatActivity {

    ShoppingListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //If this activity was called with a saved state the fragment is instantiated with the state
        if (getIntent().getBundleExtra("state") != null) {
            fragment = ShoppingListFragment.newInstance(getIntent().getBundleExtra("state"), getIntent().getStringExtra("storeId"));
        } else {
            fragment = ShoppingListFragment.newInstance(null, getIntent().getStringExtra("storeId"));
        }

        fragmentTransaction.add(R.id.shoppingParent, fragment, "shoppingFragment");
        fragmentTransaction.commit();
    }

    /**
     * This method responsible for the transition between this activity and {@link MainActivity}
     */
    public void transitionToStartScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //We save the state of the fragment and put it in the intent
        intent.putExtra("state", fragment.saveState());
        startActivity(intent);
    }

    /**
     * This method is responsible for the transition between this activity and {@link StoreMapActivity}
     */
    public void transitionToStoreMap() {
        Intent intent = new Intent(getApplicationContext(), StoreMapActivity.class);
        intent.putExtra("state", fragment.saveState());
        startActivity(intent);
    }

}
