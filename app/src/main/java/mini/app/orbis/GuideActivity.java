package mini.app.orbis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class GuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        int guideID = getIntent().getIntExtra(GlobalVars.GUIDE_ID, -1);
        if(guideID != -1) {
            GuideManager.Guide guide = GuideManager.guides[guideID];

            WebView webView = (WebView) findViewById(R.id.html_content);
            webView.loadUrl(guide.getHtmlResourcePath());

            TextView title = (TextView) findViewById(R.id.page_title);
            title.setText(guide.getGuideTitle());
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

    public void back(View view) {
        finish();
    }
}
