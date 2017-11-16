package Meta;

import android.app.PendingIntent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;


public class VPN extends VpnService {

    private static final String VPN_ADDR = "10.0.0.2";
    private static final String VPN_ROUTE = "0.0.0.0";

    private static PendingIntent VPN_INTENT;
    private static ParcelFileDescriptor VPN_INTERFACE = null;
    private static boolean IS_RUNNING;

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("VPN.java", "In onCreate");
        IS_RUNNING = true;
        setUpVPN();
    }


    protected void setUpVPN(){

        Log.i("VPN.java", "in setUpVPN");
        if(VPN_INTERFACE == null){
            Builder builder = new Builder();
            builder.addAddress(VPN_ADDR, 32);
            builder.addRoute(VPN_ROUTE, 0);
            VPN_INTERFACE = builder.setSession("Meta").setConfigureIntent(VPN_INTENT).establish();
            Log.i("VPN.java", "VPN_INTERFACE established");
        }

    }
}
