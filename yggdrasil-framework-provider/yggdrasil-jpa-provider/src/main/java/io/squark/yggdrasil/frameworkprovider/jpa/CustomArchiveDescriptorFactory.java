/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.squark.yggdrasil.frameworkprovider.jpa;

import org.hibernate.boot.archive.internal.*;
import org.hibernate.boot.archive.spi.ArchiveDescriptor;
import org.hibernate.internal.util.StringHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class CustomArchiveDescriptorFactory extends StandardArchiveDescriptorFactory {

    /**
     * Modified from super to handle nested jar.
     *
     * @param url   url
     * @param entry entry
     * @return ArchiveDescriptor
     */
    @Override
    public ArchiveDescriptor buildArchiveDescriptor(URL url, String entry) {
        final String protocol = url.getProtocol();
        String filePart = url.getFile();
        if ("jar".equals(protocol)) {
            return new JarProtocolArchiveDescriptor(this, url, entry);
        }
        // Added part:
        if (filePart.contains("!/")) {
            try {
                url = new URL("jar:" + url.toExternalForm());
                return new JarProtocolArchiveDescriptor(this, url, entry);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else if (StringHelper.isEmpty(protocol) || "file".equals(protocol) || "vfszip".equals(protocol) || "vfsfile".equals(
                protocol))
        {
            final File file;
            try {
                if (filePart != null && filePart.indexOf(' ') != -1) {
                    //unescaped (from the container), keep as is
                    file = new File(url.getFile());
                } else {
                    file = new File(url.toURI().getSchemeSpecificPart());
                }

                if (!file.exists()) {
                    throw new IllegalArgumentException(
                            String.format("File [%s] referenced by given URL [%s] does not exist", filePart,
                                    url.toExternalForm()));
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Unable to visit JAR " + url + ". Cause: " + e.getMessage(), e);
            }

            if (file.isDirectory()) {
                return new ExplodedArchiveDescriptor(this, url, entry);
            } else {
                return new JarFileBasedArchiveDescriptor(this, url, entry);
            }
        } else {
            //let's assume the url can return the jar as a zip stream
            return new JarInputStreamBasedArchiveDescriptor(this, url, entry);
        }
    }
}
