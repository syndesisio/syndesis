/*
 * Copyright (C) 2016 Red Hat, Inc.
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
package io.syndesis.server.endpoint.v1.state;

import java.math.BigInteger;

/**
 * One configuration edition of settings used to store state on the client.
 */
public abstract class Edition {

    public final String authenticationAlgorithm;

    public final String encryptionAlgorithm;

    public byte[] tid;

    protected Edition(final long tid, final String encryptionAlgorithm, final String authenticationAlgorithm) {
        this.tid = BigInteger.valueOf(tid).toByteArray();
        this.authenticationAlgorithm = authenticationAlgorithm;
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    protected abstract KeySource keySource();
}
