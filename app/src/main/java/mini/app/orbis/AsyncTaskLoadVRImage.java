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

public class AsyncTaskLoadVRImage extends AsyncTask {

    private ITaskParent parent;
    private String imagePath;
    private Bitmap left, right;

    public AsyncTaskLoadVRImage(ITaskParent parent, String imagePath) {
        this.parent = parent;
        this.imagePath = imagePath;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if(!isCancelled()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            left = Bitmap.createBitmap(bitmap, 0, 0, width / 2, height); // This may or may not work with .jps files
            right = Bitmap.createBitmap(bitmap, width / 2, 0, width / 2 - 1, height);
            return bitmap;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        parent.onImagesLoaded(left, right);
    }

    interface ITaskParent {
        void onImagesLoaded(Bitmap left, Bitmap right);
    }

}