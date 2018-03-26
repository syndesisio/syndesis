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
package io.syndesis.common.model.bulletin;

import java.util.OptionalInt;

import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithMetadata;
import io.syndesis.common.model.WithModificationTimestamps;

public interface BulletinBoard<T extends BulletinBoard<T>> extends WithId<T>, WithMetadata, WithLeveledMessages, WithModificationTimestamps {

    String getTargetResourceId();

    OptionalInt getNotices();

    OptionalInt getWarnings();

    OptionalInt getErrors();
}
