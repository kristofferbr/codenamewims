package sw805f16.codenamewims;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kbrod on 30/03/2016.
 * Class that is to be used to get a WIFI fingerprint
 */
public class WifiFingerprinter {

    WifiManager wifiM;
    Context context;
    List<ScanResult> Scannings;

    public WifiFingerprinter(Context con){

        context = con;


    }


    public ScanResult[] getFingerPrint(){

        HashMap<String,Integer> fingerPrintHashMap = new HashMap<>(3);
        ScanResult fingerPrint[] = new ScanResult[3];
        ScanResult tempScan;
        wifiM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);



        if(wifiM.startScan()){

            Scannings = wifiM.getScanResults();
            fingerPrint[0] = Scannings.get(0);
            fingerPrint[1] = Scannings.get(1);
            fingerPrint[2] = Scannings.get(2);


            for(int i = 3; i < Scannings.size() ; i++) {
                tempScan = Scannings.get(i);
                int n = 0;

                for (ScanResult s : fingerPrint) {

                    if (s.level < tempScan.level) {
                        fingerPrint[n] = s;

                    }
                    n++;
                }
            }
        }
        else
        {
            Toast.makeText(context,"No results available", Toast.LENGTH_SHORT).show();
            return null;
        }

        fingerPrintHashMap.put(fingerPrint[0].BSSID, fingerPrint[0].level);
        fingerPrintHashMap.put(fingerPrint[1].BSSID, fingerPrint[1].level);
        fingerPrintHashMap.put(fingerPrint[2].BSSID, fingerPrint[2].level);


        return fingerPrint;

    }


}
