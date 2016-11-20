package mini.app.orbis;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

public class ViewCaptureActivity extends AppCompatActivity {

    private String temporaryImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_capture);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        getIntent().getByteArrayExtra("bitmap");

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.image_name_label), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.image_name), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.save), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.discard), FontManager.Font.lato);

        temporaryImagePath = getIntent().getStringExtra(GlobalVars.EXTRA_PATH);
        ((ImageView) findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeFile(temporaryImagePath));
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
    protected void onDestroy() {
        new File(temporaryImagePath).delete();
        super.onDestroy();
    }

    public void save(View view) {
        String fileName = ((EditText) findViewById(R.id.image_name)).getText().toString();
        if(fileName.length() == 0) {
            return; // TODO show an error dialog
        }
        String regex = "[^a-zA-Z0-9æøåÆØÅ_ -]";
        Pattern p = Pattern.compile(regex);
        if(p.matcher(fileName).find()) {
            return; // TODO show an error dialog (filename contained illegal characters; --> possibly color EditText background in real-time
        }
        moveFile(temporaryImagePath, Environment.getExternalStorageDirectory().toString() + "/Orbis/" + fileName + ".jps");
        FileManager.updateFolderFiles(this);
        finish();
    }

    public void discard(View view) {
        finish();
    }

    // This is untested stackexchange code
    private void moveFile(String inputFile, String outputFile) {

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputFile).delete();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}
