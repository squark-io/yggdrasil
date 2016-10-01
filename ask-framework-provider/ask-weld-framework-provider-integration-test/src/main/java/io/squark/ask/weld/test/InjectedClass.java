package io.squark.ask.weld.test;

import javax.enterprise.inject.Default;

/**
 * ask
 * <p>
 * Created by Erik Håkansson on 2016-02-28.
 * Copyright 2016
 */
@Default
public class InjectedClass {
    public void sayHello() {
        System.out.println(InjectedClass.class.getName() + ": HELLO");
    }
}
