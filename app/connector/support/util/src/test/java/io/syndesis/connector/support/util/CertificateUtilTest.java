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
package io.syndesis.connector.support.util;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test Certificate utility.
 *
 * @author dhirajsb
 */
public class CertificateUtilTest {

    static final String TEST_CERT = "-----BEGIN CERTIFICATE-----\n" + "MIIDmTCCAoGgAwIBAgIEQOz0BjANBgkqhkiG9w0BAQsFADB9MQswCQYDVQQGEwJV\n" + "UzELMAkGA1UECBMCQ0ExEjAQBgNVBAcTCVN1bm55dmFsZTEVMBMGA1UEChMMUmVk\n" + "IEhhdCBJbmMuMR8wHQYDVQQLExZNaWRkbGV3YXJlIEVuZ2luZWVyaW5nMRUwEwYD\n" + "VQQDEwxEaGlyYWogQm9rZGUwHhcNMTcxMjA2MTkzNjQxWhcNMTgwMzA2MTkzNjQx\n" + "WjB9MQswCQYDVQQGEwJVUzELMAkGA1UECBMCQ0ExEjAQBgNVBAcTCVN1bm55dmFs\n" + "ZTEVMBMGA1UEChMMUmVkIEhhdCBJbmMuMR8wHQYDVQQLExZNaWRkbGV3YXJlIEVu\n" + "Z2luZWVyaW5nMRUwEwYDVQQDEwxEaGlyYWogQm9rZGUwggEiMA0GCSqGSIb3DQEB\n" + "AQUAA4IBDwAwggEKAoIBAQCAZ7jv6VI0UkeD7Lc1fq9AYfoDqbupzmSKROZXdAd2\n" + "ry5GTugKYMyXgbQCe1Il68gosJN8RvS2iBnaPR/b9NhKbtwG6CNPr+S6Uzdr4LQI\n" + "S2u4PBSYtmyUAW7DjTnkcKvMQ6YBZv8nUbtpMQHc8kup9cPr0z4FlrFTtVFY5ZHi\n" + "bISlxFaEP4IxioifkMpp04Ms+eyQixPeoVdA6Y2CJ/5kA6MX4pQPTFGxbBuv/fr9\n" + "xOeez/ydOpKfPV1j2MIRGnzTreb6KRbn+QloRv10JwA1oI4r9CDv0qre8YjFWJPM\n" + "DRZKH0kJNjebbB8U8B4W3EKfLnQlp0nfEjph8854HH0fAgMBAAGjITAfMB0GA1Ud\n" + "DgQWBBQdDmyRXCCaPhme/5+1Cz/Ubn6YrDANBgkqhkiG9w0BAQsFAAOCAQEAMUw2\n" + "YT1o+SVB23BHnPWLBp/82tlUOinFyBx2jRVx+wOnscjYq9/nrTzSNFzDt37gxavQ\n" + "j3Rjo+UWuFMwtRL6vMjhs+jo40A/FtFnTKWVI2edwMbTkwlXdG1ZcUX1crP8nrn8\n" + "L16lQSELrNrE7+2UsQyASt+y9ojL1iUdsTSpwPEPIL1LbfR5LvTXASFK7aY+FlSn\n" + "ONT+lH6zBqYDMwPLvIH4juzmQ9Q04Ma5KVNWUg0ZP6dT48RiVOGhyIKCOmLhD+qQ\n" + "B+PB1Wkslb66q/qQQLuXtsES7iumHYwMY45pBEPiEfT2WBJVZ4GU6N1oO8kqCgkS\n" + "tZ/7er8bDHNKtOWgGQ==\n" + "-----END CERTIFICATE-----";

    @Test
    public void testCreateKeyManagers() throws Exception {
        final KeyManager[] keyManagers = CertificateUtil.createKeyManagers(TEST_CERT, "test-cert");
        assertThat(keyManagers).isNotNull().isNotEmpty();
    }

    @Test
    public void testCreateTrustManagers() throws Exception {
        final TrustManager[] trustManagers = CertificateUtil.createTrustManagers(TEST_CERT, "test-cert");
        assertThat(trustManagers).isNotNull().isNotEmpty();
    }
}
