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

/**
 * Interface for managing client subscriptions and publishing events to them.
 */
public interface EventBus {
    /**
     * Constants for known event types.
     *
     * NOTE: May be better to use enums but EventBus is all about strings.
     */
    @SuppressWarnings("PMD.ConstantsInInterface")
    interface Type {
        String CHANGE_EVENT = "change-event";
    }

    /**
     * Constants for known event action
     *
     * NOTE: May be better to use enums but EventBus is all about strings.
     */
    @SuppressWarnings("PMD.ConstantsInInterface")
    interface Action {
        String CREATED = "created";
        String UPDATED = "updated";
        String DELETED = "deleted";
    }

    /**
     * Callback interface clients implement to receive events.
     */
    interface Subscription {

        /**
         * This method must never block.
         *
         * @param event the type of event being delivered
         * @param data the data associated the the event type
         */
        void onEvent(String event, String data);
    }

    /**
     * Adds a subscription to the event bus.
     *
     * @param subscriberId unique id for the subscription.
     * @param handler the callback that will receive the events.
     * @return the previously registered subscription with that id or null.
     */
    Subscription subscribe(String subscriberId, Subscription handler);

    /**
     * Removes a subscription from the event bus.
     * @param subscriberId unique id for the subscription.
     * @return the previously registered subscription with that id or null if not registered.
     */
    Subscription unsubscribe(String subscriberId);

    /**
     * Send an event to all subscribers in a bus.  The event MAY get delivered to the subscriptions
     * after this call returns.
     */
    void broadcast(String event, String data);

    /**
     * Send an event to a specific subscriber on the bus.  The event MAY get delivered to the subscriptions
     * after this call returns.
     */
    void send(String subscriberId, String event, String data);
}
