/*
 * Copyright (c) 2019, 2022 Oracle and/or its affiliates.
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
package io.helidon.reactive.dbclient.jdbc;

import io.helidon.reactive.dbclient.spi.DbClientProvider;

/**
 * Provider for JDBC database implementation.
 */
public class JdbcDbClientProvider implements DbClientProvider {

    static final String JDBC_DB_TYPE = "jdbc";

    @Override
    public String name() {
        return JDBC_DB_TYPE;
    }

    @Override
    public JdbcDbClientProviderBuilder builder() {
        return new JdbcDbClientProviderBuilder();
    }

}
