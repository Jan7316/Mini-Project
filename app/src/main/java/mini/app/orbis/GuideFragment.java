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
import android.widget.TextView;

/**
 * Activities that contain this fragment must implement the
 * {@link GuideFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GuideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GuideFragment extends Fragment {

    private View layout;

    private int guideID;

    private OnFragmentInteractionListener mListener;

    public GuideFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GuideFragment.
     */
    public static GuideFragment newInstance(int guideID, String title, int titleImageResourceID) {
        GuideFragment fragment = new GuideFragment();
        Bundle args = new Bundle();
        args.putInt(GlobalVars.GUIDE_ID, guideID);
        args.putString(GlobalVars.GUIDE_TITLE, title);
        args.putInt(GlobalVars.GUIDE_IMAGE, titleImageResourceID);
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

        layout = inflater.inflate(R.layout.fragment_guide, container, false);

        guideID = getArguments().getInt(GlobalVars.GUIDE_ID);
        String title = getArguments().getString(GlobalVars.GUIDE_TITLE);
        int titleImageResourceID = getArguments().getInt(GlobalVars.GUIDE_IMAGE);

        ((TextView) layout.findViewById(R.id.title)).setText(title);
        ((ImageView) layout.findViewById(R.id.image)).setImageResource(titleImageResourceID);

        FontManager.applyFontToView(getContext(), (TextView) layout.findViewById(R.id.title), FontManager.Font.lato);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openGuide(guideID);
            }
        });

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
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
        void openGuide(int guideID);
    }

}
