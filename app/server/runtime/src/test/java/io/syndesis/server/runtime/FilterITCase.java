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
package io.syndesis.server.runtime;

import io.syndesis.common.model.filter.FilterOptions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class FilterITCase extends BaseITCase {
    @Test
    public void shouldGetGlobalFilterOptions() {
       ResponseEntity<FilterOptions> response = get("/api/v1/integrations/filters/options", FilterOptions.class, tokenRule.validToken(), HttpStatus.OK);
       FilterOptions filterOptions = response.getBody();
       Assert.assertNotNull(filterOptions);
    }
}
