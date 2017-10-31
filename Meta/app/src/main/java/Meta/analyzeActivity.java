package Meta;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import xyz.hexene.localvpn.R;

public class analyzeActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);
        BottomNavigationView btView = (BottomNavigationView) findViewById(R.id.navBar);
        btView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectActivity(item);
                return true;
            }
        });
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0,1),
                new DataPoint(2,6),
                new DataPoint(5, 7),
                new DataPoint(6, 3),
                new DataPoint (8, 10)
        });
        graph.addSeries(series);
    }
    public void selectActivity(@NonNull MenuItem item){
        int id = item.getItemId();
        Intent change;
        switch(id){
            case R.id.home:
                change = new Intent(this, homeActivity.class);
                this.startActivity(change);
                break;
            case R.id.setting:
                change = new Intent(this, settingsActivity.class);
                this.startActivity(change);
                break;
        }
    }
}