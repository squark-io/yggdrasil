package io.squark.yggdrasil.core.api.util;

/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *
 *  See https://github.com/eclipse/aether-core/blob/master/aether-util/src/main/java/org/eclipse/aether/util/artifact/JavaScopes.java
 *******************************************************************************/
public interface Scopes {

    String COMPILE = "compile";

    String PROVIDED = "provided";

    String SYSTEM = "system";

    String RUNTIME = "runtime";

    String TEST = "test";


}
