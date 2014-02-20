package com.zinoviev.developer.photoview;

import android.widget.EditText;
import com.robotium.solo.Solo;
import com.zinoviev.developer.photoviewer.R;

public class OneTest extends BTest {

    public void test_ActivityTest() {
        solo.assertCurrentActivity("wrong activity", StartActivity.class);
    }

    public void test_EditTextAndButtonTest() {
        EditText mEditText = (EditText) solo.getView(R.id.text);
        solo.clearEditText(mEditText);
        solo.enterText(mEditText, "girls");

        solo.clickOnButton(solo.getString(R.string.hello_world));
        solo.sleep(3000);
        solo.goBackToActivity("StartActivity");
    }

    public void test_OrientationTest() {
        solo.clickOnButton(solo.getString(R.string.hello_world));
        solo.sleep(3000);
        solo.setActivityOrientation(Solo.LANDSCAPE);
        solo.sleep(3000);
        solo.setActivityOrientation(Solo.PORTRAIT);
        solo.sleep(3000);
        solo.goBackToActivity("StartActivity");
    }

    public void test_OnItemClick() {
        solo.clickOnButton(solo.getString(R.string.hello_world));
        solo.clickOnScreen(300, 300);
        solo.sleep(3000);
        solo.clickOnScreen(100, 100);
        solo.sleep(3000);
        solo.clickOnScreen(400, 400);
        solo.sleep(3000);
        solo.clickOnScreen(200, 200);
        solo.sleep(3000);
        solo.goBackToActivity("StartActivity");
    }
}
