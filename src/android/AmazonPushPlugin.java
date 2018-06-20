package com.kanoapps.cordova.amazonpush;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

import com.amazon.device.messaging.ADM;

/**
 * Cordova plugin that allows for an arbitrarly sized and positioned WebView to be shown ontop of the canvas
 */
public class AmazonPushPlugin extends CordovaPlugin {

    private static final String TAG = "AmazonPushPlugin";
    private static final String PREFS_NAME = "AmazonPushPlugin";

    private static CallbackContext responseCallback;

    private static CordovaWebView webView;

    /**
     * Initializes the plugin
     *
     * @param cordova The context of the main Activity.
     * @param webView The associated CordovaWebView.
     */
    @Override
    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {

        AmazonPushPlugin.webView = super.webView;
    }

    /**
     * Executes the request and returns PluginResult
     *
     * @param action
     * @param args
     * @param callbackContext
     * @return boolean
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Context context = this.cordova.getActivity().getApplicationContext();

        responseCallback = callbackContext;

        if (action.equals("register")) {

            try {

                final ADM adm = new ADM(context);
                if (adm.isSupported()) {
                    String token = adm.getRegistrationId();

                    if (token == null) {
                        adm.startRegister();
                    } else {
                        callbackContext.success(token);
                    }
                }
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }

            return true;
        }

        // Default response to say the action hasn't been handled
        return false;
    }

    protected static void setRegistrationID(String token) {

        responseCallback.success(token);
    }

    protected static void setReceivedMessage(String msg) {

        //String js = "AmazonPushPlugin.notification(" + msg + ")";

        //sendJavascript(js);
    }

    protected static void sendJavascript(final String js) {

        Runnable jsLoader = new Runnable() {
            public void run() {
                webView.loadUrl("javascript:" + js);
            }
        };
        try {
            Method post = webView.getClass().getMethod("post", Runnable.class);
            post.invoke(webView, jsLoader);
        } catch (Exception e) {

            Log.e(TAG, "sendJavascript error " + e.getMessage());
        }
    }
}
