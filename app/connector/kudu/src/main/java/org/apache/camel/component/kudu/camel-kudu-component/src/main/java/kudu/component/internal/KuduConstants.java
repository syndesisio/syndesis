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

package kudu.component.internal;

/**
 * Constants for Kudu component.
 */
public interface KuduConstants {

    // suffix for parameters when passed as exchange header properties
    String PROPERTY_PREFIX = "CamelKudu.";

    // thread profile name for this component
    String THREAD_PROFILE_NAME = "CamelKudu";
}
