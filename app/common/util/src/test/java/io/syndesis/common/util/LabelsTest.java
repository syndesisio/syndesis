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
package io.syndesis.common.util;

import org.junit.Assert;
import org.junit.Test;

public class LabelsTest {

    @Test
    public void testValid() {
        Assert.assertTrue(Labels.isValid("abcdefghijklmnopqrstyvwxyz0123456789"));
        Assert.assertTrue(Labels.isValid("012345678901234567890123456789012345678901234567890123456789123"));
    }

    @Test
    public void testInvalid() {
        Assert.assertFalse(Labels.isValid("-abcdefghijklmnopqrstyvwxyz0123456789"));
        Assert.assertFalse(Labels.isValid("abcdefghijklmnopqrstyvwxyz0123456789-"));
        Assert.assertFalse(Labels.isValid("01234567890123456789012345678901234567890123456789012345678912345"));
    }

    @Test
    public void testValidateGeneratedKeys() {
        for (int i=0;i<1000;i++)
        Assert.assertTrue(Labels.isValid(KeyGenerator.createKey()));
    }
}
