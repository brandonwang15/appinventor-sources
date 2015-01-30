// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;
import android.util.Log;

/**
 * Base class for all non-visible task components. *
 */
@SimpleObject
public abstract class AndroidNonVisibleTaskComponent implements Component {

  protected final Form form;
  protected final Task task;

  /**
   * Creates a new AndroidNonvisibleTaskComponent.
   *
   * @param container the container that this component will be placed in
   */
  protected AndroidNonVisibleTaskComponent(ComponentContainer container) {
    // The container is null for some test instances
    if (container != null) {
      if (container.$form() != null) {
        this.form = container.$form();
        this.task = null;
      } else {
        this.form = null;
        this.task = container.$task();
      }
    } else {
      this.form = null;
      this.task = null;
    }
  }

  // Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    if (form != null) {
      return form;
    } else {
      return task;
    }
  }

  protected void dispatchErrorOccurredEvent(final Component component, final String functionName,
      final int errorNumber, final Object... messageArgs) {
    if (form != null) {
      Log.i("DISPATCH", "dispatchErrorOccurredEvent FORM");      
      form.dispatchErrorOccurredEvent(component, functionName,
              errorNumber, (Object[]) messageArgs);
    } else {
      Log.i("DISPATCH", "dispatchErrorOccurredEvent TASK");      
      task.dispatchErrorOccurredEvent(component, functionName,
              errorNumber, (Object[]) messageArgs);
    }
  }
}
