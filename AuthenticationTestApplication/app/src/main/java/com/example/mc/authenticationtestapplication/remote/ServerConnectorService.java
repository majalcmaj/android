package com.example.mc.authenticationtestapplication.remote;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class ServerConnectorService extends Service {
    public static final int MSG_AUTHENTICATE = 1;

    public ServerConnectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTHENTICATE:
                    Toast.makeText(getApplicationContext(), "Authenticating...", Toast.LENGTH_SHORT).show();
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
        private Connector(Context context) {
            this.context = context;
            context.bindService(new Intent(context, ServerConnectorService.class), this, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }

        public boolean isBound() {
            return mBound;
        }

        public String authenticateUser(String username, String password) {
            if (!mBound) return "Failure";
            Message msg = Message.obtain(null, ServerConnectorService.MSG_AUTHENTICATE, username);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                return "failure";
            }
            return "Success";
        }

        public void unbind() {
            context.unbindService(this);
        }
    }
}
