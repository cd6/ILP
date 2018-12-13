package com.example.s1616573.coinz;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PickUpTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class, false, true);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    private FirebaseAuth mAuth;
    private MainFirestore mainFirestore;

    @Before
    public void setup() {
        // make sure we're in the right account
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("a@g.com", "aaaaaaaaaa");

        String userID = "M9cFGeZTvsdTGVo9XGy9WyFn3iT2";
        mainFirestore = new MainFirestore(userID);

        mainFirestore.emptyWallet();
        mainFirestore.resetPickedUpCoins();
    }

    @Test
    // Test calling pickUpCoin method then check wallet to see if coin is there
    public void pickUpTest() {
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Coin c = new Coin("c8d1-b314-cf47-8111-f01f-e0a8", 9.566871611013338, "PENY");
        mainFirestore.pickUp(c);

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

        ViewInteraction imageButton = onView(
                (withId(R.id.fab_wallet)));
        imageButton.check(matches(isDisplayed()));

        ViewInteraction floatingActionButton = onView(
                (withId(R.id.fab_wallet)));
        floatingActionButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*ViewInteraction relativeLayout = onView(
                allOf(childAtPosition(
                        (withId(R.id.rv_coins)),
                        0),
                        isDisplayed()));
        relativeLayout.check(matches(isDisplayed())); */

        ViewInteraction textView = onView(
                allOf(withId(R.id.text_currency), withText("Currency: PENY"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rv_coins),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Currency: PENY")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.text_value), withText("Value: 9.566871611013338"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rv_coins),
                                        0),
                                1),
                        isDisplayed()));
        textView2.check(matches(withText("Value: 9.566871611013338")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.text_gold_value),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.rv_coins),
                                        0),
                                2),
                        isDisplayed()));
        textView3.check(matches(isDisplayed()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
