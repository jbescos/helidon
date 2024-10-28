/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates.
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

package io.helidon.codegen.scan;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.helidon.common.types.Annotation;
import io.helidon.common.types.EnumValue;
import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypeNames;

import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ClassInfo;

/**
 * Factory for annotations.
 */
final class ScanAnnotationFactory {
    private ScanAnnotationFactory() {
    }

    /**
     * Creates an instance from an annotation mirror during annotation processing.
     *
     * @param ctx processing context
     * @param am  the annotation mirror
     * @return the new instance or empty if the annotation mirror passed is invalid
     */
    public static Annotation createAnnotation(ScanContext ctx,
                                              AnnotationInfo am) {
        TypeName typeName = ScanTypeFactory.create(am.getClassInfo());

        // ignore these annotations, unless one of them was explicitly requested
        var set = new HashSet<TypeName>();
        set.add(TypeNames.INHERITED);
        set.add(TypeNames.TARGET);
        set.add(TypeNames.RETENTION);
        set.add(TypeNames.DOCUMENTED);
        set.remove(typeName);

        return createAnnotation(ctx, am, set)
                .orElseThrow();
    }

    private static Optional<Annotation> createAnnotation(ScanContext ctx, AnnotationInfo am, HashSet<TypeName> processedTypes) {
        ClassInfo classInfo = am.getClassInfo();
        if (classInfo == null) {
            // cannot analyze this annotation
            return Optional.empty();
        }
        TypeName typeName = ScanTypeFactory.create(classInfo);

        if (processedTypes.contains(typeName)) {
            return Optional.empty();
        }
        var builder = Annotation.builder();

        classInfo.getAnnotationInfo()
                .stream()
                .map(it -> {
                    var newProcessed = new HashSet<>(processedTypes);
                    newProcessed.add(typeName);
                    return createAnnotation(ctx, it, newProcessed);
                })
                .flatMap(Optional::stream)
                .forEach(builder::addMetaAnnotation);

        return Optional.of(builder.typeName(typeName)
                                   .values(extractAnnotationValues(ctx, am))
                                   .build());
    }

    /**
     * Extracts values from the annotation mirror value.
     *
     * @param ctx the processing context
     * @param am  the annotation mirror
     * @return the extracted values
     */
    private static Map<String, Object> extractAnnotationValues(ScanContext ctx,
                                                               AnnotationInfo am) {

        Map<String, Object> result = new LinkedHashMap<>();
        AnnotationParameterValueList parameterValues = am.getParameterValues();
        for (AnnotationParameterValue parameterValue : parameterValues) {
            String name = parameterValue.getName();
            Object value = parameterValue.getValue();
            if (value != null) {
                result.put(name, toAnnotationValue(ctx, value));
            }
        }

        return result;
    }

    private static Object toAnnotationValue(ScanContext ctx, Object scanAnnotationValue) {
        if (scanAnnotationValue.getClass().isArray()) {
            List<Object> result = new ArrayList<>();
            int length = Array.getLength(scanAnnotationValue);
            for (int i = 0; i < length; i++) {
                result.add(toAnnotationValue(ctx, Array.get(scanAnnotationValue, i)));
            }
            return result;
        }

        return switch (scanAnnotationValue) {
            case AnnotationEnumValue anEnum -> EnumValue.create(TypeName.create(anEnum.getClassName()), anEnum.getValueName());
            case AnnotationClassRef aClass -> TypeName.create(aClass.getName());
            case AnnotationInfo annotation -> createAnnotation(ctx, annotation);
            default -> scanAnnotationValue;
        };

    }
}