package Meta;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import xyz.hexene.localvpn.R;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);
        setUpHome();
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
        Fragment current;
        switch(id){
            case R.id.analyze:
                current = new analyzeFragment();
                fragManage(current);
                Log.w("Switch", "Switched to analyze");
                break;
            case R.id.setting:
                current = new settingsFragment();
                fragManage(current);
                Log.w("Switch", "Switched to setting");
                break;
            default:
                current = new homeFragment();
                fragManage(current);
                Log.w("Switch", "Switched to home");
                break;
        }
    }
    protected void fragManage(Fragment frag){
        FragmentManager fm = getFragmentManager();
        if(fm != null){
            FragmentTransaction swap = fm.beginTransaction();
            swap.replace(R.id.fragLay, frag);
            swap.commit();
        }

    }
}