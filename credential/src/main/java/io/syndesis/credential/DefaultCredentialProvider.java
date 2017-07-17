/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.credential;

import org.springframework.social.connect.ConnectionFactory;

public class DefaultCredentialProvider<A, T> implements CredentialProvider<A, T> {

    private final Applicator<T> applicator;

    private final ConnectionFactory<A> connectionFactory;

    public DefaultCredentialProvider(final ConnectionFactory<A> connectionFactory, final Applicator<T> applicator) {
        this.connectionFactory = connectionFactory;
        this.applicator = applicator;
    }

    @Override
    public Applicator<T> applicator() {
        return applicator;
    }

    @Override
    public ConnectionFactory<A> connectionFactory() {
        return connectionFactory;
    }

}
