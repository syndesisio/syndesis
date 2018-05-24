package io.syndesis.server.openshift;

/**
 * Enumerates all the ways a integration can be exposed to the outside world.
 */
public enum Exposure {

    /**
     * Default: do not expose the integration.
     */
    NONE,

    /**
     * Expose the integration using a HTTP route.
     */
    DIRECT;

}
