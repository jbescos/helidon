/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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
package io.helidon.microprofile.testing.mockbeans;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.literal.InjectLiteral;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import org.mockito.MockSettings;
import org.mockito.Mockito;

/**
 * CDI extension for Mock Beans implementation.
 */
public class MockBeansCdiExtension implements Extension {

    private final Set<Class<?>> mocks = new HashSet<>();

    void processMockBean(@Observes @WithAnnotations(MockBean.class) ProcessAnnotatedType<?> obj) throws Exception {
        var configurator = obj.configureAnnotatedType();
        configurator.fields().forEach(field -> {
            MockBean mockBean = field.getAnnotated().getAnnotation(MockBean.class);
            if (mockBean != null) {
                Field f = field.getAnnotated().getJavaMember();
                // Adds @Inject to be more user friendly
                field.add(InjectLiteral.INSTANCE);
                Class<?> fieldType = f.getType();
                mocks.add(fieldType);
            }
        });
        configurator.constructors().forEach(constructor -> {
            processMockBeanParameters(constructor.getAnnotated().getParameters());
        });
    }

    private void processMockBeanParameters(List<? extends AnnotatedParameter<?>> parameters) {
        parameters.stream().forEach(parameter -> {
            MockBean mockBean = parameter.getAnnotation(MockBean.class);
            if (mockBean != null) {
                Class<?> parameterType = parameter.getJavaParameter().getType();
                mocks.add(parameterType);
            }
        });
    }

    void registerOtherBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        // Register all mocks
        mocks.forEach(type -> {
            event.addBean()
                .addType(type)
                .scope(ApplicationScoped.class)
                .alternative(true)
                .createWith(inst -> {
                    Set<Bean<?>> beans = beanManager.getBeans(MockSettings.class);
                    if (!beans.isEmpty()) {
                        Bean<?> bean = beans.iterator().next();
                        MockSettings mockSettings = (MockSettings) beanManager.getReference(bean, MockSettings.class,
                                beanManager.createCreationalContext(null));
                        return Mockito.mock(type, mockSettings);
                    } else {
                        return Mockito.mock(type);
                    }
                })
                .priority(0);
        });
    }
}
