/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates.
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

package io.helidon.common.uri;

import java.net.URI;
import java.util.Objects;

import io.helidon.common.parameters.Parameters;

class UriPathMatrix extends UriPathNoParam {
    private final String rawPath;
    private Parameters pathParams;

    UriPathMatrix(String rawPath, String noParamPath) {
        super(noParamPath);
        this.rawPath = rawPath;
    }

    UriPathMatrix(String rawPath, String noParamPath, UriPath absolute) {
        super(noParamPath, absolute);
        this.rawPath = rawPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UriPathMatrix that = (UriPathMatrix) o;
        return Objects.equals(rawPath, that.rawPath) && Objects.equals(matrixParameters(), that.matrixParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rawPath, matrixParameters());
    }

    @Override
    public String rawPath() {
        return rawPath;
    }

    @Override
    public Parameters matrixParameters() {
        if (pathParams == null) {
            pathParams = UriMatrixParameters.create(rawPath);
        }
        return pathParams;
    }

    @Override
    public void validate() {
        //noinspection ResultOfMethodCallIgnored - this is to validate the path is correct
        URI.create(rawPath);
        super.validate();
    }
}
