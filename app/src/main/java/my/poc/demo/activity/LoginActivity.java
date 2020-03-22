package my.poc.demo.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.huamai.poc.IPocEngineEventHandler;
import com.huamai.poc.PocEngineFactory;

import my.poc.demo.R;


public class LoginActivity extends AppCompatActivity {
    private EditText mAccountView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSharedPreferences = getSharedPreferences("poc-demo", Context.MODE_PRIVATE);

        mAccountView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Button signInButton = (Button) findViewById(R.id.email_sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mAccountView.setText(mSharedPreferences.getString("account", ""));
        mPasswordView.setText(mSharedPreferences.getString("password", ""));

        if (PocEngineFactory.get().hasServiceConnected()) {
            LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            LoginActivity.this.finish();
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //根据唯一码获取账号信息
        findViewById(R.id.get_account_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress(true);
                PocEngineFactory.get().addEventHandler(iPocEngineEventHandler);
                PocEngineFactory.get().login("");
            }
        });

        //自动登录
        boolean isAutoLogin = mSharedPreferences.getBoolean("autoLogin", false);
        if (isAutoLogin) {
            signInButton.performClick();
        }
    }

    private void attemptLogin() {
        // Reset errors.
        mAccountView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mAccountView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mAccountView.setError(getString(R.string.error_field_required));
            focusView = mAccountView;
            cancel = true;
        } else if (!isNumberValid(email)) {
            mAccountView.setError(getString(R.string.error_invalid_email));
            focusView = mAccountView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            //登录
            PocEngineFactory.get().addEventHandler(iPocEngineEventHandler);
            PocEngineFactory.get().login(email, password);

            mSharedPreferences.edit().putString("account", email).commit();
            mSharedPreferences.edit().putString("password", password).commit();
        }
    }

    //登录时相关的回调，该过程可能需要几秒，可根据不同状态显示相应的UI
    IPocEngineEventHandler iPocEngineEventHandler = new IPocEngineEventHandler() {

        @Override
        public void onLoginStepProgress(int progress, String msg) {
            if (progress == LoginProgress.PRO_LOGIN_SUCCESS) {
                PocEngineFactory.get().removeEventHandler(iPocEngineEventHandler);
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                LoginActivity.this.finish();
            } else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_FAILED) {
                Toast.makeText(LoginActivity.this, "Login failed " + msg, Toast.LENGTH_SHORT).show();
            } else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_NOT_EXIST) {
                Toast.makeText(LoginActivity.this, "Login failed " + msg, Toast.LENGTH_SHORT).show();
            } else if (progress == LoginProgress.PRO_BINDING_ACCOUNT_NOT_ACTIVE) {
                Toast.makeText(LoginActivity.this, "Login failed " + msg, Toast.LENGTH_SHORT).show();
            } else if (progress == LoginProgress.PRO_LOGIN_FAILED) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, "Login failed " + msg, Toast.LENGTH_SHORT).show();
            }

            ((TextView) findViewById(R.id.tips)).setText(msg);
        }
    };

    private boolean isNumberValid(String input) {
        try {
            Long.valueOf(input);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }

        findViewById(R.id.tips).setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

