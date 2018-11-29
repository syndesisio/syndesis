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
package kudu.component.api;

import org.apache.kudu.Schema;
import org.apache.kudu.client.CreateTableOptions;

public class KuduApiTable {
    private Schema schema;
    private CreateTableOptions cto;
    private String name;

    public KuduApiTable( String name, Schema schema, CreateTableOptions cto) {
        this.name = name;
        this.schema = schema;
        this.cto = cto;
    }

    public String getName() {
        return name;
    }

    public CreateTableOptions getCto() {
        return cto;
    }

    public void setCto(CreateTableOptions cto) {
        this.cto = cto;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
