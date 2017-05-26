package bit.harrl7.visscan;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.nfc.Tag;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.cacheColorHint;
import static android.R.attr.data;
import static android.view.Gravity.START;
import static android.view.View.*;

public class MainActivity extends AppCompatActivity
{

    public  enum ETestType { Flash, Wander }

    private int updateDelay = 30;

    TextView tvPause;
    DrawerLayout drawer;

    RadioButton rdoFlashTest;
    RadioButton rdoWanderTest;

    // Display dimensions
    int height;
    int width;

    Thread timer;

    // Fragment for visual test
    IVisualTest testFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        // Controls
        tvPause = (TextView) findViewById(R.id.tvPause);
        rdoFlashTest = (RadioButton) findViewById(R.id.rdoFlash);
        rdoWanderTest = (RadioButton) findViewById(R.id.rdoWander);

        // Drawer
        drawer = (DrawerLayout) findViewById(R.id.activity_flash_stim);
        drawer.setDrawerListener(new DrawSlidePauseHandler());

        // Start button
        Button btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new ButtonStartClickHandler());

        // Save button
        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new ButtonSaveResultsClickHandler());
    }


    // Open test fragment
    public void ShowTestFrag(ETestType type)
    {
        Fragment dynamicFragment = null;
        switch (type)
        {
            case Flash:
                dynamicFragment = new FlashStimFrag();
                break;
            case Wander:
                dynamicFragment = new WanderStimFrag();
                break;
        }

        FragmentManager fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        if(dynamicFragment != null) { ft.replace(R.id.fragment_container, dynamicFragment); }
        ft.commit();

        testFrag = (IVisualTest) dynamicFragment;
    }


    // Start the test
    public class ButtonStartClickHandler implements OnClickListener
    {

        @Override
        public void onClick(View view)
        {
            drawer.closeDrawers();
            tvPause.setVisibility(GONE);
            //FlashStimFrag fragment = (FlashStimFrag) getFragmentManager().findFragmentById(R.id.fragment_container);


            // Create flash test fragment
            if(rdoFlashTest.isChecked())
            {
                ShowTestFrag(ETestType.Flash);
                //fragment.NextCycle();
            }

            // Create wander test fragment
            if(rdoWanderTest.isChecked())
            {
                ShowTestFrag(ETestType.Wander);
            }
            RunTimer();
        }
    }

    // Save results
    public class ButtonSaveResultsClickHandler implements OnClickListener
    {
        @Override
        public void onClick(View view)
        {
            if(testFrag == null) { Toast.makeText(MainActivity.this, "No reults to save", Toast.LENGTH_LONG).show(); }
            else { OutputTestResults(); }
        }
    }


    // Pause on draw slide
    public class DrawSlidePauseHandler implements DrawerLayout.DrawerListener
    {

        @Override
        public void onDrawerOpened(View drawerView)
        {
            if(timer != null) timer.interrupt();
            tvPause.setVisibility(VISIBLE);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) { }  // Empty

        @Override
        public void onDrawerClosed(View drawerView) { } // Empty

        @Override
        public void onDrawerStateChanged(int newState) { } // Empty
    }


    //  Start the timer, run current fragment loop
    public void RunTimer()
    {

        timer = new Thread()
        {

            @Override
            public void run()
            {
                try {
                    while (!isInterrupted())
                    {
                        Thread.sleep(updateDelay);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                testFrag.Run();
                            }
                        });
                    }
                } catch (InterruptedException e) { } // Handle error
            }
        };

        timer.start();
    }


    // Print results to .csv file at /VisScan
    public void OutputTestResults()
    {

        // Storage dir
        String docsFolder = Environment.getExternalStorageDirectory().toString();

        // Outer folder, make if doesn't exist already
        File resultsFolder = new File(docsFolder +"/VisScan");
        if(!resultsFolder.exists())
        {
            resultsFolder.mkdirs();
        }


        // Create results txt

        // Date
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy_HH:mm:ss");
        String fileName = df.format(new Date());

        // Filename
        File file = new File(resultsFolder.getAbsoluteFile().toString(), testFrag.GetTestType() + "_" + fileName + ".csv");
        try
        {
            file.createNewFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }


        // Write to file

        // Get results data
        String csv = "";

        try
        {
            IVisualTest fragment = (IVisualTest) getFragmentManager().findFragmentById(R.id.fragment_container);
            csv = fragment.ToCSV();
        }
        catch (NullPointerException e) {}

        // Write results
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.append(csv);
            Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Screen bounds
    public Point GetBounds() { return new Point(width, height); }

    // Update frequency, use to time test logic in seconds
    public double GetUpdateFreg() { return 1000/updateDelay; }

}
