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
package io.syndesis.dv.lsp.hover;


import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class HoverProcessor implements SQLParserConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(HoverProcessor.class);
    private TextDocumentItem textDocumentItem;

    public HoverProcessor(TextDocumentItem textDocumentItem) {
        this.textDocumentItem = textDocumentItem;
    }

    public String getHover(Position position) {
        System.out.println("\n >> HP.getHover()  Position = " + position);
        try {
            DdlTokenAnalyzer helper = new DdlTokenAnalyzer(this.textDocumentItem.getText());
            Token token = helper.getTokenFor(position);

            if( token != null ) {
                System.out.print("\n >> HP.getHover() Found Token = " + token.image);
                String hoverContent = null;

                switch(token.kind) {
                    case CREATE:
                        hoverContent = "<html>Hovering on CREATE...</html>";
                        break;
                    case VIEW:
                        hoverContent = "<html>Hovering on VIEW...</html>";
                        break;

                    case SELECT:
                        hoverContent = "<html>Hovering on SELECT...</html>";
                        break;

                    default: hoverContent = "<html>GENERIC HOVER!!! :) :) :)<html>";
                }
                System.out.println("\n >> HP.getHover()  content = " + hoverContent);
                return hoverContent;
            } else {
            	System.out.print("HP.getHover() DID NOT FIND Token");
            }
        } catch (Exception e) {
            LOGGER.error("Error searching hover", e);
        }
        return "";
    }
}
