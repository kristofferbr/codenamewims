package sw805f16.codenamewims;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StoreMapActivity extends AppCompatActivity {

    public boolean isInFront = false;
    public String store_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInFront = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInFront = false;
    }
}
