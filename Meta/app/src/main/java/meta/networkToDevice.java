package meta;

import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class networkToDevice implements Runnable {
        private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
        private Selector selector;
        private LinkedBlockingQueue<DatagramChannel> open;
        public networkToDevice(ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue, Selector selector, LinkedBlockingQueue<DatagramChannel> open) {
            this.networkToDeviceQueue = networkToDeviceQueue;
            this.selector = selector;
            this.open = open;
        }
        @Override
        public void run() {
            Thread us = Thread.currentThread();
            try {
                while (!us.isInterrupted()) {
                    /* Number of datagram channels ready to be read from */
                    selector.wakeup();
                    int numChannels = selector.select();
                    //DatagramChannel available = open.take();
                    //Log.i("Channels", numChannels + "ready");
                    if (numChannels == 0) {
                        us.sleep(10);
                        continue;
                    }
                    Log.w("Success", "THIS IS WORKING AND NOT FUCKED UP");
                    /* Get all channels that are ready */
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                    while (keyIterator.hasNext() && !us.isInterrupted()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isValid() && key.isReadable()) {
                            /* Remove current key from set */
                            keyIterator.remove();
                            ByteBuffer networkData = ByteBufferPool.acquire();
                            //networkData.position(HEADER_SIZE);

                            DatagramChannel datagramChannel = (DatagramChannel) key.channel();
                            int bytesRead = datagramChannel.read(networkData);
                            if (bytesRead > 0) {
                                networkToDeviceQueue.offer(networkData);
                            }
                            //Packet currentPacket = (Packet) key.attachment();
                        }
                    }
                }
            } catch (Exception e) {
                Log.w("ERROR", e.toString());
            }

        }
}