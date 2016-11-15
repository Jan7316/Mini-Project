package mini.app.orbis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Activities that contain this fragment must implement the
 * {@link GalleryItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GalleryItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryItemFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private View layout;

    private int cellID = -1;

    private int fragmentID;

    private OnFragmentInteractionListener mListener;

    public GalleryItemFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GalleryItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryItemFragment newInstance(int ID) {
        GalleryItemFragment fragment = new GalleryItemFragment();
        Bundle args = new Bundle();
        args.putInt(GlobalVars.FRAGMENT_ID, ID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Orbis", "OnFragmentInflated called");
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_gallery_item, container, false);

        View imageView = layout.findViewById(R.id.image);

        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);

        fragmentID = getArguments().getInt(GlobalVars.FRAGMENT_ID);

        mListener.onFragmentInflated(this, layout);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public int getCurrentCellID() {
        return cellID;
    }

    public void markAsLoading() {
        if(getView() != null)
            ((ImageView) getView().findViewById(R.id.image)).setImageResource(R.drawable.ic_loading_image);
    }

    public void applyImage(int cellID, Bitmap bitmap) {
        this.cellID = cellID;
        if(!(getView() == null)) {
            ImageView imageView = (ImageView) getView().findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
            updateColorFilter();
            imageView.invalidate();
            Log.d("Orbis", "Image was set on cell " + cellID);
        } else {
            Log.d("Orbis", "Image could not be set as view was null");
        }
    }

    /**
     * The view argument only needs to be passed if the image is loaded directly from onCreateView() as getView() in such a case would be null
     */
    public void applyImage(int cellID, Bitmap bitmap, View view) {
        this.cellID = cellID;
        if(!(getView() == null)) {
            ImageView imageView = (ImageView) getView().findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
            updateColorFilter();
            imageView.invalidate();
            Log.d("Orbis", "Image was set on cell " + cellID);
        } else {
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
            imageView.invalidate();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public interface OnFragmentInteractionListener {
        void onFragmentInflated(GalleryItemFragment fragment, View view);
        void onClick(boolean isLongClick, int itemID, GalleryItemFragment fragment);
        boolean isItemSelected(int itemID);
    }

    public int getHeight() {
        if(getView() == null)
            return 0;
        return getView().getHeight();
    }

    @Override
    public void onClick(View v) {
        mListener.onClick(false, cellID, this);
    }

    @Override
    public boolean onLongClick(View v) {
        mListener.onClick(true, cellID, this);
        return true; // Cancel the onClick event
    }

    public void updateColorFilter() {
        if(mListener.isItemSelected(cellID)) {
            ((ImageView) getView().findViewById(R.id.image)).setColorFilter(Color.argb(100, 0, 0, 0));
        } else {
            ((ImageView) getView().findViewById(R.id.image)).setColorFilter(Color.argb(0, 0, 0, 0));
        }
    }

    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    public int getFragmentID() {
        return fragmentID;
    }

}
