package Meta;


import android.content.Intent;
import android.os.Bundle;
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
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent changeAct;
        switch (id){
            case R.id.analyze:
                changeAct = new Intent(this, analyzeActivity.class);
                this.startActivity(changeAct);
                break;
            case R.id.setting:
                changeAct = new Intent(this, settingsActivity.class);
                this.startActivity(changeAct);
                break;
        }
        return true;
    }
}