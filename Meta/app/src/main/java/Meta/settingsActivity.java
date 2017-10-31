package Meta;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import xyz.hexene.localvpn.R;

public class settingsActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent changeAct;
        switch (id){
            case R.id.analyze:
                changeAct = new Intent(this, analyzeActivity.class);
                this.startActivity(changeAct);
                break;
            case R.id.home:
                changeAct = new Intent(this, homeActivity.class);
                this.startActivity(changeAct);
                break;
        }
        return true;
    }
}
