package bit.harrl7.firstvrapp;


import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment
{
    private VrPanoramaView panoWidgetView;
    private ImageLoaderTask backgroundImageLoaderTask;

    public WelcomeFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_welcome, container,false);
        panoWidgetView = (VrPanoramaView) v.findViewById(R.id.pano_view);
        return v;
    }

    @Override
    public void onPause()
    {
        panoWidgetView.pauseRendering();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        panoWidgetView.resumeRendering();
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        // Destroy the widget and free memory.
        panoWidgetView.shutdown();
        super.onDestroy();
    }


    // This will create a new loader task and start it.
    private synchronized void loadPanoImage()
    {
        ImageLoaderTask task = backgroundImageLoaderTask;
        if (task != null && !task.isCancelled()) {
            // Cancel any task from a previous loading.
            task.cancel(true);
        }

        // pass in the name of the image to load from assets.
        VrPanoramaView.Options viewOptions = new VrPanoramaView.Options();
        viewOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;

        // use the name of the image in the assets/ directory.
        String panoImageName = "converted.jpg";

        // create the task passing the widget view and call execute to start.
        task = new ImageLoaderTask(panoWidgetView, viewOptions, panoImageName);
        task.execute(getActivity().getAssets());
        backgroundImageLoaderTask = task;
    }

    // Start image loading
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        loadPanoImage();
    }

}
