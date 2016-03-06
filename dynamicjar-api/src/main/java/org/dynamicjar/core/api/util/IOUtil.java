package org.dynamicjar.core.api.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-03-06.
 * Copyright 2016
 */
public class IOUtil {

    private IOUtil() {}

    public static File getResourceAsFile(InputStream in) {
        try {
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
