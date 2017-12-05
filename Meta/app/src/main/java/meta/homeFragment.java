package meta;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class homeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_home, container, false);
        TextView information = (TextView) v.findViewById(R.id.myView);
        information.setMovementMethod(new ScrollingMovementMethod());
        if(information == null){
            Log.w("Error", "Information scroll view is null");
        }
        ExecutorService executors = Executors.newFixedThreadPool(1);
        executors.submit(new updateHome(MainActivity.handled, information, v));
        return v;
    }
    public void onResume(){
        super.onResume();
    }
    private class updateHome implements Runnable{
        private ConcurrentLinkedQueue<String> packets;
        private TextView scroll;
        private HashMap <String, String> displayed;
        private View me;
        public updateHome(ConcurrentLinkedQueue<String> mainPacket, TextView v, View current){
            this.packets = mainPacket;
            this.scroll = v;
            displayed = new HashMap<String,String>();
            this.me = current;
        }
        @Override
        public void run(){
            while(true){
                String p = packets.poll();
                if(p == null){
                    continue;
                }
               // if(displayed.get(p) == null){
                    String display = "New packet heading towards: " + p + "\n";
                    Log.i("TEXT", "Setting text in textview");
                    displayed.put(p, "HANDLED");
                    scroll.append(display);
                    me.postInvalidate();

            }
        }
    }
}