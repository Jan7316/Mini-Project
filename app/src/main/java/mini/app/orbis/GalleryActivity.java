package mini.app.orbis;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements GalleryItemFragment.OnFragmentInteractionListener, ViewTreeObserver.OnScrollChangedListener {

    private final String[] IMG_EXTENSIONS = {"jpg", "png", "gif", "bmp", "webp"};

    private boolean reInitFragments, alreadyInitializedSpaces;

    public File[] images;

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
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files == null) {
            images = null;
        } else if(files.length == 0) {
            images = null;
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

        findViewById(R.id.gallery_scroll).getViewTreeObserver().addOnScrollChangedListener(this);

        if(savedInstanceState != null) {
            return;
        }

        for(int i = 0; i < Math.min(viewportHeightInGridRows() * columnCount(), images.length); i++) {
            GalleryItemFragment firstFragment = GalleryItemFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.gallery_container, firstFragment).commit();
        }

        findViewById(R.id.actions).setVisibility(View.GONE);
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
            for(int i=0;i<images.length / columnCount() + (images.length % columnCount() > 0 ? 1 : 0);i++) {
                Space space = new Space(this);
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
            applyScrollToFragment(i, getCellIdForFragment(i), topGridRow);
        }
        reInitFragments = false;
    }

    public void onFragmentInflated(GalleryItemFragment fragment) {
        int topGridRow = 0;
        applyScrollToFragment(getSupportFragmentManager().getFragments().indexOf(fragment), (topGridRow * columnCount()) + getSupportFragmentManager().getFragments().indexOf(fragment), 0); // TODO the 0 should be the current top row
    }

    private void applyScrollToFragment(int fragmentID, int cellID, int topRow) {
        GalleryItemFragment fragment = (GalleryItemFragment) getSupportFragmentManager().getFragments().get(fragmentID);
        if(!(!(fragment.getCurrentCellID() == cellID) || reInitFragments)) {
            // The fragment is already displaying the correct image
        } else {
            if(cellID >= images.length) {
                return;
            }
            String filename = images[cellID].getName(); // TODO compatibility with duplicate file names (eg example.jpg/example.png)
            if(Cache.isBitmapCached(filename)) {
                Log.d("Orbis", "Loading from cache " + fragmentID);
                fragment.applyImage(cellID, Cache.getBitmapFromMemCache(filename));
            } else if(!Cache.isBitmapBeingLoaded(filename)) {
                Log.d("Orbis", "Async task started for fragment " + fragmentID);
                Cache.markBitmapAsBeingLoaded(filename, true);
                fragment.markAsLoading();
                new AsyncTaskLoadImage(this, cellID, filename, images[cellID]).execute();
            }
            if(fragment.getView() == null) // Presumably the fragment has not yet been inflated
                return;
            GridLayout.LayoutParams layoutParams = (GridLayout.LayoutParams) fragment.getView().getLayoutParams();
            layoutParams.rowSpec = GridLayout.spec(cellID / columnCount());
            layoutParams.columnSpec = GridLayout.spec(cellID % columnCount());

            fragment.getView().setLayoutParams(layoutParams);
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
        return (GalleryItemFragment) getSupportFragmentManager().getFragments().get(((cellID / columnCount()) % viewportHeightInGridRows()) + (cellID % columnCount()));
    }

    private int scrollDistanceToGridRow(int px) {
        if(getSupportFragmentManager().getFragments() == null) {
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
        return getSupportFragmentManager().getFragments().indexOf(fragment);
    }

    public File[] toFileArray(Object[] array) {
        File[] a = new File[array.length];
        for(int i=0;i<array.length;i++) {
            a[i] = (File) array[i];
        }
        return a;
    }


    public boolean isSelectionModeOn() {
        return selectionMode;
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
            Log.d("Orbis", "Selection status was inverted for item " + itemID);
        } else {
            if(isLongClick) {
                selectItem(itemID); // This also sets selectionMode to true
                fragment.updateColorFilter();
                Log.d("Orbis", "Item " + itemID + " was selected, selection mode should now be on");
            } else {
                Log.d("Orbis", "Image " + itemID + " was clicked");
            }
        }
    }

}
