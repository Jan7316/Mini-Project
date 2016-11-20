package mini.app.orbis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.FileOutputStream;
import java.io.IOException;

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

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
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

        // Hide the status and navigation bars which would become visible otherwise
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

    public void startImageCapture(View view) {
        takePicture(false);
    }

    /**
     * SIDE
     * false: left
     * true: right
     */
    private void takePicture(boolean side) {
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        String pictureImagePath = storageDir.getAbsolutePath() + "/.temp/Orbis/latest_capture_" + (side ? "right" : "left") + ".jpg";
        File file = new File(pictureImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, (side ? GlobalVars.INTENT_CAPTURE_RIGHT : GlobalVars.INTENT_CAPTURE_LEFT));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    takePicture(true);
                } else {
                    // TODO image capture failed/was cancelled
                }
                break;
            case 2:
                if(resultCode != RESULT_OK) {
                    break; // TODO image capture failed/was cancelled
                }
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                String basePath = storageDir.getAbsolutePath() + "/.temp/Orbis/latest_capture_";
                String pathLeft = basePath + "left.jpg";
                String pathRight = basePath + "right.jpg";
                File left = new  File(pathLeft);
                File right = new  File(pathRight);
                if(left.exists() && right.exists()){
                    Bitmap leftBitmap = BitmapFactory.decodeFile(left.getAbsolutePath());
                    Bitmap rightBitmap = BitmapFactory.decodeFile(right.getAbsolutePath());
                    Bitmap combined = combineImages(leftBitmap, rightBitmap);

                    FileOutputStream out = null;
                    String resultFileName = Environment.getExternalStorageDirectory().toString() + "/Orbis/" + "new_capture.jps"; // TODO include timestamp in file name
                    try {
                        out = new FileOutputStream(resultFileName);
                        combined.compress(Bitmap.CompressFormat.JPEG, 100, out); // TODO this is lossless, may lead to very large files
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    left.delete();
                    right.delete();
                    new File(storageDir.getAbsolutePath() + "/.temp/Orbis)").delete(); // delete the temporary folder (does not work at the moment, an empty folder remains)

                    Intent intent = new Intent(this, ViewCaptureActivity.class);
                    intent.putExtra(GlobalVars.EXTRA_PATH, resultFileName);
                    startActivity(intent);
                }
                break;
        }
    }

    // This is untested code from stackoverflow
    public Bitmap combineImages(Bitmap c, Bitmap s) {

        int width = 1000; // TODO this is a random value
        int height = (int) ((((float)c.getHeight()) / c.getWidth()) * width);

        Log.d("Orbis", "Height: " + height + "; original height: " + c.getHeight() + "; original width: " + c.getWidth());
        c = Bitmap.createScaledBitmap(c, width, height, false);
        s = Bitmap.createScaledBitmap(s, width, height, false);

        Bitmap cs = Bitmap.createBitmap(width * 2, height, Bitmap.Config.ARGB_4444);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }

}
