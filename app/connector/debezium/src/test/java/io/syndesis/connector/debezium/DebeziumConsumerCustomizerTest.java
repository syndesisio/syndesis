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
package io.syndesis.connector.debezium;

import java.util.HashMap;
import java.util.Map;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import static org.assertj.core.api.Assertions.assertThat;

public class DebeziumConsumerCustomizerTest {

    final static String SAMPLE_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
        "MIID5jCCAs6gAwIBAgIJAKPwdVgydVCqMA0GCSqGSIb3DQEBCwUAMC0xEzARBgNV\n" +
        "BAoMCmlvLnN0cmltemkxFjAUBgNVBAMMDWNsdXN0ZXItY2EgdjAwHhcNMTkxMTIx\n" +
        "MTQzNTU0WhcNMjAxMTIwMTQzNTU0WjAsMRMwEQYDVQQKDAppby5zdHJpbXppMRUw\n" +
        "EwYDVQQDDAxicm9rZXIta2Fma2EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK\n" +
        "AoIBAQDkMPa5TPJ7aLIJ3IVMEkyvfXrab1sjrfviWyDHg/HBnpZ3ZBtylXsNtCYZ\n" +
        "yayqdBwZsPqZzXq3TV0+fgDb+dDjjzpC+rpxC2hvlPuwEruUDrmSu9OfQhEG9nNR\n" +
        "7mtzUMfy1pmXRctM7/AO+wanZLR7uG8ShQR6DUkexvW+oTF6Zu9/ds02PxYuCLpf\n" +
        "nRZP/+moLELbOmU3iXXc4ecagdV9jgKuiBuzvc9VoDZ1fRfyRAqKK3MkHxeR+9+/\n" +
        "WOigAdTiLLsYCOLGZrOGnjlmfsiy7DFl7j45npEiNFMp8sDACDwtO9kAb+6NKA/Y\n" +
        "n6l2o6oxwDfzc/Gt+DRp/1G1WUWJAgMBAAGjggEIMIIBBDCCAQAGA1UdEQSB+DCB\n" +
        "9YJHYnJva2VyLWthZmthLTAuYnJva2VyLWthZmthLWJyb2tlcnMuc3luZGVzaXMt\n" +
        "c2VydmljZXMuc3ZjLmNsdXN0ZXIubG9jYWyCLGJyb2tlci1rYWZrYS1ib290c3Ry\n" +
        "YXAuc3luZGVzaXMtc2VydmljZXMuc3Zjgjpicm9rZXIta2Fma2EtYm9vdHN0cmFw\n" +
        "LnN5bmRlc2lzLXNlcnZpY2VzLnN2Yy5jbHVzdGVyLmxvY2FsghZicm9rZXIta2Fm\n" +
        "a2EtYm9vdHN0cmFwgihicm9rZXIta2Fma2EtYm9vdHN0cmFwLnN5bmRlc2lzLXNl\n" +
        "cnZpY2VzMA0GCSqGSIb3DQEBCwUAA4IBAQCdJ8gEvnuqjws2B1hzCNK9Cxsr0ENd\n" +
        "06SlkistrZPbwgLjoYIppPMliHQBj/fe3Glhuyikt0dqDn17/gmsGapRPNQ0XtyD\n" +
        "LbfLaXWpaCkBUK3rv0rf4yFcOJ6OJHycqtXMmdPTuFJLLhv+FWWU8aXh7qihlPZO\n" +
        "FH9/Jkmr39yO9Io7BPgiUydZl0qYxUzmA0iwx0CHRVHOZeFqmBaMffEsXck4iu5c\n" +
        "NzJULGpQ17+M0zG77RqYuiDQOyXPmzugfjogM/WIVABrTP5vc+W/QjCF81WckOoS\n" +
        "Q+ffmxmsqD43XvaAfTUlZzyUtqDH4vPWJOJhgwTBHR5X7mSisneLyaqX\n" +
        "-----END CERTIFICATE-----\n";

    @Test
    public void shouldIncludeKafkaCustomizerOptions() {
        ComponentProxyComponent mockComponent = mock(ComponentProxyComponent.class);
        DebeziumConsumerCustomizer debeziumCustomizer = new DebeziumConsumerCustomizer(new DefaultCamelContext());
        Map<String, Object> userOptions = new HashMap<>();
        userOptions.put("brokers","1.2.3.4:9093");
        userOptions.put("transportProtocol","TSL");
        userOptions.put("brokerCertificate",SAMPLE_CERTIFICATE);

        debeziumCustomizer.customize(mockComponent, userOptions);

        assertThat(userOptions.get("configuration")).isNotNull();
    }
}
