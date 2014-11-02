// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ElementsUtil;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager that handles sending and receiving messages
 * between Activities of the same application.
 *
 */
@DesignerComponent(version = YaVersion.APPLICATION_MESSENGER_VERSION,
    category = ComponentCategory.SENSORS,
    description = "This component allows users to send messages within " +
    "and between applications. Users can listen for messages from the Android "+
    "system. They can also define custom messages to send and receive. There are " +
    "two types of messages a user can send or receive: a local message and a " +
    "global message. Local messages can only be sent and received by different " +
    "parts of the application the user is creating. Global messages can be sent " +
    "to other applications and the user's application can listen to global " +
    "messages from other applications. The user can listen to a maximum of 5 " +
    "local messages and 5 global messages.",
    nonVisible=true,
    iconName="images/applicationMessenger.png")
@SimpleObject
@UsesLibraries(libraries = "android-support-v4.jar")
public final class ApplicationMessenger extends AndroidNonVisibleTaskComponent
    implements OnPauseListener, OnResumeListener, OnDestroyListener {

  private final ComponentContainer container;
  private final String TAG = "ApplicationMessenger";
  private final String DATA_EXTRA = "DATA";

  String[] localMessages = {};
  List<BroadcastReceiver> localReceivers = new ArrayList<BroadcastReceiver>();

  /**
   * Creates a new ApplicationMessenger component.
   *
   * @param container container, component will be placed in
   */
  public ApplicationMessenger(ComponentContainer container) {
    super(container);
    this.container = container;
    if (form != null) {
      form.registerForOnResume(this);
      form.registerForOnPause(this);
    } else {
      task.registerForOnDestroy(this);
    }
    Log.i(TAG, "Registered receiver");
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(description = "A comma-separated list of the local messages this screen " +
    "should listen for. A maximum of five messages can be specified.", 
    category = PropertyCategory.BEHAVIOR)
  public void LocalMessagesToReceive(String itemstring){
    localMessages = itemstring.split(",", 5);

    // create receivers for local messages
    localReceivers.clear();
    for (int i=0; i<localMessages.length; i++) {
      final int index = i;
      BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          Log.i(TAG, "Message received " + localMessages[index]);
          String message = intent.getStringExtra(DATA_EXTRA);
          Log.i(TAG, "Message data " + message);
          switch(Integer.valueOf(index)) {
            case 0: LocalMessageReceived1(message);
                    break;
            case 1: LocalMessageReceived2(message);
                    break;            
            case 2: LocalMessageReceived3(message);
                    break;            
            case 3: LocalMessageReceived4(message);
                    break;            
            case 4: LocalMessageReceived5(message);
                    break;            
            default:
          }
        }     
      };
      localReceivers.add(receiver);
    }
    registerLocalReceivers();    
  }

  @SimpleFunction(description = "Sends a message with the specified name, messageName. " +
    "to other parts of the current application.")
  public void SendLocalMessage(String messageName, String message) {
    Intent intent = new Intent(messageName);
    intent.putExtra(DATA_EXTRA, message);
    LocalBroadcastManager.getInstance(this.container.$context()).sendBroadcast(intent);
    Log.i(TAG, "Sent local message");
  }

  @SimpleEvent(description = "This event is run when a local message of the first " +
    " specified type has been received.")
  public void LocalMessageReceived1(String message) {
    Log.i(TAG, "Event called: LocalMessageReceived1");
    EventDispatcher.dispatchEvent(this, "LocalMessageReceived1", message);
  }

  @SimpleEvent(description = "This event is run when a local message of the second " +
    " specified type has been received.")
  public void LocalMessageReceived2(String message) {
    Log.i(TAG, "Event called: LocalMessageReceived2");
    EventDispatcher.dispatchEvent(this, "LocalMessageReceived2", message);
  }

  @SimpleEvent(description = "This event is run when a local message of the third " +
    " specified type has been received.")
  public void LocalMessageReceived3(String message) {
    Log.i(TAG, "Event called: LocalMessageReceived3");
    EventDispatcher.dispatchEvent(this, "LocalMessageReceived3", message);
  }

  @SimpleEvent(description = "This event is run when a local message of the fourth " +
    " specified type has been received.")
  public void LocalMessageReceived4(String message) {
    Log.i(TAG, "Event called: LocalMessageReceived4");
    EventDispatcher.dispatchEvent(this, "LocalMessageReceived4", message);
  }

  @SimpleEvent(description = "This event is run when a local message of the fifth " +
    " specified type has been received.")
  public void LocalMessageReceived5(String message) {
    Log.i(TAG, "Event called: LocalMessageReceived5");
    EventDispatcher.dispatchEvent(this, "LocalMessageReceived5", message);
  }

  public void registerLocalReceivers() {
    unregisterLocalReceivers();
    for (int i=0; i<localMessages.length; i++) {
      LocalBroadcastManager.getInstance(this.container.$context()).registerReceiver(localReceivers.get(i), new IntentFilter(localMessages[i]));
    }
  }

  public void unregisterLocalReceivers() {
    for (BroadcastReceiver receiver : localReceivers) {
      LocalBroadcastManager.getInstance(this.container.$context()).unregisterReceiver(receiver);
    }
  }

  // OnResumeListener implementation
  @Override
  public void onResume() {
    registerLocalReceivers();
    Log.i(TAG, "Registered receiver");
  }

  // OnPauseListener implementation
  @Override
  public void onPause() {
    unregisterLocalReceivers();
  }

  // OnDestroyListener implementation
  @Override
  public void onDestroy() {
    unregisterLocalReceivers();
  }
}