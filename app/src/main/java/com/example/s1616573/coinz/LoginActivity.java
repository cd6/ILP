package com.example.s1616573.coinz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final String USER_COLLECTION = "users";
    private final String USER_PRIVATE = "user";
    private final String USER_DOCUMENT = "userDoc";

    // UI references.
    private AutoCompleteTextView emailView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;
    private TextView loginError;

    private String tag = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        emailView = findViewById(R.id.email);

        // enter key to log in
        passwordView = findViewById(R.id.password);
        passwordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        // pressing sign in button attempts login
        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(view -> {
            loginError.setVisibility(View.INVISIBLE);
            attemptLogin();
        });

        // pressing create account button attempts to create a new account
        Button mCreateAccountButton = findViewById(R.id.create_account_button);
        mCreateAccountButton.setOnClickListener(view -> {
            loginError.setVisibility(View.INVISIBLE);
            attemptCreateAccount();
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
        loginError = findViewById(R.id.text_login_error);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            openMainActivity();
            this.finish();
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        // check that email and password are valid before continuing
        boolean continueLogin = checkCredentials(email, password);

        if (continueLogin) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with user info
                    Log.d(tag, "[attemptLogin] success");
                    openMainActivity();
                    this.finish();
                } else {
                    // Sign in failed, display a message to the user
                    Log.d(tag, "[attemptLogin] failure");
                    showProgress(false);
                    loginError.setText("The email or password is incorrect");
                    loginError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void attemptCreateAccount() {
        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the create attempt.
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        // check that email and password are valid before continuing
        boolean continueCreation = checkCredentials(email, password);

        if (continueCreation) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Create account success, update UI with user info
                    Log.d(tag, "[attemptCreation] success");
                    // Add collection for user on Firestore to store their progress
                    String userID = mAuth.getUid();
                    DocumentReference docRef = db.collection(USER_COLLECTION).document(Objects.requireNonNull(userID)).collection(USER_PRIVATE).document(USER_DOCUMENT);
                    Map<String, Object> m = new HashMap<>();
                    docRef.set(m)
                            .addOnSuccessListener(aVoid -> Log.d(tag, "DocumentSnapshot successfully written!"))
                            .addOnFailureListener(e -> Log.w(tag, "Error writing document", e));
                    openMainActivity();
                    this.finish();
                } else {
                    // Create account failed, display a message to the user
                    Log.d(tag, "[attemptCreation] failure");
                    showProgress(false);
                    loginError.setText("Account creation failed");
                    loginError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private boolean checkCredentials(String email, String password) {
        View focusView = null;
        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            // carry on with login/creation
            return true;
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    private void openMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}




