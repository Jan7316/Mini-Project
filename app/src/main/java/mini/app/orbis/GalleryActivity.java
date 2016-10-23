package mini.app.orbis;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class GalleryActivity extends AppCompatActivity implements GalleryItemFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.gallery_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            String path = Environment.getExternalStorageDirectory().toString()+"/Orbis"; // TODO: Apparently this only checks the PRIMARY extenral storage (usually build-in)
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles(); // TODO: check for empty files array
            Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName:" + files[i].getName());
                Log.d("Files", "Full Path:" + files[i].getAbsolutePath());

                // Create a new Fragment to be placed in the activity layout
                GalleryItemFragment firstFragment = GalleryItemFragment.newInstance(files[i].getAbsolutePath());

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.gallery_container, firstFragment).commit();
            }

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

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
