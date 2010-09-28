/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.requestfactory.client.impl.json;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A {@link JsonContext} with integer based location context.
 */
class JsonArrayContext extends JsonContext {

  int currentIndex = 0;

  JsonArrayContext(JavaScriptObject jso) {
    super(jso);
  }

  public int getCurrentIndex() {
    return currentIndex;
  }

  @Override
  public native void removeMe() /*-{
    delete this.@com.google.gwt.requestfactory.client.impl.json.JsonContext::getJso()()[this.@com.google.gwt.requestfactory.client.impl.json.JsonArrayContext::getCurrentIndex()()];
  }-*/;

  @Override
  public native void replaceMe(double d) /*-{
    this.@com.google.gwt.requestfactory.client.impl.json.JsonContext::getJso()()[this.@com.google.gwt.requestfactory.client.impl.json.JsonArrayContext::getCurrentIndex()()] = d;
  }-*/;

  @Override
  public native void replaceMe(String d) /*-{
    this.@com.google.gwt.requestfactory.client.impl.json.JsonContext::getJso()()[this.@com.google.gwt.requestfactory.client.impl.json.JsonArrayContext::getCurrentIndex()()] = d;
  }-*/;

  @Override
  public native void replaceMe(boolean d) /*-{
    this.@com.google.gwt.requestfactory.client.impl.json.JsonContext::getJso()()[this.@com.google.gwt.requestfactory.client.impl.json.JsonArrayContext::getCurrentIndex()()] = d;
  }-*/;

  @Override
  public native void replaceMe(JavaScriptObject jso) /*-{
    this.@com.google.gwt.requestfactory.client.impl.json.JsonContext::getJso()()[this.@com.google.gwt.requestfactory.client.impl.json.JsonArrayContext::getCurrentIndex()()] = jso;
  }-*/;

  public void setCurrentIndex(int currentIndex) {
    this.currentIndex = currentIndex;
  }
}