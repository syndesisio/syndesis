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
package io.syndesis.dv.lsp.parser;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;

public class DdlAnalyzerException extends Exception {

    /**
     * The version identifier for this Serializable class.
     * Increment only if the <i>serialized</i> form of the
     * class changes.
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private Diagnostic diagnostic;

    /**
     * The following constructors are for use by you for whatever
     * purpose you can think of.  Constructing the exception in this
     * manner makes the exception behave in the normal way - i.e., as
     * documented in the class "Throwable".  The fields "errorToken",
     * "expectedTokenSequences", and "tokenImage" do not contain
     * relevant information.  The JavaCC generated code does not use
     * these constructors.
     */
    public DdlAnalyzerException() {
      super();
    }

    /** Constructor with message. */
    public DdlAnalyzerException(String message) {
      super(message);
      this.diagnostic = new Diagnostic();
      this.diagnostic.setMessage(message);
    }

    /** Constructor with message. */
    public DdlAnalyzerException(DiagnosticSeverity severity, String message, Range range) {
      super(message);
      this.diagnostic = new Diagnostic();
      this.diagnostic.setSeverity(severity);
      this.diagnostic.setMessage(message);
      this.diagnostic.setRange(range);
    }


    public Diagnostic getDiagnostic() {
        return diagnostic;
    }
}
