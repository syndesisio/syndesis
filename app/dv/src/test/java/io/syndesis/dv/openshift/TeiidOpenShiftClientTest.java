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

package io.syndesis.dv.openshift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import io.syndesis.dv.RepositoryManager;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.MetadataInstance;
import io.syndesis.dv.server.DvConfigurationProperties;

public class TeiidOpenShiftClientTest {

    static abstract class MockRepositoryManager implements RepositoryManager {

        @Override
        public <T> T runInTransaction(boolean rollbackOnly, Task<T> task) {
            return task.call();
        }
    }

    @Test public void testSetTeiidName() {
        MetadataInstance metadata = Mockito.mock(MetadataInstance.class);

        RepositoryManager mock = Mockito.mock(MockRepositoryManager.class);
        Mockito.when(mock.runInTransaction(Mockito.anyBoolean(), Mockito.any())).thenCallRealMethod();

        TeiidOpenShiftClient client = new TeiidOpenShiftClient(metadata, new EncryptionComponent("blah"), new DvConfigurationProperties(), mock, null);

        DefaultSyndesisDataSource dsd = new DefaultSyndesisDataSource();

        String name = client.getUniqueTeiidName(dsd, "sys");

        assertTrue(name.startsWith("sys_"));

        name = client.getUniqueTeiidName(dsd, "View");

        assertEquals("View", name);

        name = client.getUniqueTeiidName(dsd, "?syS.");

        assertTrue(name.startsWith("syS_"));
    }

}
