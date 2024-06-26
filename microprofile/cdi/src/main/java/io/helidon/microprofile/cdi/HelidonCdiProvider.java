/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
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

package io.helidon.microprofile.cdi;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;

/**
 * Implementation of the CDI SPI.
 */
public class HelidonCdiProvider implements CDIProvider {
    private static final AtomicReference<CDI<Object>> CURRENT_CDI = new AtomicReference<>();

    @Override
    public CDI<Object> getCDI() {
        CDI<Object> cdi = CURRENT_CDI.get();
        if (null == cdi) {
            throw new IllegalStateException("There is no CDI instance available");
        }
        return cdi;
    }

    static void setCdi(CDI<Object> cdi) {
        CURRENT_CDI.set(cdi);
    }

    static void unset() {
        CURRENT_CDI.set(null);
    }
}
