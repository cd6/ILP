package com.example.s1616573.coinz;


import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@SuppressWarnings("ALL")
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class, false, true);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Before
    public void setUp() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
        mActivityTestRule.launchActivity(new Intent());
    }

    @Test
    // testing loggin in existing user
    public void loginActivityTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatAutoCompleteTextView = onView(
                allOf(withId(R.id.email)));
        appCompatAutoCompleteTextView.perform(scrollTo(), replaceText("a@g.c"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatAutoCompleteTextView2 = onView(
                allOf(withId(R.id.email)));
        appCompatAutoCompleteTextView2.perform(scrollTo(), replaceText("a@g.com"));

        ViewInteraction appCompatAutoCompleteTextView3 = onView(
                allOf(withId(R.id.email)));
        appCompatAutoCompleteTextView3.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.password)));
        appCompatEditText.perform(scrollTo(), replaceText("aaaaaa"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.password)));
        appCompatEditText2.perform(scrollTo(), click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.password)));
        appCompatEditText3.perform(scrollTo(), click());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.password)));
        appCompatEditText4.perform(scrollTo(), replaceText("aaaaaaaaaa"));

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.password)));
        appCompatEditText5.perform(closeSoftKeyboard());

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.email), withText("a@g.com")));
        editText2.check(matches(withText("a@g.com")));

        ViewInteraction editText3 = onView(
                allOf(withId(R.id.password), withText("••••••••••")));
        editText3.check(matches(withText("••••••••••")));

        ViewInteraction button = onView(
                allOf(withId(R.id.email_sign_in_button)));
        button.check(matches(isDisplayed()));

        ViewInteraction switch_2 = onView(
                allOf(withId(R.id.switch_create)));
        switch_2.check(matches(isDisplayed()));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.email_sign_in_button), withText("Sign in")));
        appCompatButton.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withText("Map")));
        textView.check(matches(withText("Map")));

        ViewInteraction viewGroup = onView(
                allOf(withId(R.id.include)));
        viewGroup.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.exchange_rate_item)));
        textView2.check(matches(isDisplayed()));

        ViewInteraction imageView = onView(
                allOf(withContentDescription("More options")));
        imageView.check(matches(isDisplayed()));

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.fab_bank)));
        imageButton.check(matches(isDisplayed()));

        ViewInteraction imageButton2 = onView(
                allOf(withId(R.id.fab_wallet)));
        imageButton2.check(matches(isDisplayed()));
    }
}
