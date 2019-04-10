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
package io.syndesis.model2;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

@Entity(name = "ConfiguredConnector")
@Table(name = "syndesis_configured_connector")
@Data
public class ConfiguredConnector implements Serializable {

    private static final long serialVersionUID = -5012309870194352171L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String tags;
    @ManyToOne
    @JoinColumn(name = "connector_id")
    private Connector connector;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_instance_id")
    private Set<ActionInstance> actionInstances;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "configured_connection_id")
    private Set<ConfiguredConnectorProperty> configuredConnectorProperties;
}
