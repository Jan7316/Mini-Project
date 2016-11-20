package mini.app.orbis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

public class ImportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_images);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        FontManager.applyFontToView(this, (Button) findViewById(R.id.cancel), FontManager.Font.lato);
    }

    public void cancel(View view) {
        finish();
    }

    public void createReferences(View view) {
        DialogProperties properties = new DialogProperties();

        properties.selection_mode= DialogConfigs.MULTI_MODE;
        properties.selection_type=DialogConfigs.FILE_SELECT;
        properties.root=new File(DialogConfigs.DEFAULT_DIR); // TODO the file chooser does not allow to switch between primary and secondary storage
        properties.error_dir=new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions=new String[]{"jps"};

        FilePickerDialog dialog = new FilePickerDialog(ImportActivity.this,properties);
        dialog.setTitle("Select Files To Import");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                handleFileSelection(files);
            }
        });

        // Hide the status and navigation bars which would become visible otherways
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void handleFileSelection(String[] selection) {
        for(String fileString : selection) {
            File file = new File(fileString);
            Log.d("Orbis", "Handling file to be referenced: " + fileString);
            if(file.exists()) {
                FileManager.addReferences(this, file);
                Log.d("Orbis", "File reference was created: " + fileString);
            }
        }
        finish();
    }

}
