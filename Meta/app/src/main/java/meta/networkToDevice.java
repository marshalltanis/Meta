package meta;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class networkToDevice implements Runnable {
        private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
        private Selector selector;
        public networkToDevice(ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue, Selector selector) {
            this.networkToDeviceQueue = networkToDeviceQueue;
            this.selector = selector;
        }
        @Override
        public void run() {
            Thread us = Thread.currentThread();
            try {
                while (!us.isInterrupted()) {
                    /* Number of datagram channels ready to be read from */
                    int numChannels = selector.select();
                    Log.i("Channels", numChannels + "ready");
                    if (numChannels == 0) {
                        us.sleep(10);
                        continue;
                    }
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