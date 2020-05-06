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

import java.util.ArrayList;
import java.util.List;

public class DdlTokenParserReport {
    private List<DdlAnalyzerException> exceptions;
    private boolean parensMatch = true;
    private boolean bracesMatch = true;

    public DdlTokenParserReport() {
        exceptions = new ArrayList<DdlAnalyzerException>();
    }

    public List<DdlAnalyzerException> getExceptions() {
        return exceptions;
    }

    public void addException(DdlAnalyzerException error) {
        if (error != null) {
            exceptions.add(error);
        }
    }

    public boolean doParensMatch() {
        return parensMatch;
    }

    public void setParensMatch(boolean parensMatch) {
        this.parensMatch = parensMatch;
    }

    public boolean doBracesMatch() {
        return bracesMatch;
    }

    public void setBracesMatch(boolean bracesMatch) {
        this.bracesMatch = bracesMatch;
    }

    public void log() {
        System.out.println("\n########### DDL PARSER REPORT ###########\n");
        if( !this.exceptions.isEmpty() ) {
            for(DdlAnalyzerException ex: this.exceptions ) {
                System.out.println("\t" + ex.getMessage());
            }
        } else {
            System.out.println("\t No parsing problems detected") ;
        }

        System.out.println("\n#########################################\n");
    }
}
