package io.squark.dynamicjar.frameworkprovider.resteasy.test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-29.
 * Copyright 2016
 */
@ApplicationPath("/")
public class MyApplication extends Application {
    public MyApplication() {
        System.err.println("AAAAA");
    }

}
