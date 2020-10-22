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

package io.helidon.examples.microprofile.google.cloud.background.functions;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.helidon.microprofile.cloud.common.CloudFunction;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

/**
 * Example Function.
 *
 */
@CloudFunction
@ApplicationScoped
public class ExampleFunction implements BackgroundFunction<Map<String, String>> {

    @Inject
    private Service service;

    private String executeService(String input) {
        return service.toUpperCase(input);
    }

    @Override
    public void accept(Map<String, String> payload, Context context) throws Exception {
        System.out.println("Payload " + executeService(payload.toString()));
    }

}
