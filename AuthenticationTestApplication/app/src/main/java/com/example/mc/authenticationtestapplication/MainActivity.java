package com.example.mc.authenticationtestapplication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AccountManager am = AccountManager.get(this);
        Account[] accounts =am.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
        if(accounts.length == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
            am.addAccount(LoginActivity.ACCOUNT_TYPE, LoginActivity.READ_ONLY_TOKEN_TYPE, null, null, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle bnd = future.getResult();
                        Toast.makeText(MainActivity.this, "Bundle: " + bnd.toString(), Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        }else {
            getExistingAccountAuthToken(accounts[0], LoginActivity.READ_ONLY_TOKEN_TYPE);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                AccountManager am = AccountManager.get(this);
                Account[] accounts =  am.getAccountsByType(LoginActivity.ACCOUNT_TYPE);
                getExistingAccountAuthToken(accounts[0], LoginActivity.READ_ONLY_TOKEN_TYPE);
            }else {
                finish();
            }
        }
    }

    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        AccountManager am = AccountManager.get(this);
        final AccountManagerFuture<Bundle> future = am.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    Toast.makeText(MainActivity.this, ((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
