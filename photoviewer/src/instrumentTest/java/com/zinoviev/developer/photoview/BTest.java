package com.zinoviev.developer.photoview;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import com.robotium.solo.Solo;
import com.zinoviev.developer.photoviewer.R;

public class BTest extends ActivityInstrumentationTestCase2<StartActivity> {

    protected Solo solo;

    public BTest() {
        super(StartActivity.class);
    }

    @Override
    public void setUp() throws Exception {

        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
