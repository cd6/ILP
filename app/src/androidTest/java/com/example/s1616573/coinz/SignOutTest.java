package com.example.s1616573.coinz;


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

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SignOutTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    private FirebaseAuth mAuth;

    @Before
    public void setup() {
        mAuth = FirebaseAuth.getInstance();
    }

    @Test
    public void signOutTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // sign in if no user is signed in yet, specific user is not important
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            ViewInteraction appCompatAutoCompleteTextView = onView(
                    (withId(R.id.email)));
            appCompatAutoCompleteTextView.perform(scrollTo(), replaceText("a@g.com"), closeSoftKeyboard());

            ViewInteraction appCompatEditText = onView(
                    (withId(R.id.password)));
            appCompatEditText.perform(scrollTo(), replaceText("aaaaaaaaaa"), closeSoftKeyboard());

            ViewInteraction appCompatButton = onView(
                    (withId(R.id.email_sign_in_button)));
            appCompatButton.perform(scrollTo(), click());
        }

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

        ViewInteraction imageView = onView(
                (withContentDescription("More options")));
        imageView.check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction textView = onView(
                allOf(withId(R.id.title), withText("Sign out")));
        textView.check(matches(withText("Sign out")));

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("Sign out")));
        appCompatTextView.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(android.R.id.message), withText("Are you sure you want to sign out?")));
        textView2.check(matches(withText("Are you sure you want to sign out?")));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(android.R.id.button2), withText("No")));
        appCompatButton2.perform(scrollTo(), click());

        ViewInteraction imageView2 = onView(
                (withContentDescription("More options")));
        imageView2.check(matches(isDisplayed()));

        ViewInteraction imageView3 = onView(
                (withContentDescription("More options")));
        imageView3.check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.title), withText("Sign out")));
        textView3.check(matches(withText("Sign out")));

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.title), withText("Sign out")));
        appCompatTextView2.perform(click());

        ViewInteraction button = onView(
                (withId(android.R.id.button1)));
        button.check(matches(isDisplayed()));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("Yes")));
        appCompatButton3.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction scrollView = onView(
                (withId(R.id.login_form)));
        scrollView.check(matches(isDisplayed()));
    }
}
