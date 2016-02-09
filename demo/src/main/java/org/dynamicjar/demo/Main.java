package org.dynamicjar.demo;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dynamicjar.core.main.DynamicJar;

import java.io.IOException;

/**
 * *** AUTOTRADE ***
 * <p>
 * <p>
 * Created by Erik Håkansson on 2016-02-05.
 * Copyright 2016
 */
public class Main {
    public static void main(String[] args) {
        try {
            DynamicJar.loadDependencies(Main.class, "org.dynamicjar", "dynamicjar-demo");
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
