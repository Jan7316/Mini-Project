package mini.app.orbis;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
    private String temporaryFolderPath = "";

    private EditText filename;

    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_capture);

        decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.image_name_label), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.image_name), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.save), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.discard), FontManager.Font.lato);

        temporaryImagePath = getIntent().getStringExtra(GlobalVars.EXTRA_PATH);
        ((ImageView) findViewById(R.id.image)).setImageBitmap(BitmapFactory.decodeFile(temporaryImagePath));
        temporaryFolderPath = Environment.getExternalStorageDirectory().toString() + "/Orbis/.temp";

        filename = (EditText) findViewById(R.id.image_name);
        filename.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onFilenameChanged(s.toString());
            }
        });
        filename.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
                    return false;
                }
                return false;
            }
        });
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
        new File(temporaryFolderPath).delete();
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
        moveFile(temporaryImagePath, Environment.getExternalStorageDirectory().toString() + "/Orbis/" + fileName + "." + GlobalVars.SAVE_FILE_FORMAT);
        FileManager.updateFolderFiles(this);
        setResult(GlobalVars.RESULT_FINISH_PARENT);
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

    private void onFilenameChanged(String newText) {
        switch(getTextStatus(newText)) {
            case -1:
                filename.setBackgroundColor(Color.argb(50, 255, 0, 0));
                break;
            case 0:
                filename.setBackgroundColor(Color.argb(0, 0, 0, 0));
                break;
            case 1:
                filename.setBackgroundColor(Color.argb(50, 0, 255, 0));
                break;
        }
    }

    private int getTextStatus(String text) {
        if(text.length() == 0) {
            return 0;
        }
        String regex = "[^a-zA-Z0-9æøåÆØÅ_ -]";
        Pattern p = Pattern.compile(regex);
        if(p.matcher(text).find()) {
            return -1;
        }
        return 1;
    }

}
