package io.syndesis.server.endpoint.v1.dto;

import java.util.Optional;

import org.immutables.value.Value;

/**
 * @author roland
 * @since 07.03.18
 */
@Value.Immutable
public interface MetaData {

    enum Type {
        DANGER, INFO, SUCCESS, WARNING
    }

    Optional<String> getMessage();

    Optional<Type> getType();
}
