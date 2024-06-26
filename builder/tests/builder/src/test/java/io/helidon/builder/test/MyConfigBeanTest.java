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

package io.helidon.builder.test;

import io.helidon.builder.test.testsubjects.MyConfigBean;
import io.helidon.builder.test.testsubjects.MyConfigBeanImpl;
import io.helidon.builder.test.testsubjects.MyConfigBeanManualImpl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class MyConfigBeanTest {

    @Test
    void manual() {
        MyConfigBean val = MyConfigBeanManualImpl.builder().build();
        assertThat(val.toString(),
                   equalTo("MyConfigBeanManualImpl(name=null, enabled=false, port=0)"));

        val = MyConfigBeanManualImpl.toBuilder(val)
                .name("test")
                .enabled(true)
                .port(80)
                .build();
        assertThat(val.toString(),
                   equalTo("MyConfigBeanManualImpl(name=test, enabled=true, port=80)"));
    }

    @Test
    void codeGen() {
        MyConfigBean val = MyConfigBeanImpl.builder().name("test").build();
        assertThat(val.toString(),
                   equalTo("MyConfigBean(name=test, enabled=false, port=8080)"));

        val = MyConfigBeanImpl.toBuilder(val)
                .name("test")
                .enabled(true)
                .port(80)
                .build();
        assertThat(val.toString(),
                   equalTo("MyConfigBean(name=test, enabled=true, port=80)"));
    }

    @Test
    void mixed() {
        MyConfigBean val1 = MyConfigBeanManualImpl.builder().name("initial").build();
        val1 = MyConfigBeanImpl.toBuilder(val1)
                .name("test")
                .enabled(true)
                .port(80)
                .build();
        assertThat(val1.toString(),
                   equalTo("MyConfigBean(name=test, enabled=true, port=80)"));

        MyConfigBean val2 = MyConfigBeanImpl.builder().name("test").build();
        val2 = MyConfigBeanManualImpl.toBuilder(val2)
                .name("test")
                .enabled(true)
                .port(80)
                .build();
        assertThat(val2.toString(),
                   equalTo("MyConfigBeanManualImpl(name=test, enabled=true, port=80)"));

        assertThat(val1.hashCode(), is(val2.hashCode()));
        assertThat(val1, equalTo(val2));
        assertThat(val2, equalTo(val1));
    }

}
