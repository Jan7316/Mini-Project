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
    private Bitmap image;

    public AsyncTaskLoadVRImage(ITaskParent parent, String imagePath) {
        this.parent = parent;
        this.imagePath = imagePath;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if(!isCancelled()) {
            BitmapFactory.Options loadingOptions = new BitmapFactory.Options();
            loadingOptions.inScaled = false;
            image = BitmapFactory.decodeFile(imagePath, loadingOptions);
            return image;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        parent.onImagesLoaded(image);
    }

    interface ITaskParent {
        void onImagesLoaded(Bitmap image);
    }

}