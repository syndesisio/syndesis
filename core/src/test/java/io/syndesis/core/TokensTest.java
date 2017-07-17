/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.core;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TokensTest {

    @Test
    public void expiryToken() {
        // 2017-04-20T08:17:31.000+0200
        String expired_token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJKc0tLQTE5S2IwTmJGcTYtaGI1QnRUU0s2UzN0dWJYTjF4REFlYnFMT0pJIn0.eyJqdGkiOiI0N2QwYzBlOS02MDE4LTRhMzgtYjUyNy0zMTQwZWNjNWMzMGMiLCJleHAiOjE0OTI2NjkwNTEsIm5iZiI6MCwiaWF0IjoxNDkyNjY4NzUxLCJpc3MiOiJodHRwczovL2lwYWFzLXN0YWdpbmcuYjZmZi5yaC1pZGV2Lm9wZW5zaGlmdGFwcHMuY29tL2F1dGgvcmVhbG1zL2lwYWFzIiwiYXVkIjoiaXBhYXMtdWkiLCJzdWIiOiIyMTI1ZDUyZC0wZGI1LTQ2NzAtYjc0YS05ZDI4ZDllMTliN2EiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJpcGFhcy11aSIsIm5vbmNlIjoib3JOZUFNRWlsNGFGUVg3aGF1WDBweXVUUnV4N1o3VU1EOXh4ZllZb2xKY3RtcldmWnlGMGRBM1hzeldncHBCcyIsImF1dGhfdGltZSI6MTQ5MjY2ODc1MCwic2Vzc2lvbl9zdGF0ZSI6IjE4ZmQzNTBhLWMzNmQtNGJlMy1iNzI3LTM3YmQwYzQwOTVhZiIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6ImIzM2ZmOWEyLWZmYjctNDVhMS04OWU3LTEzNTEzOGE0YzI3ZCIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2lwYWFzLXN0YWdpbmcuYjZmZi5yaC1pZGV2Lm9wZW5zaGlmdGFwcHMuY29tIiwiaHR0cDovL2xvY2FsaG9zdDo0MjAwIl0sInJlc291cmNlX2FjY2VzcyI6eyJicm9rZXIiOnsicm9sZXMiOlsicmVhZC10b2tlbiJdfX0sIm5hbWUiOiJSb2xhbmQgSHVzcyIsInByZWZlcnJlZF91c2VybmFtZSI6InJodXNzIiwiZ2l2ZW5fbmFtZSI6IlJvbGFuZCIsImZhbWlseV9uYW1lIjoiSHVzcyIsImVtYWlsIjoicmh1c3NAcmVkaGF0LmNvbSJ9.Iku4ZDzU5jacjzecRoTWj1z6WV0BsEsrUT8Jyo_oTxarhouYkg0RV_dQma1hYLSC_Lhh-l4ZvA7FEsWOBSq-4Yf7OzE_CnDBVZQlFRTajLDkHObQdYprG04Ukodh8H4y1eDoaxGMVrEjIKRLMKChyDC4WLonqhqQf9YoInWVEDVoYgzDJf-e5i5If8lRR3nJdA1K94GbGjSDbaS4kW-rbJjUDDem4NuMfIxgV1jca3JaNll9yjd37gzYFDZo3KsL1_Z2gTB4440DnpOU7lh4Qv59474mu6U47LGe-4CilEU5hH4FavYlcYIK2HBUjT3UAhgaq4ryfR-7URf7DBi_NA";
        assertTrue(Tokens.isTokenExpired(expired_token));
        assertTrue(Tokens.isTokenExpired("blub"));
    }
}
