package com.example.s1616573.coinz;


import android.support.design.widget.TextInputLayout;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
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
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("ALL")
@LargeTest
@RunWith(AndroidJUnit4.class)
public class CreateAccountTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void createAccountTest() {
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
        appCompatAutoCompleteTextView.perform(scrollTo(), replaceText("a"), closeSoftKeyboard());

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
        appCompatAutoCompleteTextView2.perform(scrollTo(), replaceText(""));

        ViewInteraction appCompatAutoCompleteTextView3 = onView(
                allOf(withId(R.id.email)));
        appCompatAutoCompleteTextView3.perform(closeSoftKeyboard());

        ViewInteraction switch_ = onView(
                allOf(withId(R.id.switch_create)));
        switch_.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.username)));
        appCompatEditText.perform(scrollTo(), replaceText(""), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.username)));
        appCompatEditText2.perform(scrollTo(), click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.username)));
        appCompatEditText3.perform(scrollTo(), replaceText("ANEWUSER"), closeSoftKeyboard());

        ViewInteraction appCompatAutoCompleteTextView4 = onView(
                allOf(withId(R.id.email)));
        appCompatAutoCompleteTextView4.perform(scrollTo(), replaceText("newuser@coinz.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.password)));
        appCompatEditText4.perform(scrollTo(), replaceText("securepassword"), closeSoftKeyboard());

        ViewInteraction editText = onView(
                allOf(withId(R.id.username), withText("ANEWUSER")));
        editText.check(matches(withText("ANEWUSER")));

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.email), withText("newuser@coinz.com")));
        editText2.check(matches(withText("newuser@coinz.com")));

        ViewInteraction editText3 = onView(
                allOf(withId(R.id.password), withText("••••••••••••••")));
        editText3.check(matches(withText("••••••••••••••")));

        ViewInteraction button = onView(
                allOf(withId(R.id.create_account_button)));
        button.check(matches(isDisplayed()));

        ViewInteraction switch_2 = onView(
                allOf(withId(R.id.switch_create)));
        switch_2.check(matches(isDisplayed()));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.create_account_button), withText("Create account")));
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

        ViewInteraction viewGroup = onView(
                allOf(withId(R.id.include)));
        viewGroup.check(matches(isDisplayed()));
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
