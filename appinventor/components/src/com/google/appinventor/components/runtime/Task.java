 // -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

// ***********************************************
// If we're not going to go this route with onDestroy, then at least get rid of the DEBUG flag.

package com.google.appinventor.components.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import android.app.Service;
import android.app.Dialog;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.collect.Sets;
import com.google.appinventor.components.runtime.util.AlignmentUtil;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FullScreenVideoUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.ViewUtil;

/**
 * Component underlying services, not directly accessible to Simple programmers.
 *
 * <p>This is the root container of any Android Service.
 *
 *
 */
@DesignerComponent(version = YaVersion.FORM_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT,
    description = "Top-level component containing all other components in the program",
    showOnPalette = false)
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_WIFI_STATE,android.permission.ACCESS_NETWORK_STATE")
public class Task extends Service
    implements Component, ComponentContainer, HandlesEventDispatching {
  private static final String LOG_TAG = "Task";

  private static Task activeTask = null;

  private String taskName;

  // Application lifecycle related fields
  private final Set<OnDestroyListener> onDestroyListeners = Sets.newHashSet();

  // AppInventor lifecycle: listeners for the Initialize Event
  private final Set<OnInitializeListener> onInitializeListeners = Sets.newHashSet();

  private boolean taskInitialized = false;

  @Override
  public void onCreate() {
    super.onCreate();

    activeTask = this;

    String className = getClass().getName();
    int lastDot = className.lastIndexOf('.');
    taskName = className.substring(lastDot + 1);
    Log.d(LOG_TAG, "Task " + taskName + " got onCreate");

    $define();

    Initialize();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  /**
   * An app can register to be notified when App Inventor's Initialize
   * block has fired.  They will be called in Initialize().
   *
   * @param component
   */
  public void registerForOnInitialize(OnInitializeListener component) {
    onInitializeListeners.add(component);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    String startValue = intent.getStringExtra(Form.SERVICE_ARG);
    Object decodedStartVal = Form.decodeJSONStringForForm(startValue, "get start value");
    TaskStarted(decodedStartVal);
    return START_STICKY;
  }

  @SimpleEvent(description = "Task has been started")
  public void TaskStarted(Object startValue) {
    EventDispatcher.dispatchEvent(this, "TaskStarted", startValue);
  }

  /**
   * ErrorOccurred event handler.
   */
  @SimpleEvent(
      description = "Event raised when an error occurs. Only some errors will " +
      "raise this condition.  For those errors, the system will show a notification " +
      "by default.  You can use this event handler to prescribe an error " +
      "behavior different than the default.")
  public void ErrorOccurred(Component component, String functionName, int errorNumber,
      String message) {
    String componentType = component.getClass().getName();
    componentType = componentType.substring(componentType.lastIndexOf(".") + 1);
    Log.e(LOG_TAG, "Task " + taskName + " ErrorOccurred, errorNumber = " + errorNumber +
        ", componentType = " + componentType + ", functionName = " + functionName +
        ", messages = " + message);
    if (!EventDispatcher.dispatchEvent(
        this, "ErrorOccurred", component, functionName, errorNumber, message)
      && taskInitialized) {
      new Notifier(this).LogError(message);
    }
  }


  public void dispatchErrorOccurredEvent(final Component component, final String functionName,
      final int errorNumber, final Object... messageArgs) {
    Log.i(LOG_TAG, "TASK dispatchErrorOccurredEvent");
    String message = ErrorMessages.formatMessage(errorNumber, messageArgs);
    ErrorOccurred(component, functionName, errorNumber, message);
  }

  @Override
  public void onDestroy() {
    Log.i(LOG_TAG, "Task " + taskName + " got onDestroy");
    for (OnDestroyListener onDestroyListener : onDestroyListeners) {
      onDestroyListener.onDestroy();
    }
    super.onDestroy();
    // for debugging and future growth
    Log.i(LOG_TAG, "Task " + taskName + " got onDestroy");

    // Unregister events for components in this form.
    EventDispatcher.removeDispatchDelegate(this);
  }

  public void registerForOnDestroy(OnDestroyListener component) {
    onDestroyListeners.add(component);
  }

  /**
   * Compiler-generated method to initialize and add application components to
   * the form.  We just provide an implementation here to artificially make
   * this class concrete so that it is included in the documentation and
   * Codeblocks language definition file generated by
   * {@link com.google.appinventor.components.scripts.DocumentationGenerator} and
   * {@link com.google.appinventor.components.scripts.LangDefXmlGenerator},
   * respectively.  The actual implementation appears in {@code runtime.scm}.
   */
  protected void $define() {    // This must be declared protected because we are called from Screen1 which subclasses
                                // us and isn't in our package.
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canDispatchEvent(Component component, String eventName) {
    // Events can only be dispatched after the screen initialized event has completed.
    boolean canDispatch = taskInitialized || (component == this && eventName.equals("Initialize"));
    Log.e(LOG_TAG, "canDispatch " + canDispatch);
    return canDispatch;
  }

  /**
   * A trivial implementation to artificially make this class concrete so
   * that it is included in the documentation and
   * Codeblocks language definition file generated by
   * {@link com.google.appinventor.components.scripts.DocumentationGenerator} and
   * {@link com.google.appinventor.components.scripts.LangDefXmlGenerator},
   * respectively.  The actual implementation appears in {@code runtime.scm}.
   */
  @Override
  public boolean dispatchEvent(Component component, String componentName, String eventName,
      Object[] args) {
    throw new UnsupportedOperationException();
  }

  /**
  * Initialize event handler.
  */
  @SimpleEvent(description = "Service starting")
  public void Initialize() {
    EventDispatcher.dispatchEvent(Task.this, "Initialize");
    taskInitialized = true;

    //  Call all apps registered to be notified when Initialize Event is dispatched
    for (OnInitializeListener onInitializeListener : onInitializeListeners) {
      onInitializeListener.onInitialize();
    }
  }

  // Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return this;
  }

  // ComponentContainer implementation

  @Override
  public Context $context() {
    return this;
  }

  @Override
  public Form $form() {
    return null;
  }

  @Override
  public Task $task() {
    return this;
  }

  // We don' need these but ComponentContainer implements
  // these so, we'll see what to do

  @Override
  public void $add(AndroidViewComponent component) {
  }

  @Override
  public void setChildWidth(AndroidViewComponent component, int width) {
  }

  @Override
  public void setChildHeight(AndroidViewComponent component, int height) {
  }

  public static Task getActiveTask() {
    return activeTask;
  }

  /**
   * Specifies the Version Code.
   *
   * @param vCode the version name of the application
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
    defaultValue = "1")
  @SimpleProperty(userVisible = false,
    description = "An integer value which must be incremented each time a new Android "
    +  "Application Package File (APK) is created for the Google Play Store.")
  public void VersionCode(int vCode) {
    // We don't actually need to do anything.
  }

  /**
   * Specifies the Version Name.
   *
   * @param vName the version name of the application
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = "1.0")
  @SimpleProperty(userVisible = false,
    description = "A string which can be changed to allow Google Play "
    + "Store users to distinguish between different versions of the App.")
  public void VersionName(String vName) {
    // We don't actually need to do anything.
  }

  public void deleteComponent(Object component) {
    if (component instanceof OnDestroyListener) {
      OnDestroyListener onDestroyListener = (OnDestroyListener) component;
      if (onDestroyListeners.contains(onDestroyListener)) {
        onDestroyListeners.remove(onDestroyListener);
      }
    }
  }

  // This is used by runtime.scm to call the Initialize of a component.
  public void callInitialize(Object component) throws Throwable {
    Method method;
    try {
      method = component.getClass().getMethod("Initialize", (Class<?>[]) null);
    } catch (SecurityException e) {
      Log.i(LOG_TAG, "Security exception " + e.getMessage());
      return;
    } catch (NoSuchMethodException e) {
      //This is OK.
      return;
    }
    try {
      Log.i(LOG_TAG, "calling Initialize method for Object " + component.toString());
      method.invoke(component, (Object[]) null);
    } catch (InvocationTargetException e){
      Log.i(LOG_TAG, "invoke exception: " + e.getMessage());
      throw e.getTargetException();
    }
  }

}
