package com.clevertap.android.sdk.pushnotification;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.clevertap.android.sdk.Constants;
import com.clevertap.android.sdk.Logger;
import com.clevertap.android.sdk.Utils;

import static com.clevertap.android.sdk.pushnotification.CTPushNotificationReceiver.DEEPLINK_ACTIVITY;

public class CTNotificationIntentService extends IntentService {

    public final static String MAIN_ACTION = "com.clevertap.PUSH_EVENT";

    public final static String TYPE_BUTTON_CLICK = "com.clevertap.ACTION_BUTTON_CLICK";

    public CTNotificationIntentService() {
        super("CTNotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        String type = extras.getString("ct_type");
        if (TYPE_BUTTON_CLICK.equals(type)) {
            Logger.v("CTNotificationIntentService handling " + TYPE_BUTTON_CLICK);
            handleActionButtonClick(extras);
        } else {
            Logger.v("CTNotificationIntentService: unhandled intent " + intent.getAction());
        }
    }

    private void handleActionButtonClick(Bundle extras) {
        try {
            boolean autoCancel = extras.getBoolean("autoCancel", false);
            int notificationId = extras.getInt("notificationId", -1);
            String dl = extras.getString("dl");

            Context context = getApplicationContext();
            Intent launchIntent = null;
            if (dl != null) {
                try {
                    Class<?> webLinkActivity = CTPushNotificationReceiver.getExtraDeepLinkClz();
                    try {
                        if (webLinkActivity == null) {
                            webLinkActivity = Class.forName(DEEPLINK_ACTIVITY);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (webLinkActivity != null) {
                        launchIntent = new Intent(context, webLinkActivity);
                        launchIntent.setData(Uri.parse(dl));
                        Utils.setPackageNameFromResolveInfoList(context, launchIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (launchIntent == null) {
                launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            }

            if (launchIntent == null) {
                Logger.v("CTNotificationService: create launch intent.");
                return;
            }

            launchIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            launchIntent.putExtra(CTPushNotificationReceiver.FROM_CLEVERTAP, CTPushNotificationReceiver.CLEVERTAP_NOTIFICATION_CLICKED);
            launchIntent.putExtras(extras);
            launchIntent.removeExtra("dl");

            if (autoCancel && notificationId > -1) {
                NotificationManager notificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(notificationId);
                }

            }
            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)); // close the notification drawer
            startActivity(launchIntent);
        } catch (Throwable t) {
            Logger.v("CTNotificationService: unable to process action button click:  " + t.getLocalizedMessage());
        }
    }
}
