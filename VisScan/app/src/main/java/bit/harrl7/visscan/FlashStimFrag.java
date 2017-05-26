package bit.harrl7.visscan;


import android.app.Activity;
import android.app.Notification;
import android.app.Notification.MessagingStyle.Message;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.zip.Inflater;

import static java.lang.System.in;


/**
 * A static stimulus will appear periodicaly, prompting the user for input
 */
public class FlashStimFrag extends Fragment implements IVisualTest
{
    // Display
    ImageView ivFlashStim;
    FrameLayout stimContainer;
    TextView tv;
    Point bounds;

    // Stimuli
    FlashStimTrial currentStim;
    ArrayList stimList;

    // Cycle control
    boolean isDrawCycle;
    double noShowRate;
    private int cycleDelay;
    int cycleTick;


    public FlashStimFrag()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_flash_stim, container, false);

        // Screen bounds for stim placement
        MainActivity main = (MainActivity) getActivity();
        bounds =  main.GetBounds();

        // Toggle for draw/ rest cycles
        isDrawCycle = true;

        // Stim record
        stimList = new ArrayList<>();

        // Screen controls
        ivFlashStim = (ImageView) v.findViewById(R.id.ivStim);
        tv = (TextView) v.findViewById(R.id.textView);
        stimContainer = (FrameLayout) v.findViewById(R.id.stimContainer);

        // On click for full screen layout
        RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.rl);
        rl.setOnClickListener(new ScreenCickHandler());

        // Cycle control
        int aSecond = (int) ((MainActivity) getActivity()).GetUpdateFreg();
        isDrawCycle = true;
        noShowRate = 0.1;
        cycleDelay = aSecond * 2;
        cycleTick = cycleDelay;


        return v;
    }

    @Override
    public void Run()
    {
        cycleTick++;

        if(cycleTick > cycleDelay)
        {
            cycleTick = 0;
            NextCycle();
        }
    }


    // Screen touch event
    public class ScreenCickHandler implements OnClickListener
    {

        @Override
        public void onClick(View view)
        {
            // Hide stim and set hit to true
            ivFlashStim.setVisibility(View.INVISIBLE);
            if(currentStim != null) { currentStim.hit = true; }
        }
    }


    // Start the next cycle, draw or rest
    public void NextCycle()
    {
        isDrawCycle = !isDrawCycle;

        if(isDrawCycle)
        {
            MoveStim();
        }
        else
        {
            // On rest cycle: hide stim, print previous stim record
            ivFlashStim.setVisibility(View.GONE);
            if(currentStim != null) { tv.setText(currentStim.ToCsv()); }
        }
    }


    // Move the stim and create a new record
    public void MoveStim()
    {
        // RNG for stim position and noShow trials
        int x = (int) (Math.random() * (bounds.x-ivFlashStim.getWidth()));
        int y = (int) (Math.random() * (bounds.y-ivFlashStim.getHeight()));
        boolean showTrial = (Math.random() > noShowRate);


        // Set padding of stim container to move stim
        stimContainer.setPadding(x, y, 0, 0);

        // Create new stim as current stim
        currentStim = new FlashStimTrial(new Point(x,y), showTrial);
        stimList.add(currentStim);

        // If show trial, show stim
        if(showTrial) { ivFlashStim.setVisibility(View.VISIBLE); }

    }

    // Return test results as CSV String
    public String ToCSV()
    {
        String s = "X, Y, Hit, Shown\r\n";

        for(Object item : stimList)
        {
            FlashStimTrial stim = (FlashStimTrial) item;
            s += stim.ToCsv() +"\r\n";
        }

        return s;
    }

    public String GetTestType()
    {
        return "Flash";
    }


}
