package Meta;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import xyz.hexene.localvpn.R;

public class homeActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView btView = (BottomNavigationView) findViewById(R.id.navBar);
        btView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectActivity(item);
                return true;
            }
        });
    }
    public void selectActivity(@NonNull MenuItem item){
        int id = item.getItemId();
        Intent change;
        switch(id){
            case R.id.analyze:
                change = new Intent(this, analyzeActivity.class);
                this.startActivity(change);
                break;
            case R.id.setting:
                change = new Intent(this, settingsActivity.class);
                this.startActivity(change);
                break;
        }
    }
}