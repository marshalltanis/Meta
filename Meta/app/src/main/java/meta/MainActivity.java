package meta;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.ConcurrentLinkedQueue;


public class MainActivity extends AppCompatActivity{
    public static ConcurrentLinkedQueue<String> handled;
    private homeFragment home;
    private analyzeFragment analyze;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);
        setUpHome();
        handled = new ConcurrentLinkedQueue<String>();


    }

    public void startVPN(){
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, 0);
        else
            onActivityResult(0, RESULT_OK, null);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == RESULT_OK){
            Log.w("onActivityResult", "About to start vpn!!!");
            startService(new Intent(this, VPN.class));
        }
    }

    protected void setUpHome(){
        BottomNavigationView btView = (BottomNavigationView) findViewById(R.id.navBar);
        if(btView == null){
            Log.w("Error", "btView is null");
            return;
        }
        Menu start = btView.getMenu();
        selectScreen(start.getItem(0));
        btView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectScreen(item);
                return true;
            }
        });
    }
    protected void selectScreen(@NonNull MenuItem item){
        int id = item.getItemId();
        item.setChecked(true);
        switch(id){
//            case R.id.analyze:
//                if(analyze == null){
//                    analyze = new analyzeFragment();
//                }
//                fragManage(analyze);
//                Log.w("Switch", "Switched to analyze");
//                break;
            case R.id.startVpnButton:
                startVPN();
                break;
            default:
               if(home == null) {
                   home = new homeFragment();
               }
                fragManage(home);
                Log.w("Switch", "Switched to home");
                break;
        }
    }
    protected void fragManage(Fragment frag){
        FragmentManager fm = getFragmentManager();
        if(fm != null){
            FragmentTransaction swap = fm.beginTransaction();
            swap.replace(R.id.fragLay, frag);
            swap.addToBackStack(null);
            swap.commit();
        }

    }
}