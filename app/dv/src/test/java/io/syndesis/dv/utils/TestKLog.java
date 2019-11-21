/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings( {"javadoc", "nls"} )
public class TestKLog {

    private KLog logger;

    @Before
    public void setup() {
        logger = KLog.getLogger();
    }

    @Test
    public void testLogInit() {
        try {
            logger = KLog.getLogger();
            assertNotNull(logger);
        } catch (Throwable throwable) {
            fail("Should not throw an exception " + throwable.getLocalizedMessage());
        }
    }
}
