package bit.harrl7.vrdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Timer;


/**
 * Display an Image on a Panorama view
 * The accelerometer detects when the user looks down, and the app changes the image
 */

public class SimpleVrPanoramaActivity extends Activity implements SensorEventListener
{
    private static final String TAG = SimpleVrPanoramaActivity.class.getSimpleName();
    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;
    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    public boolean loadImageSuccessful;
    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri fileUri;
    /** Configuration information for the panorama. **/
    private Options panoOptions = new Options();
    private ImageLoaderTask backgroundImageLoaderTask;


    public String[] panoImages;
    public  int panoIndex;

    // Readout of the z acceleration for testing
    private TextView txtAccel;

    private  SensorManager mSensorManager;
    private  Sensor mAccelerometer;

    // Bools to check for reduced feature set mode
    private boolean deviceHasGyro;

    // Bool to stop screen transition event triggering multiple times
    private Boolean sceneChangeReady;

    // Camera pitch calibration for non-vr devices
    private Boolean goodAngle;
    private ProgressBar goodAngleBar;

    /**
     * Called when the app is launched via the app icon or an intent using the adb command above. This
     * initializes the app and loads the image to render.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make the source link clickable.
        //TextView sourceText = (TextView) findViewById(R.id.source);
        //sourceText.setText(Html.fromHtml(getString(R.string.source)));
       //sourceText.setMovementMethod(LinkMovementMethod.getInstance());

        panoWidgetView = (VrPanoramaView) findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());

        panoIndex = 0;
        panoImages = new String[4];
        panoImages[0] = "trainstation.jpg";
        panoImages[1] = "walterpeak.jpg";
        panoImages[2] = "tekapo.jpg";
        panoImages[3] = "wakatipu.jpg";

        sceneChangeReady = false;
        goodAngle = false;
        goodAngleBar = (ProgressBar) findViewById(R.id.progBarGoodAngle);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        txtAccel = (TextView) findViewById(R.id.txtAccel);

        deviceHasGyro = false;
        if(!deviceHasGyro) panoWidgetView.setFullscreenButtonEnabled(false);
        panoWidgetView.setInfoButtonEnabled (false);

        Button btnNextImg = (Button) findViewById(R.id.btnNext);
        btnNextImg.setOnClickListener(new ButtonNextImageHandler());

        //VrPanoramaView panoView = (VrPanoramaView) findViewById(R.id.pano_view);

        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());
    }


    // Next button click
    public class ButtonNextImageHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if(goodAngle) panoWidgetView.setDisplayMode(2);
            else Toast.makeText(SimpleVrPanoramaActivity.this, "Please hold device upright landscape", Toast.LENGTH_LONG).show();
        }
    }


    // Read accelerometer
    @Override
    public void onSensorChanged(SensorEvent e)
    {
        float zAcceleration = e.values[2];

        DecimalFormat df = new DecimalFormat("#.####");
        txtAccel.setText(df.format(zAcceleration));
        txtAccel.bringToFront();

        // If looking down change scene, lock scene changing
        if (zAcceleration > 9 && sceneChangeReady)
        {
            sceneChangeReady = false;
            goToNextScene();
        }

        // If not looking down unlock scene changing
        if (zAcceleration < 4) { sceneChangeReady = true; }


        // check for good value for non vr viewing
        goodAngle = false;
        if (zAcceleration > -1 && zAcceleration < 1) goodAngle = true;

        int progress = (int) (zAcceleration * 10 + 100);
        goodAngleBar.setProgress(progress);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do nothing
    }


    /**
     *  Transition to the next pano image
     */
    public void goToNextScene()
    {
        panoIndex = (panoIndex+1)%panoImages.length;

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundImageLoaderTask != null)
        {
            // Cancel any task from a previous intent sent to this activity.
            backgroundImageLoaderTask.cancel(true);
        }
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }



    /**
     * Called when the Activity is already running and it's given a new intent.
     */
    @Override
    protected void onNewIntent(Intent intent)
    {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent(intent);
    }


    /**
     * Load custom images based on the Intent or load the default image. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    private void handleIntent(Intent intent)
    {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_VIEW Intent recieved");

            fileUri = intent.getData();
            if (fileUri == null) {
                Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.i(TAG, "Using file " + fileUri.toString());
            }

            panoOptions.inputType = intent.getIntExtra("inputType", Options.TYPE_MONO);
            Log.i(TAG, "Options.inputType = " + panoOptions.inputType);
        } else {
            Log.i(TAG, "Intent is not ACTION_VIEW. Using default pano image.");
            fileUri = null;
            panoOptions.inputType = Options.TYPE_MONO;
        }

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundImageLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundImageLoaderTask.cancel(true);
        }
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }

    @Override
    protected void onPause() {
        panoWidgetView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoWidgetView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTask extends AsyncTask<Pair<Uri, Options>, Void, Boolean>
    {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Uri, Options>... fileInformation)
        {
            Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;

            if (fileInformation == null || fileInformation.length < 1 || fileInformation[0] == null || fileInformation[0].first == null)
            {
                AssetManager assetManager = getAssets();

                try {
                    istr = assetManager.open(panoImages[panoIndex]);
                    panoOptions = new Options();
                    panoOptions.inputType = Options.TYPE_MONO;
                } catch (IOException e) {
                    Log.e(TAG, "Could not decode default bitmap: " + e);
                    return false;
                }
            } else {
                try {
                    istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
                    panoOptions = fileInformation[0].second;
                } catch (IOException e) {
                    Log.e(TAG, "Could not load file: " + e);
                    return false;
                }
            }

            panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);

            try {
                istr.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream: " + e);
            }

            return true;
        }
    }


    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener
    {

        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage)
        {
            loadImageSuccessful = false;
            Toast.makeText(SimpleVrPanoramaActivity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }


    private void fadeOutAndHideImage(final VrPanoramaView img)
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }
}
