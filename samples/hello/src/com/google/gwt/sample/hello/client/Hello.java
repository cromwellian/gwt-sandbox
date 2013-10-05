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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.gwt.user.client.Window.alert;

/**
 * HelloWorld application.
 */
public class Hello implements EntryPoint {


    public void onModuleLoad() {
      List<Double> strings = make(10, Math::random);
      double specialSum = filterSum(strings, x -> x > 0.5);
      alert("" + specialSum);
    }

    public <T> List<T> make(int numCopies, Supplier<T> supplier) {
        List<T> toReturn = new ArrayList<>();
        while (numCopies-- > 0) {
            toReturn.add(supplier.get());
        }
        return toReturn;
    }

    Double filterSum(List<Double> list, Predicate<Double> pred) {
        double s = 0;
        for (double d : list) {
            if (pred.test(d)) {
                s+=d;
            }
        }
        return s;
    }
}
