// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;


import android.content.Context;
import android.util.Log;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.app.PendingIntent;

/**
 *
 * Non-visible notifications component that will allow users to launch, delete and modify notifications.
 *
 */
@DesignerComponent(version = YaVersion.NOTIFICATION_COMPONENT_VERSION,
    description = "Non-visible component providing notification capabilities." +
    "Users can launch, update, and remove notifications",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/notification.png")
@SimpleObject
@UsesLibraries(libraries = "android-support-v4.jar")
public class NotificationComponent extends AndroidNonVisibleTaskComponent {

  private static final String ARGUMENT_NAME = "APP_INVENTOR_START";

  private NotificationManager mNotificationManager;
  private Context context;

  public NotificationComponent(ComponentContainer container) {
    super(container);
    context = container.$context();
    mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @SimpleFunction(description="Launches a notification with a spefic id, title, and text message.")
  public void LaunchNotification(int notificationId, String title, String text) {
    NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(title)
                .setContentText(text);

    Intent resultIntent = new Intent();
    PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);

    mNotificationManager.notify(notificationId, mBuilder.build());
  }

  @SimpleFunction(description="Launches a notification with a spefic id, title, and text message.")
  public void LaunchNotificationWithRedirect(int notificationId, String title, String text, String screenName, String value) {
    NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(title)
                .setContentText(text);

    String packageName = context.getPackageName();
    Intent resultIntent = new Intent();
    resultIntent.putExtra(ARGUMENT_NAME, value);
    resultIntent.setClassName(context, packageName + "." + screenName);
    PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(resultPendingIntent);

    mNotificationManager.notify(notificationId, mBuilder.build());
  }


  @SimpleFunction(description="Cancels the notification with the specified id.")
  public void CancelNotification(int notificationId) {
    mNotificationManager.cancel(notificationId);
  }

  @SimpleFunction(description="Cancels all notifications launched by this component.")
  public void CancelAllNotifications() {
    mNotificationManager.cancelAll();
  }
}
