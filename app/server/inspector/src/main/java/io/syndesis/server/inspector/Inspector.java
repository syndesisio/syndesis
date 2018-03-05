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
package io.syndesis.server.inspector;

import java.util.List;
import java.util.Optional;

interface Inspector {

    /**
     * Collects recursively all possible 'path' of the given shape definition.
     * Each item in the path corresponds to fields, fields of fields and so on.
     *
     * @param kind the kind of data shape
     * @param type the type of data shape
     * @param specification specification of data shape
     * @param optional an live example of data
     * @return all paths within the data shape
     */
    List<String> getPaths(String kind, String type, String specification, Optional<byte[]> optional);

    /**
     * Can this {@link Inspector} implementation support the given data shape.
     *
     * @param kind the kind of data shape
     * @param type the type of data shape
     * @param specification specification of data shape
     * @param optional an live example of data
     * @return true if it can
     */
    boolean supports(String kind, String type, String specification, Optional<byte[]> optional);
}
