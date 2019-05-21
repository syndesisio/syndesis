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
package io.syndesis.connector.email.meta;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.component.extension.MetaDataExtension;
import org.junit.Before;
import org.junit.Test;
import io.syndesis.connector.email.AbstractEmailServerTest;
import io.syndesis.connector.email.server.EMailTestServer;

public class EMailMetaDataTest extends AbstractEmailServerTest {

    @Before
    public void emailSetup() throws Exception {
        super.setup();
        context.start();
    }

    @Test
    public void testMetaDataExtensionRetrieval() throws Exception {
        EMailTestServer server = imapServer();
        EMailMetaDataExtension extension = new EMailMetaDataExtension(context);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(PROTOCOL, server.getProtocol());
        parameters.put(HOST, server.getHost());
        parameters.put(PORT, Integer.toString(server.getPort()));
        parameters.put(USER, TEST_ADDRESS);
        parameters.put(PASSWORD, TEST_PASSWORD);

        Optional<MetaDataExtension.MetaData> meta = extension.meta(parameters);
        assertThat(meta).isPresent();

        Object payload = meta.get().getPayload();
        assertThat(payload).isInstanceOf(EMailMetadata.class);
        assertThat(((EMailMetadata) payload).getProtocol()).isEqualTo(Protocol.getValueOf(server.getProtocol()));
    }
}
