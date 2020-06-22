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

    private final Diagnostic diagnostic;
    private String targetedString;

    public DdlAnalyzerException() {
      super();
      this.diagnostic = null;
    }

    /** Constructor with message. */
    public DdlAnalyzerException(String message) {
      super(message);
      this.diagnostic = new Diagnostic();
      this.diagnostic.setMessage(message);
    }

    /** Constructor with message. */
    public DdlAnalyzerException(DiagnosticSeverity severity, String message, Range range) {
      this(message);
      this.diagnostic.setSeverity(severity);
      this.diagnostic.setRange(range);
    }

    public Diagnostic getDiagnostic() {
        return diagnostic;
    }

    public void setErrorCode(String code) {
        this.diagnostic.setCode(code);
    }

    public String getErrorCode() {
        return this.diagnostic.getCode();
    }

    public String getTargetedString() {
        return targetedString;
    }

    public void setTargetedString(String targetedString) {
        this.targetedString = targetedString ;
    }

}
