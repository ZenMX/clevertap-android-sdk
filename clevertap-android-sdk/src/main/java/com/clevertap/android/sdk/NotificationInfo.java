package com.clevertap.android.sdk;

import android.os.Bundle;

/**
 * Contains information regarding the notification payload in the GCM intent.
 * <p/>
 * Use {@link CleverTapAPI#getNotificationInfo(Bundle)} to retrieve this information.
 */
@SuppressWarnings("WeakerAccess")
public final class NotificationInfo {
    /**
     * Whether or not this notification was sent via CleverTap.
     */
    public final boolean fromCleverTap;

    /**
     * Whether to parse this notifcation payload or not.
     * <p/>
     * True if and only if this notification is from CleverTap, and it should be parsed for information.
     */
    private final boolean shouldRender;

    NotificationInfo(boolean fromCleverTap, boolean shouldRender) {
        this.fromCleverTap = fromCleverTap;
        this.shouldRender = shouldRender;
    }

    @Override
    public String toString() {
        return "NotificationInfo{" +
                "fromCleverTap=" + fromCleverTap +
                ", shouldRender=" + shouldRender +
                '}';
    }
}
