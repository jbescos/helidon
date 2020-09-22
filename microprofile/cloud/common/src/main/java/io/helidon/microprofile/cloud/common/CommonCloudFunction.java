/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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
package io.helidon.microprofile.cloud.common;

import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import io.helidon.config.Config;
import io.helidon.microprofile.cdi.Main;

/**
 * Contains common functionality to be extended by every cloud implementations.
 *
 * @param <T> the delegate type
 */
public abstract class CommonCloudFunction<T> {

    /**
     * Fully qualified name of the class that will implement the cloud function
     */
    public static final String CLOUD_FUNCTION_IMPLEMENTATION = "helidon.cloud.function.implementation.class";

    /**
     * First time is invoked it initializes Helidon and creates an instance
     * specified in configuration {@link #CLOUD_FUNCTION_IMPLEMENTATION}.
     * Next invocations will reuse the same instance.
     *  
     *
     * @return the implementation of specific cloud interface
     */
    @SuppressWarnings("unchecked")
    protected final T delegate() {
        return (T) LazyHelidonInitializer.DELEGATE;
    }

    /**
     * Avoids Helidon is initialized in case CommonCloudFunction is never used.
     *
     */
    private static class LazyHelidonInitializer {

        private static final Logger LOGGER = Logger.getLogger(LazyHelidonInitializer.class.getName());
        private static final Object DELEGATE;

        static {
            // FIXME use ConfigProvider.getConfig()
            // FIXME create CloudFunctionCdiExtension
            // Mandatory value, but it can be empty
            String delegateClass = Config.create().get(CLOUD_FUNCTION_IMPLEMENTATION).asString().get();
            LOGGER.fine(() -> CLOUD_FUNCTION_IMPLEMENTATION + "=" + delegateClass);
            // FIXME how to shutdown?
            Main.main(new String[0]);
            LOGGER.fine(() -> "Helidon is started");
            if (!delegateClass.isEmpty()) {
                try {
                    DELEGATE = CDI.current().select(Class.forName(delegateClass)).get();
                    LOGGER.fine(() -> "Delegate is " + DELEGATE);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(delegateClass + " was not found", e);
                }
            } else {
                // It doesn't support delegate
                DELEGATE = new Object();
            }
        }

    }

}
