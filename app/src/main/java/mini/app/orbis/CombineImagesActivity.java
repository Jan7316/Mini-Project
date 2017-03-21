package mini.app.orbis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

public class CombineImagesActivity extends AppCompatActivity {

    private File left, right;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combine_images);

        decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.image_name_label), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.image_name), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.save), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewById(R.id.discard), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewOnSide(false, R.id.file), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewOnSide(true , R.id.file), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewOnSide(false, R.id.change_file), FontManager.Font.lato);
        FontManager.applyFontToView(this, (TextView) findViewOnSide(true , R.id.change_file), FontManager.Font.lato);

        findViewOnSide(false, R.id.change_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLeftImage(v);
            }
        });
        findViewOnSide(true, R.id.change_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRightImage(v);
            }
        });

        EditText filename = (EditText) findViewById(R.id.image_name);
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


        disableSaveButton();
    }

    public void selectLeftImage(View view) {
        selectImage(false);
    }

    public void selectRightImage(View view) {
        selectImage(true);
    }

    /**
     * false = left
     * true = right
     */
    private void selectImage(boolean side) {
        final boolean isRight = side;
        DialogProperties properties = new DialogProperties();
        properties.selection_mode= DialogConfigs.SINGLE_MODE;
        properties.selection_type=DialogConfigs.FILE_SELECT;
        properties.root=new File(DialogConfigs.DEFAULT_DIR); // TODO the file chooser does not allow to switch between primary and secondary storage
        properties.error_dir=new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions=new String[]{"jps", "jpg", "jpeg", "png", "bmp", "webp"};
        FilePickerDialog dialog = new FilePickerDialog(CombineImagesActivity.this,properties);
        dialog.setTitle("Select " + (side ? "Right" : "Left") + " Image");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                handleFileSelection(files, isRight);
            }
        });
        // Hide the status and navigation bars which would become visible otherwise
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    /**
     * false = left
     * true = right
     */
    private void handleFileSelection(String[] files, boolean side) {
        File file = new File(files[0]);
        if(!file.exists())
            return;
        if(side) {
            right = file;
        } else {
            left = file;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ((ImageView) findViewOnSide(side, R.id.image)).setImageBitmap(bitmap);
        ((TextView) findViewOnSide(side, R.id.file)).setText(file.getName());
        checkIfImageIsReadyForSaving();
    }

    private void checkIfImageIsReadyForSaving() {
        if(getTextStatus(((TextView) findViewById(R.id.image_name)).getText().toString()) == 1) {
            if(left != null && right != null) {
                enableSaveButton();
                return;
            }
        }
        disableSaveButton();
    }

    private void disableSaveButton() {
        Button button = (Button) findViewById(R.id.save);
        button.setTextColor(getResources().getColor(R.color.colorButtonDisabled));
        button.setClickable(false);
    }

    private void enableSaveButton() {
        Button button = (Button) findViewById(R.id.save);
        button.setTextColor(getResources().getColor(R.color.colorButton));
        button.setClickable(true);
    }

    /**
     * false = left
     * true = right
     */
    private View findViewOnSide(boolean side, int id) {
        return side ? findViewById(R.id.right).findViewById(id) : findViewById(R.id.left).findViewById(id);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void save(View view) {
        String fileName = ((EditText) findViewById(R.id.image_name)).getText().toString();
        if(fileName.length() == 0) {
            return;
        }
        String regex = "[^a-zA-Z0-9æøåÆØÅ_ -]";
        Pattern p = Pattern.compile(regex);
        if(p.matcher(fileName).find()) {
            return;
        }
        if(left == null || right == null)
            return;

        Bitmap combined = combineImages(left.getAbsolutePath(), right.getAbsolutePath());

        FileOutputStream out = null;
        String resultFileName = Environment.getExternalStorageDirectory().toString() + "/Orbis/" + fileName + "." + GlobalVars.SAVE_FILE_FORMAT;
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

        FileManager.updateFolderFiles(this);
        setResult(GlobalVars.RESULT_FINISH_PARENT);
        finish();
    }

    private Bitmap combineImages(String cPath, String sPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap s = BitmapFactory.decodeFile(cPath, options);
        int imageWidth = s.getWidth();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int scaleValue = Math.round(imageWidth/(size.x/2));

        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleValue;
        Bitmap c = BitmapFactory.decodeFile(cPath, options);
        s = BitmapFactory.decodeFile(sPath, options);

        int width = size.x;
        int height = (int) ((((float)c.getHeight()) / c.getWidth()) * width);

        Bitmap cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }

    public void discard(View view) {
        finish();
    }

    private void onFilenameChanged(String newText) {
        View filename = findViewById(R.id.image_name);
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
        checkIfImageIsReadyForSaving();
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


    // TAKEN FROM STACKOVERFLOW (supposed to hide the navigation bars after the keyboard is shown); DOES NOT SEAM TO WORK
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // When the window loses focus (e.g. the action overflow is shown),
        // cancel any pending hide action. When the window gains focus,
        // hide the system UI.
        if (hasFocus) {
            delayedHide(300);
        } else {
            mHideHandler.removeMessages(0);
        }
    }
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    private final Handler mHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSystemUI();
        }
    };
    private void delayedHide(int delayMillis) {
        mHideHandler.removeMessages(0);
        mHideHandler.sendEmptyMessageDelayed(0, delayMillis);
    }
}
