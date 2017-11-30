package meta;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 *
 *      FROM NETWORK =======> DEVICE
 *                              ||
 *                              ||
 *                              ||
 *                              ||
 *                              \/
 *                              VPN =======> TO NETWORK
 *
 */

public class VPN extends VpnService {

    private static final String VPN_ADDR = "10.0.0.2";
    private static final String VPN_ROUTE = "0.0.0.0";

    private static PendingIntent VPN_INTENT;
    private static ParcelFileDescriptor VPN_INTERFACE = null;
    private static boolean IS_RUNNING;

    private ConcurrentLinkedQueue<ByteBuffer> NETWORK_TO_DEVICE_QUEUE;
    private ConcurrentLinkedQueue<Packet> DEVICE_TO_NETWORK_QUEUE;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("VPN.java", "In onCreate");
        IS_RUNNING = true;
        setUpVPN();

        NETWORK_TO_DEVICE_QUEUE = new ConcurrentLinkedQueue<ByteBuffer>();


        ExecutorService executors = Executors.newFixedThreadPool(1);
        executors.submit(new VPNRUN(NETWORK_TO_DEVICE_QUEUE));
        Log.w("Tag", "Started");
    }


    protected void setUpVPN() {

        Log.i("VPN.java", "in setUpVPN");
        if (VPN_INTERFACE == null) {
            Builder builder = new Builder();
            builder.addAddress(VPN_ADDR, 32);
            builder.addRoute(VPN_ROUTE, 0);
            VPN_INTERFACE = builder.setSession("Meta").setConfigureIntent(VPN_INTENT).establish();
            Log.i("VPN.java", "VPN_INTERFACE established");
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private static class VPNRUN implements Runnable {

        private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;

        public VPNRUN(ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue){

            this.networkToDeviceQueue = networkToDeviceQueue;

        }

        @Override
        public void run(){
            Log.i("SUCCESS", "STARTING VPN");

            FileChannel vpnIn = new FileInputStream(VPN_INTERFACE.getFileDescriptor()).getChannel();
            FileChannel vpnOut = new FileOutputStream(VPN_INTERFACE.getFileDescriptor()).getChannel();

            try{

                ByteBuffer dataFromDevice = null;
                ByteBuffer dataFromNetwork = null;
                boolean dataSent = true;
                boolean dataReceived = true;

                while(!Thread.interrupted()){

                    /* Try writing from device to network */
                    if(dataSent) {
                        dataFromDevice = ByteBufferPool.acquire();
                    }
                    else{
                        dataFromDevice.clear();
                    }
                    Log.w("Attempt", "Attempting to read");
                    int bytesRead = vpnIn.read(dataFromDevice);
                    if(bytesRead > 0){
                        Log.w("Bytes", "" + bytesRead);
                        dataSent = true;
                        dataFromDevice.flip();

                        Packet packet = new Packet(dataFromDevice);

                        Log.w("Packet", "" + packet.toString());

                        bytesRead = vpnOut.write(dataFromDevice);
                        dataFromDevice.flip();
                        byte[] bytes = new byte[dataFromDevice.remaining()];
                        if(bytesRead > 0){

                            dataFromDevice.get(bytes);
                            Log.w("TAG", bytes.toString());
                        }
                    } else {
                        dataSent = false;
                    }


                    /* Try writing from network to device */

                    dataFromNetwork = ByteBufferPool.acquire();
                    if(dataFromNetwork != null) {
                        dataFromNetwork.flip();
                        int bytesWrote = 0;
                        while (dataFromNetwork.hasRemaining()) {
                            bytesWrote = vpnOut.write(dataFromNetwork);
                        }
                        if (bytesWrote > 0) {
                            Log.w("TAG", "Successfully wrote 0 bytes");
                        } else if (bytesWrote < 0) {
                            Log.w("TAG", "vpnOut.write(dataFromNetwork) error");
                        }
                        ByteBufferPool.release(dataFromNetwork);
                    }

                }
            } catch (Exception e){
                Log.w("Error" , e.toString());
            }
        }

    }
}