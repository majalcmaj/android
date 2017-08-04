package com.example.mc.authenticationtestapplication;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mc.authenticationtestapplication.remote.ServerConnectorService;

public class LoginActivity extends AccountAuthenticatorActivity implements ServerConnectorService.Receiver {

    public static final String ARG_ACCOUNT_TYPE = "com.example.mc.ACCOUNT_TPYE";
    public static final String ARG_AUTH_TYPE = "com.example.mc.AUTH_TYPE";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "com.example.mc.IS_ADDING_NEW_ACCOUNT";
    public static final String ACCOUNT_TYPE = "com.example.mc.TECH_ACCOUNT";
    public static final String READ_ONLY_TOKEN_TYPE = "com.example.mc.READ_ONLY_TOKEN";

    private TextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private ServerConnectorService.Connector mConnnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUsernameView = (TextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mConnnector = ServerConnectorService.getConnector(this);
        mConnnector.setAsReceiver(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mConnnector.unbind();

    }

    @Override
    public void onReceiveResult(final int resultCode, final Bundle resultData) {
        if (resultCode == RESULT_OK) {
            finishLogin(resultData);
        } else {
            showProgress(false);
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
    }


    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            mConnnector.authenticateUser(username, password);
        }
    }

    // TODO: Can be made more abstract?
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

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

    }

    private void finishLogin(Bundle data) {
        String token = data.getString(ServerConnectorService.VAR_TOKEN);
        String username = data.getString(ServerConnectorService.VAR_USERNAME);
        String password = data.getString(ServerConnectorService.VAR_PASSWORD);

        final Account account = new Account(username, ACCOUNT_TYPE);
        final AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
        Account foundAccount = null;
        for (Account acc : accounts) {
            if (username.equals(acc.name)) {
                foundAccount = acc;
            }
        }

        if (foundAccount == null) {
            String authtokenType = READ_ONLY_TOKEN_TYPE;
            am.addAccountExplicitly(account, password, null);
            am.setAuthToken(account, authtokenType, token);
        } else {
            am.setPassword(foundAccount, password);
        }
        setAccountAuthenticatorResult(data);

        setResult(RESULT_OK);
        finish();
    }
}


