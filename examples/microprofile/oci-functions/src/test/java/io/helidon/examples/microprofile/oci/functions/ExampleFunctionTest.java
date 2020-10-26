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

package io.helidon.examples.microprofile.oci.functions;

import com.fnproject.fn.testing.FnResult;
import com.fnproject.fn.testing.FnTestingRule;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExampleFunctionTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

    @Test
    public void testWithFN() throws ClassNotFoundException {
        testing.givenEvent().withBody("helidon function").enqueue();
        testing.thenRun(OCIType.class, "apply");
        FnResult result = testing.getOnlyResult();
        assertEquals("HELIDON FUNCTION", result.getBodyAsString());
    }

    @Test
    public void testWithoutFN() {
        String result = new OCIType().apply("helidon function");
        assertEquals("HELIDON FUNCTION", result);
    }
}
