package mini.app.orbis;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridLayout;
import android.widget.Space;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements GalleryItemFragment.OnFragmentInteractionListener, ViewTreeObserver.OnScrollChangedListener {

    private final String[] IMG_EXTENSIONS = {"jpg", "png", "gif", "bmp", "webp"};

    private boolean reInitFragments, alreadyInitializedSpaces;

    private final String STATE_SCROLL_POSITION = "SCROLL_POSITION";

    public File[] images;

    private int[] spaceViewIds;

    private boolean[] selectedItems;
    /**
     * True if items are currently selected
     */
    private boolean selectionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        String path = Environment.getExternalStorageDirectory().toString() + "/Orbis"; // TODO: Environment.getExternalStorageDirectory() is the build-in external storage
        loadFolderIntoGallery(path);

        findViewById(R.id.gallery_scroll).getViewTreeObserver().addOnScrollChangedListener(this);

        if(savedInstanceState == null) {
            for(int i = 0; i < Math.min(viewportHeightInGridRows() * columnCount(), images.length); i++) {
                GalleryItemFragment firstFragment = GalleryItemFragment.newInstance(i);
                getSupportFragmentManager().beginTransaction().add(R.id.gallery_container, firstFragment).commit();
            }

            findViewById(R.id.actions).setVisibility(View.GONE);
        } else {
            findViewById(R.id.gallery_scroll).setScrollY(savedInstanceState.getInt(STATE_SCROLL_POSITION));
        }

    }

    private void loadFolderIntoGallery(String path) {
        File directory = new File(path);
        if(directory.exists()) {
            File[] files = directory.listFiles();
            if(files == null) {
                images = null;
            } else if(files.length == 0) {
                images = new File[0];
                selectedItems = new boolean[0];
            } else {
                ArrayList<File> imageFiles = new ArrayList<File>();
                for(File file : files) {
                    if (isImageExtension(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1, file.getAbsolutePath().length()))) {
                        imageFiles.add(file);
                    }
                }
                images = toFileArray(imageFiles.toArray());
                selectedItems = new boolean[images.length];
            }
        } else {
            directory.mkdir();
            images = new File[0];
            selectedItems = new boolean[0];
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_SCROLL_POSITION, findViewById(R.id.gallery_scroll).getScrollY());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        reInitFragments = true;
    }

    public synchronized void notifyImageLoaded(int cellID, String imageID) {
        GalleryItemFragment fragment = getFragmentForCellId(cellID);
        if(getCellIdForFragment(indexOfFragment(fragment)) == cellID) {
            fragment.applyImage(cellID, Cache.getBitmapFromMemCache(imageID));
            Log.d("Orbis", "Image was applied for cell " + cellID);
        } else {
        }
    }

    private boolean isImageExtension(String ext) {
        for (String img_extension : IMG_EXTENSIONS) {
            if (ext.equals(img_extension))
                return true;
        }
        return false;
    }

    @Override
    public void onScrollChanged() {
        int currentTop = findViewById(R.id.gallery_scroll).getScrollY();
        int topGridRow = scrollDistanceToGridRow(currentTop);

        if(!alreadyInitializedSpaces) {
            int numberRequired = images.length / columnCount() + (images.length % columnCount() > 0 ? 1 : 0);
            spaceViewIds = new int[numberRequired];
            for(int i=0;i<numberRequired;i++) {
                Space space = new Space(this);
                int viewID = View.generateViewId();
                spaceViewIds[i] = viewID;
                space.setId(viewID);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = ((GalleryItemFragment) getSupportFragmentManager().getFragments().get(0)).getHeight();
                layoutParams.width = 1;
                layoutParams.columnSpec = GridLayout.spec(3);
                layoutParams.rowSpec = GridLayout.spec(i);
                space.setLayoutParams(layoutParams);
                ((GridLayout) findViewById(R.id.gallery_container)).addView(space);
            }
            alreadyInitializedSpaces = true;
        }
        for(int i=0;i<getSupportFragmentManager().getFragments().size();i++) {
            applyScrollToFragment(i, getCellIdForFragment(i), topGridRow, false, null);
        }
        reInitFragments = false;
    }

    public void updateSpaces() {
        int numberRequired = images.length / columnCount() + (images.length % columnCount() > 0 ? 1 : 0);
        if(numberRequired < spaceViewIds.length) {
            int[] newSpaceList = new int[numberRequired];
            for(int i=0;i<numberRequired; i++) {
                newSpaceList[i] = spaceViewIds[i];
            }
            for(int i=numberRequired;i<spaceViewIds.length;i++) {
                ((GridLayout) findViewById(R.id.gallery_container)).removeView(findViewById(spaceViewIds[i]));
            }
            spaceViewIds = newSpaceList;
        } else if(numberRequired > spaceViewIds.length) {
            // TODO
        }
    }

    @Override
    public void onFragmentInflated(GalleryItemFragment fragment, View view) {
        applyScrollToFragment(indexOfFragment(fragment), getCellIdForFragment(indexOfFragment(fragment)), 0, false, view); // TODO this does nothing, or at least not what it is supposed to do
    }

    private void applyScrollToFragment(int fragmentID, int cellID, int topRow, boolean forceImageUpdate, View fragmentView) {

        GalleryItemFragment fragment = (GalleryItemFragment) getSupportFragmentManager().getFragments().get(fragmentID);

        if(fragmentView == null) {
            if(fragment.getView() != null) {
                fragmentView = fragment.getView();
            } else {
                return;
            }
        }

        if(!(!(fragment.getCurrentCellID() == cellID) || reInitFragments || forceImageUpdate)) {
            // The fragment is already displaying the correct image
        } else {
            if(cellID >= images.length) {
                fragmentView.setVisibility(View.INVISIBLE);
                fragment.setCellID(cellID);
                return;
            }

            fragment.setCellID(cellID);

            fragmentView.setVisibility(View.VISIBLE);
            String filename = images[cellID].getName(); // TODO compatibility with duplicate file names (eg example.jpg/example.png)
            if(Cache.isBitmapCached(filename)) {
                Log.d("Orbis", "Loading from cache " + fragmentID);
                fragment.applyImage(cellID, Cache.getBitmapFromMemCache(filename), fragmentView);
            } else if(!Cache.isBitmapBeingLoaded(filename)) {
                Log.d("Orbis", "Async task started for fragment " + fragmentID);
                Cache.markBitmapAsBeingLoaded(filename, true);
                fragment.markAsLoading();
                new AsyncTaskLoadThumbnail(this, cellID, filename, images[cellID]).execute();
            }
            if(fragmentView == null) // Presumably the fragment has not yet been inflated
                return;
            GridLayout.LayoutParams layoutParams = (GridLayout.LayoutParams) fragmentView.getLayoutParams();
            layoutParams.rowSpec = GridLayout.spec(cellID / columnCount());
            layoutParams.columnSpec = GridLayout.spec(cellID % columnCount());

            fragmentView.setLayoutParams(layoutParams);
        }
    }

    /**
     * To be called when images were delete, a new image created or another folder opened
     */
    private void updateFragmentImages() {
        for(int i=0;i<getSupportFragmentManager().getFragments().size();i++) {
            applyScrollToFragment(i, getCellIdForFragment(i), scrollDistanceToGridRow(findViewById(R.id.gallery_scroll).getScrollY()), true, null);
        }
    }

    private int getCellIdForFragment(int fragmentID) {
        int currentTopRow = scrollDistanceToGridRow(findViewById(R.id.gallery_scroll).getScrollY());
        int fragmentInitialCol = fragmentID % columnCount();
        int fragmentInitialRow = fragmentID / columnCount();

        int iterationStep = currentTopRow % viewportHeightInGridRows();
        int screen = currentTopRow / viewportHeightInGridRows();

        if (fragmentInitialRow + 1 <= iterationStep) {
            return (fragmentInitialRow + ((screen + 1) * viewportHeightInGridRows())) * columnCount() + fragmentInitialCol;
        } else {
            return (fragmentInitialRow + (screen * viewportHeightInGridRows())) * columnCount() + fragmentInitialCol;
        }

    }

    private GalleryItemFragment getFragmentForCellId(int cellID) {
        return (GalleryItemFragment) getSupportFragmentManager().getFragments().get(((cellID / columnCount()) % viewportHeightInGridRows()) * columnCount() + (cellID % columnCount()));
    }

    private int scrollDistanceToGridRow(int px) {
        if(getSupportFragmentManager().getFragments() == null) {
            return 0;
        }
        if(getSupportFragmentManager().getFragments().get(0) == null) { // Happens when return is pressed to quickly after starting the activity
            return 0;
        }
        if(((GalleryItemFragment) getSupportFragmentManager().getFragments().get(0)).getHeight() > 0)
            return px / ((GalleryItemFragment) getSupportFragmentManager().getFragments().get(0)).getHeight(); // TODO does not account for possible margins between grid cells
        return 0;
    }

    /**
     * Maximum number of grid rows visible at any time, dependent on row height and screen size
     */
    private int viewportHeightInGridRows() {
        return 3; // TODO these are just test values
    }

    private int columnCount() {
        return 3; // TODO these are just test values
    }

    public int indexOfFragment(GalleryItemFragment fragment) {
        //return getSupportFragmentManager().getFragments().indexOf(fragment);
        return fragment.getFragmentID();
    }

    public File[] toFileArray(Object[] array) {
        File[] a = new File[array.length];
        for(int i=0;i<array.length;i++) {
            a[i] = (File) array[i];
        }
        return a;
    }

    @Override
    public boolean isItemSelected(int itemID) {
        return selectedItems[itemID];
    }

    public void invertSelectionStatus(int itemID) {
        if(isItemSelected(itemID)) {
            deselectItem(itemID);
        } else {
            selectItem(itemID);
        }
    }

    public void selectItem(int itemID) {
        if(getNumberOfSelectedItems() == 0) {
            selectionMode = true;
            showActionBarIcons();
        }
        selectedItems[itemID] = true;
    }

    public void deselectItem(int itemID) {
        selectedItems[itemID] = false;
        if(getNumberOfSelectedItems() == 0) {
            selectionMode = false;
            hideActionBarIcons();
        }
    }

    public void showActionBarIcons() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);
        View actionBarItems = findViewById(R.id.actions);
        actionBarItems.setVisibility(View.VISIBLE);
        actionBarItems.startAnimation(fadeIn);
    }

    public void hideActionBarIcons() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(500);
        final View actionBarItems = findViewById(R.id.actions);
        fadeOut.setAnimationListener(new Animation.AnimationListener(){
            public void onAnimationStart(Animation anim) {}
            public void onAnimationRepeat(Animation anim) {}
            public void onAnimationEnd(Animation anim) {
                actionBarItems.setVisibility(View.GONE);
            }
        });
        actionBarItems.startAnimation(fadeOut);
    }

    public void deleteSelectedItems(View view) {
        if(getNumberOfSelectedItems() < 1)
            return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete " + getNumberOfSelectedItems() + " images")
                .setMessage("Are you sure you want to delete these " + getNumberOfSelectedItems() + " images?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d("Orbis", "Delete pictures");

                        int[] indices = Util.allIndices(true, selectedItems);
                        File[] newImages = new File[images.length - getNumberOfSelectedItems()];
                        int j=0;
                        for(int i=0;i<images.length;i++) {
                            if(!Util.contains(indices, i)) {
                                newImages[j] = images[i];
                                j++;
                            }
                        }
                        images = newImages;
                        selectedItems = new boolean[newImages.length];
                        updateFragmentImages();
                        hideActionBarIcons();
                        updateSpaces();
                    }})
                .setNegativeButton(android.R.string.cancel, null).create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public void shareSelectedItems(View view) {
        // TODO
    }

    public int getNumberOfSelectedItems() {
        return Util.count(selectedItems, true);
    }

    @Override
    public void onClick(boolean isLongClick, int itemID, GalleryItemFragment fragment) {
        if(selectionMode) {
            invertSelectionStatus(itemID);
            fragment.updateColorFilter();
        } else {
            if(isLongClick) {
                selectItem(itemID); // This also sets selectionMode to true
                fragment.updateColorFilter();
            } else {
                openImageInVRViewer(itemID);
            }
        }
    }

    private void openImageInVRViewer(int itemID) {
        Intent intent = new Intent(this, VRViewerActivity.class);
        intent.putExtra(GlobalVars.EXTRA_PATH, images[itemID].getAbsolutePath());
        startActivity(intent);
    }

    public void navigateBack(View view) {
        finish();
    }

    public void chooseFolder(View view) {
        DialogProperties properties = new DialogProperties();

        properties.selection_mode= DialogConfigs.SINGLE_MODE;
        properties.selection_type=DialogConfigs.DIR_SELECT;
        properties.root=new File(DialogConfigs.DEFAULT_DIR); // TODO the file chooser does not allow to switch between primary and secondary storage
        properties.error_dir=new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions=null;

        FilePickerDialog dialog = new FilePickerDialog(GalleryActivity.this,properties);
        dialog.setTitle("Choose Folder");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                Log.d("Orbis", files[0]);
            } // TODO actually implement a folder change
        });

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        /*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, FOLDER_CHOOSER_INTENT_ID);*/
    }

}
