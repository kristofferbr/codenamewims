package sw805f16.codenamewims;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kbrod on 30/03/2016.
 * Class that is to be used to get a WIFI fingerprint
 */
public class WifiFingerprinter{

    WifiManager wifiM;
    Context context;
    List<ScanResult> Scannings;

    public WifiFingerprinter(Context con){

        context = con;


    }


    public ScanResult[] getFingerPrint(){

        ScanResult[] fingerPrint = new ScanResult[0];
        wifiM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);



        if(wifiM.startScan()) {

            Scannings = wifiM.getScanResults();
            fingerPrint = Scannings.toArray(fingerPrint);
            fingerPrint = filterScanResults(fingerPrint);
        } else {
            Toast.makeText(context, "There were no wifi signals", Toast.LENGTH_SHORT).show();
            return null;
        }

        return fingerPrint;

    }


    public ScanResult[] filterScanResults(ScanResult[] in) {
        //TODO: Change the resource later
        List<String> filter = Arrays.asList(context.getResources().getStringArray(R.array.test_bssid));
        ScanResult[] tmpArray = new ScanResult[filter.size()];
        int i = 0;
        for (ScanResult result : in) {
            if (filter.contains(result.BSSID)) {
                tmpArray[i] = result;
                i++;
            }
        }
        return tmpArray;
    }
}
