package meta;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class deviceToNetwork implements Runnable {
    private ConcurrentLinkedQueue<Packet> deviceToNetworkQueue;
    private FileChannel vpnIn;
    public deviceToNetwork(ConcurrentLinkedQueue<Packet> d2n, ParcelFileDescriptor vpnIn){
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
                    deviceToNetworkQueue.offer(current);
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
