/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.redhat.ipaas.api;

import java.io.Serializable;
import java.util.Set;

public class User implements Serializable{
    
    private static final long serialVersionUID = -8963536197984599474L;
    String id;
    String name;
    String kind;
    Set<Integration> integrations;
    
    static User getHardcodedUser() {
    	User user = new User();
    	user.id = "1";
    	user.name = "Clint Eastwood";
    	user.kind = "UsuallyNot";
    	return user;
    }
}
