package com.google.gwt.sample.hello.client;

import static com.google.gwt.user.client.Window.alert;

/**
 * Created by cromwellian on 10/3/13.
 */ //  ClickHandler e;
    public interface Provider<T> {
        T get();

        default Provider<T> name69() {

           return this;
        }
    }
