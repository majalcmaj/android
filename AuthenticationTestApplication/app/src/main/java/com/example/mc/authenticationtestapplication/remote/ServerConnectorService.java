package com.example.mc.authenticationtestapplication.remote;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.JsonReader;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ServerConnectorService extends Service {
    public static final int MSG_AUTHENTICATE = 1;
    public static final String VAR_TOKEN = "tokenString";
    public static final String VAR_USERNAME = "usernameString";
    public static final String VAR_PASSWORD = "passwordString";

    private static final String RESULT_RECEIVER_EXTRA = "resultReceiver";
    private ResultReceiver resultReceiver;
    public ServerConnectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER_EXTRA);
        return mMessenger.getBinder();
    }


    private class IncomingHandler extends Handler {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTHENTICATE:
                    Pair<String, String> usernamePassword = (Pair<String, String>)msg.obj;
                    final String username = usernamePassword.first;
                    final String password = usernamePassword.second;

                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected void onPostExecute(String token) {
                            if(token != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString(VAR_TOKEN, token);
                                bundle.putString(VAR_USERNAME, username);
                                bundle.putString(VAR_PASSWORD, password);
                                resultReceiver.send(Activity.RESULT_OK, bundle);
                            }else {
                                resultReceiver.send(Activity.RESULT_CANCELED, Bundle.EMPTY);
                            }
                        }

                        @Override
                        protected String doInBackground(Void... voids) {
                            String token = userSignIn(username, password);
                            return token;
                        }
                    }.execute((Void)null);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    public static Connector getConnector(Context context) {
        return new Connector(context);
    }

    public static class Connector implements ServiceConnection {
        private boolean mBound;
        private Messenger mService;
        private Context context;
        ConnectorResultReceiver resultReceiver;
        private Connector(Context context) {
            this.context = context;
            Intent serviceIntent = new Intent(context, ServerConnectorService.class);
            resultReceiver = new ConnectorResultReceiver(new Handler());
            serviceIntent.putExtra(RESULT_RECEIVER_EXTRA, resultReceiver);
            context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }

        public boolean isBound() {
            return mBound;
        }

        public void authenticateUser(String username, String password) {
            if (!mBound) return;

            Pair<String, String> usernamePassword = new Pair<>(username, password);

            Message msg = Message.obtain(null, ServerConnectorService.MSG_AUTHENTICATE, usernamePassword);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
            return;
        }

        public void unbind() {
            context.unbindService(this);
        }

        public void setAsReceiver(Receiver receiver) {
            resultReceiver.setReceiver(receiver);
        }
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }
    public static class ConnectorResultReceiver extends ResultReceiver {

        private ConnectorResultReceiver(Handler handler) {
            super(handler);
        }

        private Receiver mReceiver;
        public void setReceiver(Receiver receiver) {
            mReceiver = receiver;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(mReceiver != null) {
                mReceiver.onReceiveResult(resultCode, resultData);
            }
        }
    }

    private String userSignIn(String username, String password) {
        if(username.equals("admin") && password.equals("admin")) {
            return "1234567890";
        }else return null;
    }

}
