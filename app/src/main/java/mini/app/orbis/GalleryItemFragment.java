package mini.app.orbis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

/**
 * Activities that contain this fragment must implement the
 * {@link GalleryItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GalleryItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryItemFragment extends Fragment {
    private static final String ARG_PATH = "path";
    private String path;

    private View layout;

    private int cellID = -1;

    private final String[] IMG_EXTENSIONS = {"jpg", "png", "gif", "bmp", "webp"};

    private OnFragmentInteractionListener mListener;

    public GalleryItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GalleryItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryItemFragment newInstance() {
        GalleryItemFragment fragment = new GalleryItemFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_gallery_item, container, false);

        if (path == null) {
            return layout;
        }

        File imgFile = new File(path);
        if (imgFile.exists()) {
            //if (isImageExtension(imgFile.getAbsolutePath().substring(imgFile.getAbsolutePath().lastIndexOf(".") + 1, imgFile.getAbsolutePath().length()))) {
            //    Bitmap myBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imgFile.getAbsolutePath()), 400, 300);
            //    ImageView myImage = (ImageView) layout.findViewById(R.id.image);
            //    myImage.setImageBitmap(myBitmap);
            //}
        }

        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    public int getCurrentCellID() {
        return cellID;
    }

    public void applyImage(int cellID, Bitmap bitmap) {
        this.cellID = cellID;
        Log.d("Orbis", "Image was applied for cell " + cellID);
        if(!(getView() == null)) {
            ImageView imageView = (ImageView) getView().findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mListener.onFragmentInflated(this);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInflated(GalleryItemFragment fragment);
    }

    public int getHeight() {
        if(getView() == null)
            return 0;
        return getView().getHeight();
    }

}
