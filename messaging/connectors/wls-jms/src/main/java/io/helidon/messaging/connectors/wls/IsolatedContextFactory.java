/*
 * Copyright (c) 2022 Oracle and/or its affiliates.
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
package io.helidon.messaging.connectors.wls;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Initial JNDI context for Weblogic thin client initial context loaded by different classloader.
 */
public class IsolatedContextFactory implements InitialContextFactory {

    private static final String WLS_INIT_CTX_FACTORY = "weblogic.jndi.WLInitialContextFactory";

    @Override
    public Context getInitialContext(Hashtable<?, ?> env) throws NamingException {
        return ThinClientClassLoader.executeInIsolation(() -> {
            try {
                Class<?> wlInitialContextFactory = ThinClientClassLoader.getInstance().loadClass(WLS_INIT_CTX_FACTORY);
                Constructor<?> contextFactoryConstructor = wlInitialContextFactory.getConstructor();
                InitialContextFactory contextFactoryInstance = (InitialContextFactory) contextFactoryConstructor.newInstance();
                return contextFactoryInstance.getInitialContext(env);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot find " + WLS_INIT_CTX_FACTORY, e);
            } catch (NoSuchMethodException
                     | InvocationTargetException
                     | InstantiationException
                     | IllegalAccessException e) {
                throw new RuntimeException("Cannot instantiate " + WLS_INIT_CTX_FACTORY, e);
            }
        });
    }
}
