package mini.app.orbis;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridLayout;
import android.widget.Space;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

/*
 * TODO: Bug: Once when I deleted several images (about 5), the app went back to the home screen after removing the files
 */

public class GalleryActivity extends AppCompatActivity implements GalleryItemFragment.OnFragmentInteractionListener, ViewTreeObserver.OnScrollChangedListener {

    private boolean reInitFragments, alreadyInitializedSpaces;

    private final String STATE_SCROLL_POSITION = "SCROLL_POSITION";

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

        if(FileManager.getFiles(this).length == 0) {
            findViewById(R.id.gallery_scroll).setBackgroundResource(R.drawable.gallery_helper);
        }

        findViewById(R.id.gallery_scroll).getViewTreeObserver().addOnScrollChangedListener(this);

        if(savedInstanceState == null) {
            for(int i = 0; i < viewportHeightInGridRows() * columnCount(); i++) {
                GalleryItemFragment firstFragment = GalleryItemFragment.newInstance(i);
                getSupportFragmentManager().beginTransaction().add(R.id.gallery_container, firstFragment).commit();
            }
        } else {
            findViewById(R.id.gallery_scroll).setScrollY(savedInstanceState.getInt(STATE_SCROLL_POSITION));
        }

        findViewById(R.id.delete).setVisibility(View.GONE);
        findViewById(R.id.share).setVisibility(View.GONE);

        FontManager.applyFontToView(this, (TextView) findViewById(R.id.title), FontManager.Font.lato);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
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

