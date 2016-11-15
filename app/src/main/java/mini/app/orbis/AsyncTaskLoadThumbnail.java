package mini.app.orbis;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

/**
 * Created by Jan on 23/10/2016.
 */

public class AsyncTaskLoadThumbnail extends AsyncTask {

    private GalleryActivity parent;
    private int cellID;
    private String id;
    private File file;

    public AsyncTaskLoadThumbnail(GalleryActivity parent, int cellID, String id, File file) { // The file must be an existing image file
        this.parent = parent;
        this.cellID = cellID;
        this.id = id;
        this.file = file;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if(!isCancelled()) {
            Log.d("Orbis", "Working on " + cellID);
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()), 800, 300); // TODO the proportions of this may be way out of line
            Bitmap leftHalf = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / 2, bitmap.getHeight());
            Cache.addBitmapToMemoryCache(id, leftHalf);
            return bitmap;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        parent.notifyImageLoaded(cellID, id);
    }
}