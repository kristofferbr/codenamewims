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


        if (getIntent().getExtras() != null) {
            fragment = ShoppingListFragment.newInstance(getIntent().getBundleExtra("state"));
        } else {
            fragment = new ShoppingListFragment();
        }

        fragmentTransaction.add(R.id.shoppingParent, fragment, "shoppingFragment");
        fragmentTransaction.commit();
    }

    public void transitionToStartScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("state", fragment.saveState());
        startActivity(intent);
    }

    public void transitionToStoreMap() {
        Intent intent = new Intent(getApplicationContext(), StoreMapActivity.class);
        intent.putExtra("state", fragment.saveState());
        startActivity(intent);
    }

}
