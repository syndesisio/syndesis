package io.syndesis.dv.metadata.internal;

import java.io.StringReader;

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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.teiid.metadata.MetadataFactory;
import org.teiid.metadata.MetadataRepository;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.TranslatorException;

import io.syndesis.dv.RepositoryManager;
import io.syndesis.dv.model.SourceSchema;

@Component
public class DDLDBMetadataRepository implements MetadataRepository<Object, Object> {

    public static final String TYPE_NAME = "DDLDB"; //$NON-NLS-1$

    @Autowired
    private TeiidServer teiidServer;
    @Autowired
    private RepositoryManager repositoryManager;

    @PostConstruct
    public void init() {
        teiidServer.addMetadataRepository(TYPE_NAME, this);
    }

    @Override
    public void loadMetadata(MetadataFactory factory,
            ExecutionFactory<Object, Object> executionFactory, Object connectionFactory,
            String text) throws TranslatorException {
        SourceSchema schema = repositoryManager.findSchemaBySourceId(text);
        if (schema != null && schema.getDdl() != null) {
            factory.parse(new StringReader(schema.getDdl()));
        }
    }

}
