package com.google.devrel.vrviewapp;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Liam on 28-Mar-17.
 *
 * since the image is large we don't want to load it in the main UI thread when the application starts.
 * We want to load it asynchronously.
 *
 * It needs to extend the AsyncTask class so it can perform the image loading in a background thread. The parameters to the AsyncTask are:
 *
     AssetManager to use to load the image.
     Void parameters to the progress method (which we don't implement).
     Bitmap return value back to the main thread.
 */

public class ImageLoaderTask extends AsyncTask<AssetManager, Void, Bitmap>
{
    /*
        When we load the image, we need to pass which image to load and where to display it.
        We'll pass this information via the constructor.Also add a TAG member so we can log errors
    */
    private static final String TAG = "ImageLoaderTask";
    private final String assetName;
    private final WeakReference<VrPanoramaView> viewReference;
    private final VrPanoramaView.Options viewOptions;

    /*
        To avoid re-loading the image when the device is rotated, we'll cache the last image loaded.
        We do this by keeping the name of the last asset loaded, and the resulting bitmap.
    */
    private static WeakReference<Bitmap> lastBitmap = new WeakReference<>(null);
    private static String lastName;




    public ImageLoaderTask(VrPanoramaView view, VrPanoramaView.Options viewOptions, String assetName)
    {
        viewReference = new WeakReference<>(view);
        this.viewOptions = viewOptions;
        this.assetName = assetName;
    }




    /*
        We've added the image to the assets directory of the project, so we'll use the AssetManager to get an InputStream to the image.
        Then pass that input stream to the BitmapFactory to load the image and return it back to the main thread.
        If there is a problem, we'll log it and return a null image. We check the last image loaded before opening the stream in order to conserve memory usage.
     */
    @Override
    protected Bitmap doInBackground(AssetManager... params)
    {
        AssetManager assetManager = params[0];

        if (assetName.equals(lastName) && lastBitmap.get() != null) {
            return lastBitmap.get();
        }

        try(InputStream istr = assetManager.open(assetName))
        {
            Bitmap b = BitmapFactory.decodeStream(istr);
            lastBitmap = new WeakReference<>(b);
            lastName = assetName;
            return b;
        } catch (IOException e) {
            Log.e(TAG, "Could not decode default bitmap: " + e);
            return null;
        }
    }


    /*
        Back in the main thread, we can render the bitmap in the VrPanoramaView.
        Once the background work is done, the onPostExecute(Bitmap bitmap) method is called by AsyncTask on the main thread.
        In here, we'll pass the bitmap to the VrPanoramaView.
     */
    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        final VrPanoramaView vw = viewReference.get();
        if (vw != null && bitmap != null) {
            vw.loadImageFromBitmap(bitmap, viewOptions);
        }
    }
}
