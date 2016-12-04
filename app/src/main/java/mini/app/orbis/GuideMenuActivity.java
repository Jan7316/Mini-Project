package mini.app.orbis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class GuideMenuActivity extends AppCompatActivity implements GuideFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_menu);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if(savedInstanceState == null) {
            GuideManager.Guide[] guides = GuideManager.guides;
            for(int i=0; i<guides.length; i++) {
                GuideFragment fragment = GuideFragment.newInstance(i, guides[i].getGuideTitle(), guides[i].getTitleImageResourceID());
                getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
            }
        }

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.page_title), FontManager.Font.lato);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void openGuide(int guideID) {
        //TODO
    }

    public void back(View view) {
        finish();
    }
}
