package meta;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeviceToNetwork implements Runnable {
    private ConcurrentLinkedQueue<Packet> deviceToNetworkQueue;
    private FileChannel vpnIn;
    public DeviceToNetwork(ConcurrentLinkedQueue<Packet> d2n, ParcelFileDescriptor vpnIn){
        this.deviceToNetworkQueue = d2n;
        this.vpnIn = new FileInputStream(vpnIn.getFileDescriptor()).getChannel();
    }
    @Override
    public void run(){
        Thread us = Thread.currentThread();
        int bytesRead = 0;
        while(true){
            ByteBuffer newPack = ByteBufferPool.acquire();
            try{
                 bytesRead= vpnIn.read(newPack);
                if(bytesRead > 0){
                    newPack.flip();
                    Packet current = new Packet(newPack);
                    Log.i("Dest", current.ip4Header.destinationAddress.toString());
                    current.backingBuffer.flip();
                    deviceToNetworkQueue.offer(current);
                    String newP = current.ip4Header.destinationAddress.toString();
                    MainActivity.handled.offer(newP);
                    ByteBufferPool.release(newPack);
                }
                else {
                    us.sleep(10);
                }
            } catch (Exception e){
                Log.w("ERROR", e.toString());
            }
        }
    }
}
