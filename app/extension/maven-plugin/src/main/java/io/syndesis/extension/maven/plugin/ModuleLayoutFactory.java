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
package io.syndesis.extension.maven.plugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.LibraryScope;

final class ModuleLayoutFactory implements LayoutFactory {
    private static final Set<LibraryScope> LIB_DESTINATION_SCOPES = new HashSet<>(
        Arrays.asList(
            LibraryScope.COMPILE,
            LibraryScope.RUNTIME,
            LibraryScope.CUSTOM));

    private final boolean filterDestinationScopes;

    ModuleLayoutFactory(final boolean filterDestinationScopes) {
        this.filterDestinationScopes = filterDestinationScopes;
    }

    @Override
    public Layout getLayout(final File file) {
        return new Layout() {
            @Override
            public String getClassesLocation() {
                return null;
            }

            @Override
            public String getLauncherClassName() {
                return null;
            }

            @Override
            public String getLibraryDestination(final String libraryName, final LibraryScope scope) {
                if (!filterDestinationScopes || LIB_DESTINATION_SCOPES.contains(scope)) {
                    return "lib/";
                }

                return null;
            }

            @Override
            public boolean isExecutable() {
                return false;
            }
        };
    }
}