        updateFragmentImages(); // functionality not yet checked, supposed to apply any changes made to the file list while in ImportActivity to the fragments in here
    }

    public synchronized void notifyImageLoaded(int cellID, String imageID) {
        GalleryItemFragment fragment = getFragmentForCellId(cellID);
        if(getCellIdForFragment(indexOfFragment(fragment)) == cellID) {
            fragment.applyImage(cellID, Cache.getBitmapFromMemCache(imageID));
            Log.d("Orbis", "Image was applied for cell " + cellID);
        }
    }

    @Override
    public void onScrollChanged() {
        int currentTop = findViewById(R.id.gallery_scroll).getScrollY();
        int topGridRow = scrollDistanceToGridRow(currentTop);

        if(!alreadyInitializedSpaces) {
            File[] images = FileManager.getFiles(this);
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
        File[] images = FileManager.getFiles(this);
        int numberRequired = images.length / columnCount() + (images.length % columnCount() > 0 ? 1 : 0);
        if(numberRequired < spaceViewIds.length) {
            int[] newSpaceList = new int[numberRequired];
            System.arraycopy(spaceViewIds, 0, newSpaceList, 0, numberRequired);
            for(int i=numberRequired;i<spaceViewIds.length;i++) {
                ((GridLayout) findViewById(R.id.gallery_container)).removeView(findViewById(spaceViewIds[i]));
            }
            spaceViewIds = newSpaceList;
        } else if(numberRequired > spaceViewIds.length) {
            int[] newSpaceList = new int[numberRequired];
            System.arraycopy(spaceViewIds, 0, newSpaceList, 0, spaceViewIds.length);
            for(int i=spaceViewIds.length;i<numberRequired;i++) {
                Space space = new Space(this);
                int viewID = View.generateViewId();
                newSpaceList[i] = viewID;
                space.setId(viewID);
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.height = ((GalleryItemFragment) getSupportFragmentManager().getFragments().get(0)).getHeight();
                layoutParams.width = 1;
                layoutParams.columnSpec = GridLayout.spec(3);
                layoutParams.rowSpec = GridLayout.spec(i);
                space.setLayoutParams(layoutParams);
                ((GridLayout) findViewById(R.id.gallery_container)).addView(space);
            }
            spaceViewIds = newSpaceList;
        }
    }

    @Override
    public void onFragmentInflated(GalleryItemFragment fragment, View view) {
        Log.d("Orbis", "Fragment " + indexOfFragment(fragment) + " was inflated");
        applyScrollToFragment(indexOfFragment(fragment), getCellIdForFragment(indexOfFragment(fragment)), 0, false, view);
    }

    private void applyScrollToFragment(int fragmentID, int cellID, int topRow, boolean forceImageUpdate, View fragmentView) {

        if(!alreadyInitializedSpaces) {
            File[] images = FileManager.getFiles(this);
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
                ((android.support.v7.widget.GridLayout) findViewById(R.id.gallery_container)).addView(space);
            }
            alreadyInitializedSpaces = true;
        }

        if(fragmentID == -1) // Can happen if the user returns to the menu while some fragment have not yet finished inflating
            return;

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
            File[] images = FileManager.getFiles(this);
            if(cellID >= images.length) {
                fragmentView.setVisibility(View.INVISIBLE);
                fragment.setCellID(cellID);
                return;
            }

            fragment.setCellID(cellID);

            fragmentView.setVisibility(View.VISIBLE);
            String filename = images[cellID].getName();
            Log.d("Orbis", "The file for fragment " + fragmentID + " is " + filename);
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
            android.support.v7.widget.GridLayout.LayoutParams layoutParams = (android.support.v7.widget.GridLayout.LayoutParams) fragmentView.getLayoutParams();
            layoutParams.rowSpec = android.support.v7.widget.GridLayout.spec(cellID / columnCount(), 1, android.support.v7.widget.GridLayout.BASELINE, 1);
            layoutParams.columnSpec = android.support.v7.widget.GridLayout.spec(cellID % columnCount(), 1, android.support.v7.widget.GridLayout.START, 1);
            layoutParams.setGravity(Gravity.FILL);

            fragmentView.setLayoutParams(layoutParams);
        }
    }

    /**
     * To be called when images were deleted, a new image created or another folder opened
     */
    private void updateFragmentImages() {
        for(int i=0;i<getSupportFragmentManager().getFragments().size();i++) {
            applyScrollToFragment(i, getCellIdForFragment(i), scrollDistanceToGridRow(findViewById(R.id.gallery_scroll).getScrollY()), true, null);
        }
        File[] images = FileManager.getFiles(this);
        if(images != null) {
            if(images.length == 0) {
                findViewById(R.id.gallery_scroll).setBackgroundResource(R.drawable.gallery_helper);
            } else {
                findViewById(R.id.gallery_scroll).setBackgroundResource(0);
            }
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
        return fragment == null ? -1 : fragment.getFragmentID();
    }

    @Override
    public boolean isItemSelected(int itemID) {
        return getSelectedItems()[itemID];
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
        } else if(getNumberOfSelectedItems() == 1) {
            hideShareIcon();
        }
        setSelectionStatus(itemID, true);
    }

    public void deselectItem(int itemID) {
        if(getNumberOfSelectedItems() == 2) {
            showShareIcon();
        }
        setSelectionStatus(itemID, false);
        if(getNumberOfSelectedItems() == 0) {
            selectionMode = false;
            hideActionBarIcons();
        }
    }

    private final int ANIMATION_DURATION = 500;

    public void showActionBarIcons() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(ANIMATION_DURATION);
        View deleteIcon = findViewById(R.id.delete);
        deleteIcon.setVisibility(View.VISIBLE);
        deleteIcon.startAnimation(fadeIn);
        View shareIcon = findViewById(R.id.share);
        shareIcon.setVisibility(View.VISIBLE);
        shareIcon.startAnimation(fadeIn);
    }

    public void hideActionBarIcons() {
        fadeOutIcon(findViewById(R.id.delete));
        fadeOutIcon(findViewById(R.id.share));
    }

    public void showShareIcon() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(ANIMATION_DURATION);
        View shareIcon = findViewById(R.id.share);
        shareIcon.setVisibility(View.VISIBLE);
        shareIcon.startAnimation(fadeIn);
    }

    public void hideShareIcon() {
        fadeOutIcon(findViewById(R.id.share));
    }

    private void fadeOutIcon(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(ANIMATION_DURATION);
        final View icon = view;
        fadeOut.setAnimationListener(new Animation.AnimationListener(){
            public void onAnimationStart(Animation anim) {}
            public void onAnimationRepeat(Animation anim) {}
            public void onAnimationEnd(Animation anim) {
                icon.setVisibility(View.GONE);
            }
        });
        icon.startAnimation(fadeOut);
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

                        int[] indices = Util.allIndices(true, getSelectedItems());
                        deleteItems(indices);
                    }})
                .setNegativeButton(android.R.string.cancel, null).create();
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                this.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void deleteItems(int[] indices) {
        FileManager.deleteItems(this, indices);

        updateFragmentImages();
        hideActionBarIcons();
        updateSpaces();
    }

    public void shareSelectedItems(View view) {
        if(getNumberOfSelectedItems() == 1) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpg");
            int index = Util.allIndices(true, getSelectedItems())[0]; // There should be only one index
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(FileManager.getFiles(this)[index]));
            startActivity(Intent.createChooser(share, "Share Image"));
        }
    }

    public int getNumberOfSelectedItems() {
        return Util.count(getSelectedItems(), true);
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
        intent.putExtra(GlobalVars.EXTRA_PATH, FileManager.getFiles(this)[itemID].getAbsolutePath());
        intent.putExtra(GlobalVars.EXTRA_IMAGE_ID, itemID);
        Log.d("Orbis", FileManager.getFiles(this)[itemID].getAbsolutePath());
        startActivity(intent);
    }

    public void navigateBack(View view) {
        finish();
    }

    /**
     * This functionality was replaced by ImportActivity
     */
    @Deprecated
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

    public void addItems(View view) {
        Intent intent = new Intent(this, ImportActivity.class);
        startActivity(intent);
    }

    private boolean[] getSelectedItems() {
        if(selectedItems == null) {
            selectedItems = new boolean[FileManager.getFiles(this).length];
            return selectedItems;
        }
        if(selectedItems.length == FileManager.getFiles(this).length) {
            return selectedItems;
        }
        selectedItems = new boolean[FileManager.getFiles(this).length];
        return selectedItems;
    }

    private void setSelectionStatus(int item, boolean status) {
        getSelectedItems(); // Make sure selected items are initialised
        if(item >= selectedItems.length)
            return;
        selectedItems[item] = status;
    }

}
