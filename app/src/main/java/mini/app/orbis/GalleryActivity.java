package mini.app.orbis;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements GalleryItemFragment.OnFragmentInteractionListener, ViewTreeObserver.OnScrollChangedListener {

    private final String[] IMG_EXTENSIONS = {"jpg", "png", "gif", "bmp", "webp"};

    private boolean reInitFragments, alreadyInitializedSpaces;

    public File[] images;
    //public GalleryItemFragment[] items;
    ArrayList<AsyncTaskLoadImage> tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        String path = Environment.getExternalStorageDirectory().toString() + "/Orbis"; // TODO: Apparently this only refers to the PRIMARY (built-in) external storage
        File directory = new File(path);
        File[] files = directory.listFiles(); // TODO: check for empty files array
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
        }

        findViewById(R.id.gallery_scroll).getViewTreeObserver().addOnScrollChangedListener(this);

        if(savedInstanceState != null) {
            return;
        }

        //items = new GalleryItemFragment[viewportHeightInGridRows() * columnCount()]; // TODO CAUTION: THIS WILL NOT WORK DYNAMICALLY AS GRID ROW HEIGHT WOULD DEPEND ON THE FRAGMENTS WHICH DO NOT YET EXIST

        for(int i = 0; i < viewportHeightInGridRows() * columnCount(); i++) {
            // Create a new Fragment to be placed in the activity layout
            GalleryItemFragment firstFragment = GalleryItemFragment.newInstance();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.gallery_container, firstFragment).commit();

            //items[i] = firstFragment;
        }



        // -------------------------------------------------------------------------------------------------

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        //if(findViewById(R.id.gallery_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
          // Log.d("Files", "Path: " + path);
            //File directory = new File(path);
            //File[] files = directory.listFiles(); // TODO: check for empty files array
           /// for(int i = 0; i < files.length; i++) {
             //   Log.d("Files", "FileName:" + files[i].getName());
             //   Log.d("Files", "Full Path:" + files[i].getAbsolutePath());

                // Create a new Fragment to be placed in the activity layout
             //   GalleryItemFragment firstFragment = GalleryItemFragment.newInstance();

                // Add the fragment to the 'fragment_container' FrameLayout
             //   getSupportFragmentManager().beginTransaction()
             //           .add(R.id.gallery_container, firstFragment).commit();
            //}

            /*for(int i=0;i<20;i++) {
                // Create a new Fragment to be placed in the activity layout
                GalleryItemFragment firstFragment = new GalleryItemFragment();

                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments
                firstFragment.setArguments(getIntent().getExtras());

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.gallery_container, firstFragment).commit();
            }*/
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
        if(getCellIdForFragment(indexOfFragment(fragment)) == cellID) {      // Unlike getFragmentForCellId, this takes into account the current scroll position
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
            for(int i=0;i<images.length / columnCount() + 1;i++) {
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
            //Log.d("Orbis", "Fragment " + i + " is now cell " + getCellIdForFragment(i));
            applyScrollToFragment(i, getCellIdForFragment(i), topGridRow);
        }
        reInitFragments = false;
    }

    public void onFragmentInflated(GalleryItemFragment fragment) {
        int topGridRow = 0;
        Log.d("Orbis", "Top row set to 0");
        applyScrollToFragment(getSupportFragmentManager().getFragments().indexOf(fragment), (topGridRow * columnCount()) + getSupportFragmentManager().getFragments().indexOf(fragment), 0); // TODO the 0 should be the current top row
    }

    private void applyScrollToFragment(int fragmentID, int cellID, int topRow) {
        GalleryItemFragment fragment = (GalleryItemFragment) getSupportFragmentManager().getFragments().get(fragmentID);
        // Nothing has to happen here
        if(!(!(fragment.getCurrentCellID() == cellID) || reInitFragments)) {

        } else {                                        // TODO THE APP NEEDS THE ORBIS FOLDER TO CONTAIN MIN 12 IMAGES!!
            if(cellID >= images.length) {
                return;
            }
            String filename = images[cellID].getName(); // TODO CAUTION THIS WILL NOT WORK IF THERE ARE DUPLICATE FILE NAMES (EG MULTIPLE FOLDERS/DIFFERENT FILE TYPES)
            if(Cache.isBitmapCached(filename)) {
                Log.d("Orbis", "Loading from cache " + fragmentID);
                fragment.applyImage(cellID, Cache.getBitmapFromMemCache(filename));
            } else if(!Cache.isBitmapBeingLoaded(filename)) {
                Log.d("Orbis", "Async task started for fragment " + fragmentID);
                Cache.markBitmapAsBeingLoaded(filename, true);
                if(fragment.getView() != null)
                    ((ImageView) fragment.getView().findViewById(R.id.image)).setImageResource(R.drawable.picture);
                new AsyncTaskLoadImage(this, cellID, filename, images[cellID]).execute();
            }
            if(fragment.getView() == null) // Presumably the fragment has not yet been inflated
                return;
            GridLayout.LayoutParams layoutParams = (GridLayout.LayoutParams) fragment.getView().getLayoutParams();
            layoutParams.rowSpec = GridLayout.spec(cellID / columnCount()); // TODO HIGHLY UNTESTED
            layoutParams.columnSpec = GridLayout.spec(cellID % columnCount());

            if(cellID % columnCount() == columnCount() - 1) {
               // Space space = (Space) findViewById(R.id.topSpace);
                //LinearLayout.LayoutParams spaceLayoutParams = (LinearLayout.LayoutParams) space.getLayoutParams();
                //spaceLayoutParams.height = topRow * ((GalleryItemFragment) getSupportFragmentManager().getFragments().get(0)).getHeight();
                //space.setLayoutParams(spaceLayoutParams);
            }
            fragment.getView().setLayoutParams(layoutParams);
        }
    }

    private int getCellIdForFragment(int fragmentID) { // TODO THESE FORMULAS DO NOT WORK
        int currentTopRow = scrollDistanceToGridRow(findViewById(R.id.gallery_scroll).getScrollY());
        int fragmentInitialCol = fragmentID % columnCount();
        int fragmentInitialRow = fragmentID / columnCount();
        /*int currentTopAsInitialRow = currentTopRow % viewportHeightInGridRows();
        Log.d("Orbis", "Calculations: fragment " + fragmentID + ": initial row " + fragmentInitialRow + ", currentTopAsInitialRow " + currentTopAsInitialRow);
        if(currentTopAsInitialRow == fragmentInitialRow) {
            return currentTopRow * columnCount() + fragmentInitialCol;
        } else if(currentTopAsInitialRow < fragmentInitialRow) {
            return (currentTopRow + fragmentInitialRow - currentTopAsInitialRow) * columnCount() + fragmentInitialCol;
        } else {
            return (currentTopRow + 1 + fragmentInitialRow) * columnCount() + fragmentInitialCol;
        }*/

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

    public <E> int indexOf(E element, E[] array) {
        for(int i=0;i<array.length;i++) {
            if(array[i].equals(element))
                return i;
        }
        return -1;
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

}
