package meta;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
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

    private HashMap<String, DatagramChannel> channelMap;
    private Selector select;
    private ConcurrentLinkedQueue<ByteBuffer> NETWORK_TO_DEVICE_QUEUE;
    private ConcurrentLinkedQueue<Packet> DEVICE_TO_NETWORK_QUEUE;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("VPN.java", "In onCreate");
        IS_RUNNING = true;
        setUpVPN();
        channelMap = new HashMap<String, DatagramChannel>();
        NETWORK_TO_DEVICE_QUEUE = new ConcurrentLinkedQueue<ByteBuffer>();
        DEVICE_TO_NETWORK_QUEUE = new ConcurrentLinkedQueue<Packet>();

        try{
            select = Selector.open();
        }catch(Exception e){
            Log.w("ERROR", e.toString());
        }
        ExecutorService executors = Executors.newFixedThreadPool(3);
        executors.submit(new VPNRUN(NETWORK_TO_DEVICE_QUEUE, DEVICE_TO_NETWORK_QUEUE, select, channelMap, this));
        executors.submit(new deviceToNetwork(DEVICE_TO_NETWORK_QUEUE, VPN_INTERFACE));
        executors.submit(new networkToDevice(NETWORK_TO_DEVICE_QUEUE, select));
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
        private ConcurrentLinkedQueue<Packet> deviceToNetworkQueue;
        private Selector select;
        private VPN vpn;
        private HashMap<String, DatagramChannel> map;
        public VPNRUN(ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue, ConcurrentLinkedQueue<Packet> deviceToNetworkQueue, Selector select, HashMap<String, DatagramChannel> map, VPN vpn){
            this.deviceToNetworkQueue = deviceToNetworkQueue;
            this.networkToDeviceQueue = networkToDeviceQueue;
            this.select = select;
            this.map = map;
            this.vpn = vpn;
        }

        @Override
        public void run(){
            Log.i("SUCCESS", "STARTING VPN");

            FileChannel vpnIn = new FileInputStream(VPN_INTERFACE.getFileDescriptor()).getChannel();
            FileChannel vpnOut = new FileOutputStream(VPN_INTERFACE.getFileDescriptor()).getChannel();

            try{

                Packet packetFromDevice = null;
                ByteBuffer dataFromNetwork = null;
                boolean dataSent = true;
                boolean dataReceived = true;
                DatagramChannel destOut;
                while(!Thread.interrupted()){
                    packetFromDevice = deviceToNetworkQueue.poll();
                    if(packetFromDevice != null){
                        byte[] current = new byte[10000];
                        packetFromDevice.backingBuffer.get(current);
                        Log.w("Working", "Writing to network datagram channel: " + current.toString());
                        destOut = map.get(packetFromDevice.ip4Header.destinationAddress.toString());
                        if(destOut == null){
                            destOut = DatagramChannel.open();
                            int destPort = 4444;
                            if(packetFromDevice.isUDP()){
                                destPort = packetFromDevice.udpHeader.destinationPort;
                            }
                            else if(packetFromDevice.isTCP()){
                                destPort = packetFromDevice.tcpHeader.destinationPort;
                            }
                            try {
                                if(packetFromDevice.ip4Header.destinationAddress != null) {
                                    destOut.connect(new InetSocketAddress(packetFromDevice.ip4Header.destinationAddress, destPort));
                                    map.put(packetFromDevice.ip4Header.destinationAddress.toString(), destOut);
                            /* This is to make sure selector only wakes up when there is a packet from the destination to us */
                                    packetFromDevice.swapSourceAndDestination();
                                    select.wakeup();
                                    destOut.configureBlocking(false);
                                    destOut.register(select, SelectionKey.OP_READ, packetFromDevice);
                                    vpn.protect(destOut.socket());
                                    try {
                                        int bytesWritten = destOut.write(packetFromDevice.backingBuffer);
                                        Log.i("SUCCESS", "" + bytesWritten);
                                    } catch (Exception e) {
                                        Log.w("ERROR", e.toString());
                                    }
                                }
                            }catch (Exception e){
                                Log.w("ERROR", "Failed to connect");
                            }

                        }
                    }
                    /* Try writing from network to device */
                    dataFromNetwork = networkToDeviceQueue.poll();
                    if(dataFromNetwork != null) {
                        dataFromNetwork.flip();
                        int bytesWrote = 0;
                        //while (dataFromNetwork.hasRemaining()) {
                        bytesWrote = vpnOut.write(dataFromNetwork);
                        //}
                        if (bytesWrote > 0) {
                            Log.w("TAG", "Wrote " + bytesWrote);
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