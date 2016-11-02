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

public class AsyncTaskLoadImage extends AsyncTask {

    private GalleryActivity parent;
    private int cellID;
    private String id;
    private File file;

    public AsyncTaskLoadImage(GalleryActivity parent, int cellID, String id, File file) { // The file must be an existing image file
        this.parent = parent;
        this.cellID = cellID;
        this.id = id;
        this.file = file;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if(!isCancelled()) {
            Log.d("Orbis", "Working on " + cellID);
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()), 400, 300);
            Cache.addBitmapToMemoryCache(id, bitmap);
            return bitmap;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        parent.notifyImageLoaded(cellID, id);
    }
}