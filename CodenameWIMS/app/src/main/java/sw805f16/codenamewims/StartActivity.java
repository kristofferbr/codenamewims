package sw805f16.codenamewims;

/**
 * Created by Netray on 01/04/2016.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class StartActivity extends Activity {

    int storeID;

    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Removes action bar for start screen.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen); // When refactoring, change to activity_start_screen layoutet.

        Button storemapButton = (Button) findViewById(R.id.startStoreBtn);
        Button shoppingButton = (Button) findViewById(R.id.startShoppingBtn);
        ImageButton settingsButton = (ImageButton) findViewById(R.id.startSettingsBtn);
        Button exitButton = (Button) findViewById(R.id.startExitBtn);
        Button chooseStoreButton = (Button) findViewById(R.id.startChooseStoreBtn);


        chooseStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(StartActivity.this, "REPLACE ME!", Toast.LENGTH_SHORT).show();
            }
        });

        storemapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                startActivity(intent);

            }
        });

        shoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShoppingActivity.class);
                startActivity(intent);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }
}
