package sw805f16.codenamewims;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by kbrod on 30/03/2016.
 * Class that is to be used to get a WIFI fingerprint
 */
public class WifiFingerprinter{
    Context context;

    public WifiFingerprinter(Context con){

        context = con;


    }


    public ArrayList<ScanResult> getFingerPrint(){

        WifiManager wifiM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        ArrayList<ScanResult> Scannings = new ArrayList<>();

        if(wifiM.startScan()) {

            Scannings.addAll(wifiM.getScanResults());
            Scannings = filterScanResults(Scannings);
        } else {
            Toast.makeText(context, "There were no wifi signals", Toast.LENGTH_SHORT).show();
            return null;
        }

        return Scannings;

    }


    public ArrayList<ScanResult> filterScanResults(ArrayList<ScanResult> in) {
        //TODO: Change the resource later
        List<String> filter = Arrays.asList(context.getResources().getStringArray(R.array.test_bssid));
        ArrayList<ScanResult> tmpArray = new ArrayList<>();
        for (ScanResult result : in) {
            if (filter.contains(result.BSSID)) {
                tmpArray.add(result);
            }
        }
        return tmpArray;
    }
}
