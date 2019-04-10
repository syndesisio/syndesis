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
package io.syndesis.server.endpoint.v1.handler.events;

import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import io.syndesis.common.model.EventMessage;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Component;

@Path("/event/reservations")
@Api(value = "events")
@Component
public class EventReservationsHandler {

    // TODO: expire reservations after 1 min to avoid exploits
    private final ConcurrentHashMap<String, Reservation> reservedEventStreams = new ConcurrentHashMap<>();

    public static class Reservation {
        private final Principal principal;
        private final long createdAt = System.currentTimeMillis();

        Reservation(Principal principal) {
            this.principal = principal;
        }

        public Principal getPrincipal() {
            return principal;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }

    @POST()
    public EventMessage reserveEventStream(@Context SecurityContext sc) {
        String uuid;
        do  {
            uuid = UUID.randomUUID().toString();
        } while( reservedEventStreams.putIfAbsent(uuid, new Reservation(sc.getUserPrincipal()))!=null ); // low probability but might as well check.
        return EventMessage.of("uuid", uuid);
    }

    public Reservation claimReservation(String reservation) {
        return reservedEventStreams.remove(reservation);
    }

    public Reservation existsReservation(String reservation) {
        return reservedEventStreams.get(reservation);
    }

}
