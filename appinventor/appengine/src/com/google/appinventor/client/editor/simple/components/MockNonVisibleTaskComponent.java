// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.widgets.Icon;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public final class MockNonVisibleTaskComponent extends MockComponent {

  private final Icon iconWidget;

  /**
   * Creates a new instance of a non-visible task component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   */
  public MockNonVisibleTaskComponent(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
    iconWidget = new Icon(iconImage);

    initComponent(iconWidget);
  }

  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);
  }

  /**
   * {@inheritDoc}
   *
   * This is always {@code false} for subclasses of this class.
   */
  @Override
  public final boolean isVisibleComponent() {
    return false;
  }


  @Override
  public final boolean isTaskComponent() {
    return true;
  }

  @Override
  public final int getPreferredHeight() {
    return 0;
  }

  @Override
  public final int getPreferredWidth() {
    return 0;
  }

  @Override
  protected final void onSelectedChange(boolean selected) {
    super.onSelectedChange(selected);
    if (selected) {
      iconWidget.select();
    } else {
      iconWidget.deselect();
    }
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // No visual changes after property changes (other than icon name).
    if (propertyName.equals(PROPERTY_NAME_NAME)) {
      iconWidget.setCaption(newValue);
    }
  }
}
