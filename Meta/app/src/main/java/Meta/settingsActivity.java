package Meta;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import xyz.hexene.localvpn.R;

public class settingsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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
            case R.id.home:
                change = new Intent(this, homeActivity.class);
                this.startActivity(change);
                break;
            case R.id.analyze:
                change = new Intent(this, analyzeActivity.class);
                this.startActivity(change);
                break;
        }
    }
}
