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
package io.syndesis.server.metrics.jsondb;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import io.syndesis.common.util.SyndesisServerException;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.jolokia.client.J4pClient;
import org.jolokia.client.J4pClientBuilder;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.jolokia.client.request.J4pSearchRequest;
import org.jolokia.client.request.J4pSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("PMD") // uff
public class PodMetricsReader implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PodMetricsReader.class);

    private static final String JOLOKIA_URL_FORMAT = "%sapi/v1/namespaces/%s/pods/https:%s:8778/proxy/jolokia/";

    private static final String ROUTE_ID = "RouteId";
    private static final String START_TIMESTAMP = "StartTimestamp";
    private static final String EXCHANGES_TOTAL = "ExchangesTotal";
    private static final String EXCHANGES_FAILED = "ExchangesFailed";
    private static final String LAST_COMPLETED_TIMESTAMP = "LastExchangeCompletedTimestamp";
    private static final String LAST_FAILED_TIMESTAMP = "LastExchangeFailureTimestamp";
    private static final String RESET_TIMESTAMP = "ResetTimestamp";

    private final J4pClient jolokia;
    private final String integration;
    private final String integrationId;
    private final String version;
    private final String pod;
    private final RawMetricsHandler handler;

    private final Map<String, ObjectName> cache = new HashMap<>();
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public PodMetricsReader(KubernetesClient kubernetes, String pod, String integration, String integrationId, String version,
            RawMetricsHandler handler) {
        this.pod = pod;
        this.integration = integration;
        this.integrationId = integrationId;
        this.version = version;
        this.handler = handler;
        this.jolokia = forPod(kubernetes, pod);
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("Collecting stats from integrationId: {}", integrationId);
            List<Map<String, String>> routeStats = getRoutes(integration, "[a-zA-z0-9_-]+");

            routeStats.forEach(
                m -> {

                    long messages = toLong(m.getOrDefault(EXCHANGES_TOTAL, "0"));
                    long errors = toLong(m.getOrDefault(EXCHANGES_FAILED, "0"));
                    Date lastCompleted = toDate(m.get(LAST_COMPLETED_TIMESTAMP));
                    Date lastFailed = toDate(m.get(LAST_FAILED_TIMESTAMP));
                    Date lastMessage =
                            (lastCompleted == null && lastFailed != null) ||
                            (lastCompleted != null && lastFailed != null && lastFailed.after(lastCompleted))
                        ? lastFailed
                        : lastCompleted;

                    Date resetDate = toDate(m.get(RESET_TIMESTAMP));
                    Date startDate = toDate(m.get(START_TIMESTAMP));


                    handler.persist(new RawMetrics.Builder()
                        .pod(pod)
                        .integrationId(integrationId)
                        .version(version)
                        .messages(messages)
                        .errors(errors)
                        .startDate(startDate)
                        .lastProcessed(Optional.ofNullable(lastMessage))
                        .resetDate(resetDate)
                        .build());
                }
            );

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            LOGGER.error("Collecting stats from integrationId: {}", integrationId);
        }
    }


    private Long toLong(String s) {
        if (s == null) {
            return 0L;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Date toDate(String s) {
        if (s == null) {
            return null;
        }
        try {
            return DATE_FORMAT.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * Code borrowed from the DefaultCamelController: https://github.com/apache/camel/blob/master/platforms/commands/commands-jolokia/src/main/java/org/apache/camel/commands/jolokia/DefaultJolokiaCamelController.java
     * Slight modifications have been applied.
     * Credits to: Claus & Tomo
     */
    private ObjectName lookupCamelContext(String camelContextName) throws Exception {
        ObjectName on = cache.get(camelContextName);
        if (on == null) {
            ObjectName found = null;
            J4pSearchResponse sr = jolokia.execute(new J4pSearchRequest("org.apache.camel:type=context,*"));
            if (sr != null) {
                for (ObjectName name : sr.getObjectNames()) {
                    String id = name.getKeyProperty("name");
                    id = removeLeadingAndEndingQuotes(id);
                    if (camelContextName.equals(id)) {
                        found = name;
                        break;
                    }
                }
            }
            if (found != null) {
                on = found;
                cache.put(camelContextName, on);
            }
        }
        return on;
    }

    public List<Map<String, String>> getRoutes(String camelContextName, String filter) throws Exception {
        if (jolokia == null) {
            throw new IllegalStateException("Need to connect to remote jolokia first");
        }

        List<Map<String, String>> answer = new ArrayList<Map<String, String>>();

        ObjectName found = camelContextName != null ? lookupCamelContext(camelContextName) : null;
        if (found != null) {

            String pattern = String.format("%s:context=%s,type=routes,*", found.getDomain(), found.getKeyProperty("context"));
            J4pSearchResponse sr = jolokia.execute(new J4pSearchRequest(pattern));

            List<J4pReadRequest> list = new ArrayList<J4pReadRequest>();
            for (ObjectName on : sr.getObjectNames()) {
                list.add(new J4pReadRequest(on, ROUTE_ID, RESET_TIMESTAMP, EXCHANGES_TOTAL, EXCHANGES_FAILED, LAST_COMPLETED_TIMESTAMP, LAST_FAILED_TIMESTAMP, START_TIMESTAMP ));
            }

            List<J4pReadResponse> lrr = jolokia.execute(list);
            for (J4pReadResponse rr : lrr) {
                String routeId = rr.getValue(ROUTE_ID).toString();
                if (filter == null || routeId.matches(filter)) {
                    Map<String, String> row = new LinkedHashMap<String, String>();
                    for (String attribute : rr.getAttributes()) {
                        if (rr.getValue(attribute) != null) {
                            row.put(attribute, rr.getValue(attribute).toString());
                        }
                    }
                    answer.add(row);
                }
            }

        }

        // sort the list
        Collections.sort(answer, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                // group by camel context first, then by route name
                String c1 = o1.get("camelContextName");
                String c2 = o2.get("camelContextName");
                int answer = c1.compareTo(c2);
                if (answer == 0) {
                    // okay from same camel context, then sort by route id
                    answer = o1.get("routeId").compareTo(o2.get("routeId"));
                }
                return answer;
            }
        });
        return answer;
    }

    /*
     * End of Camel Controller Code.
     */

    /**
     * Creates a {@link J4pClient} for the specified pod.
     *
     * @param kubernetes The {@link KubernetesClient} instance.
     * @param pod        The name of the pod.
     * @return An instance of the {@link J4pClient}.
     */
    private static J4pClient forPod(KubernetesClient kubernetes, String pod) {
        String jolokiaUrl = String.format(JOLOKIA_URL_FORMAT, kubernetes.getMasterUrl(), kubernetes.getNamespace(), pod);
        try {
            return new J4pClientBuilder()
                .url(jolokiaUrl)
                .user("user")
                .authenticator(new JolokiaKubernetesAuthenticator(kubernetes))
                .sslConnectionSocketFactory(new SSLConnectionSocketFactory(SSLUtils.sslContext(kubernetes.getConfiguration())))
                .build();

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            throw SyndesisServerException.launderThrowable(e);
        }
    }

    /**
     * Removes all leading and ending quotes (single and double) from the string
     *
     * @param s  the string
     * @return the string without leading and ending quotes (single and double)
     */
    public static String removeLeadingAndEndingQuotes(String s) {
        if (isEmpty(s)) {
            return s;
        }

        String copy = s.trim();
        if (copy.startsWith("'") && copy.endsWith("'")) {
            return copy.substring(1, copy.length() - 1);
        }
        if (copy.startsWith("\"") && copy.endsWith("\"")) {
            return copy.substring(1, copy.length() - 1);
        }

        // no quotes, so return as-is
        return s;
    }

    /**
     * Tests whether the value is <tt>null</tt> or an empty string.
     *
     * @param value  the value, if its a String it will be tested for text length as well
     * @return true if empty
     */
    public static boolean isEmpty(Object value) {
        return !isNotEmpty(value);
    }

    /**
     * Tests whether the value is <b>not</b> <tt>null</tt>, an empty string or an empty collection/map.
     *
     * @param value  the value, if its a String it will be tested for text length as well
     * @return true if <b>not</b> empty
     */
    @SuppressWarnings("unchecked")
    public static boolean isNotEmpty(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof String) {
            String text = (String) value;
            return text.trim().length() > 0;
        } else if (value instanceof Collection) {
            return !((Collection<?>)value).isEmpty();
        } else if (value instanceof Map) {
            return !((Map<?, ?>)value).isEmpty();
        } else {
            return true;
        }
    }
}
