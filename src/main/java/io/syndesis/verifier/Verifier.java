package io.syndesis.verifier;

import java.util.List;
import java.util.Map;

/**
 * @author roland
 * @since 28/03/2017
 */
public interface Verifier {

    List<VerifierResponse> verify(Map<String, Object> parameters);

    enum Scope {
        PARAMETERS,
        CONNECTIVITY;
    }

    enum Status {
        OK,
        ERROR,
        UNSUPPORTED
    }
}
