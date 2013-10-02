/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.sample.hello.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

import static com.google.gwt.user.client.Window.alert;

/**
 * HelloWorld application.
 */
public class Hello implements EntryPoint {
  double field = Math.random();
  static double sfield = 80;
//  ClickHandler e;


  public void onModuleLoad() {

    int  localx = 42;

//    Button b2 = new Button("Click", new ClickHandler() {
//        @Override
//        public void onClick(ClickEvent event) {
//            Window.alert("old style");
//        }
//    });
    Button b = new Button("Click me", (ClickEvent event) -> {
//        int innerLocal = 10;
//        Runnable r = () -> {
          alert("x2hello " + event + localx + field);
//          alert("world " + event + localx + sfield + innerLocal);
//        };
//        r.run();
    });

    Button b2 = new Button("Click2", this::someMethod);
    RootPanel.get().add(b2);
    RootPanel.get().add(b);
  }

   public void someMethod(ClickEvent event) {
       alert("goodbye " + event + field);
   }
}
