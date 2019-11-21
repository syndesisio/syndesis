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
package io.syndesis.dv.openshift;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.syndesis.dv.KException;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

@Component
public class SyndesisConnectionMonitor {
    private static final Log LOGGER = LogFactory.getLog(SyndesisConnectionMonitor.class);
    private volatile WebSocket webSocket;
    private volatile boolean connected;
    private ObjectMapper mapper = new ObjectMapper();
    private SyndesisConnectionSynchronizer connectionSynchronizer;
    private ScheduledThreadPoolExecutor executor;

    static class Message {
        private String event;
        private String data;

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    static class EventMsg implements Comparable<EventMsg>{
        enum Type {
            created, deleted, updated
        };
        private Type action;
        private String kind;
        private String id;

        public Type getAction() {
            return action;
        }
        public void setAction(Type action) {
            this.action = action;
        }
        public String getKind() {
            return kind;
        }
        public void setKind(String kind) {
            this.kind = kind;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        @Override
        public int compareTo(EventMsg o) {
            return id.compareTo(o.id);
        }
    }

    public SyndesisConnectionMonitor(@Autowired SyndesisConnectionSynchronizer scs, @Autowired ScheduledThreadPoolExecutor connectionExecutor) {
        this.connectionSynchronizer = scs;
        this.executor = connectionExecutor;
    }

    static Request.Builder buildRequest() {
        Request.Builder builder = new Request.Builder();
        builder.addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("X-Forwarded-User", "user")
            .addHeader("SYNDESIS-XSRF-TOKEN", "awesome")
            .addHeader("X-Forwarded-Access-Token", "supersecret");
        return builder;
    }

    public void connect() {
        if (isConnected()) {
            return;
        }
        String RESERVATIONS_PATH = "http://syndesis-server/api/v1/event/reservations";
        String WS_PATH = "ws://syndesis-server/api/v1/event/streams.ws/";

        ConnectionPool pool = new ConnectionPool(5, 10000, TimeUnit.MILLISECONDS);
        OkHttpClient client = new OkHttpClient.Builder().connectionPool(pool).build();

        Request request = buildRequest().url(RESERVATIONS_PATH).post(RequestBody.create(null, "")).build();

        Message message = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                message = mapper.readValue(response.body().bytes(), Message.class);
            }
            response.close();
        } catch (IOException e) {
            LOGGER.info("Failed to retrive Subscription ID for reading the connection events: " + e.getMessage());
        }

        if (message == null) {
            return;
        }

        LOGGER.info("Connecting to syndesis server for process connection events");

        request = buildRequest().url(WS_PATH + message.getData()).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                LOGGER.debug("   ---->>>  onOpen(): Socket has been opened successfully.");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                LOGGER.debug("   ---->>>  onMessage(String): New Text Message received " + text);
                handleMessage(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString message) {
                LOGGER.debug("   ---->>>  onMessage(ByteString): New ByteString Message received " + message);
                handleMessage(new String(message.asByteBuffer().array(), Charset.defaultCharset()));
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                LOGGER.debug("   ---->>>  onClosing(): Close request from server with reason '" + reason + "'");
                webSocket.close(1000, reason);
                connected = false;
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                LOGGER.debug("   ---->>>  onClosed(): Socket connection closed with reason '" + reason + "'");
                connected = false;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                LOGGER.error("   ---->>>  onFailure(): failure received", t);
                webSocket.close(1000, t.getMessage());
                connected = false;
            }
        });
    }

    private void handleMessage(String text) {
        executor.execute(() -> {
            try {
                Message msg = mapper.readValue(text.getBytes(), Message.class);
                if (msg.getEvent().contentEquals("message") && msg.getData().contentEquals("connected")) {
                    connected = true;
                    connectionSynchronizer.synchronizeConnections(false);
                } else if (msg.getEvent().contentEquals("change-event")) {
                    EventMsg event = mapper.readValue(msg.getData().getBytes(), EventMsg.class);
                    if (event.getKind().contentEquals("connection")) {
                        connectionSynchronizer.handleConnectionEvent(event);
                    } else {
                        LOGGER.debug("Message discarded " + text);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("handleMessage: Failed to process the message", e);
            }
        });
    }

    public boolean isConnected() {
        return webSocket != null && connected;
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "programmed standard close() call");
        }
        webSocket = null;
        connected = false;
    }

    @PostConstruct
    public void init() {
        this.executor.scheduleAtFixedRate(()->this.connect(), 5, 15, TimeUnit.SECONDS);
        this.executor.scheduleAtFixedRate(()->{
            try {
                if (connected) {
                    connectionSynchronizer.synchronizeConnections(true);
                }
            } catch (KException e) {
                LOGGER.error("failed to synchronize", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }
}
