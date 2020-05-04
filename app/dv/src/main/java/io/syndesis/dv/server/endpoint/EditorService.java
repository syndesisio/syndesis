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
package io.syndesis.dv.server.endpoint;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.metadata.Schema;
import org.teiid.metadata.Table;
import org.teiid.query.validator.ValidatorReport;
import org.teiid.util.FullyQualifiedName;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.dv.metadata.MetadataInstance;
import io.syndesis.dv.metadata.MetadataInstance.ValidationResult;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.server.DvService;
import io.syndesis.dv.server.Messages;
import io.syndesis.dv.server.V1Constants;
import io.syndesis.dv.utils.StringUtils;

/**
 * A REST service for obtaining view information.
 */
@RestController
@RequestMapping(value=V1Constants.APP_PATH+V1Constants.FS+V1Constants.EDITORS_SEGMENT)
@Api( tags = {V1Constants.EDITORS_SEGMENT} )
public final class EditorService extends DvService {

    private static final String SUCCESS = "SUCCESS"; //$NON-NLS-1$

    private static final String ERROR = "ERROR"; //$NON-NLS-1$

    public static final String PREVIEW_VDB = "Preview"; //$NON-NLS-1$

    @Autowired
    private MetadataInstance metadataInstance;

    @Autowired
    private MetadataService metadataService;

    /**
     * Get the view editor state with the given id from the user's profile
     * @return a JSON document representing the view editor state in the user profile (never <code>null</code>)
     * @throws Exception
     */
    @RequestMapping(value = V1Constants.ID_PLACEHOLDER,
            method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Returns the view editor state with the given id",
                  response = ViewDefinition.class)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public RestViewDefinitionStatus getViewDefinition(
            @ApiParam(value = "Name of the view editor state to fetch", required = true)
            final @PathVariable(ID) String viewEditorStateId) throws Exception {
        return repositoryManager.runInTransaction(true, ()->{
            ViewDefinition viewEditorState = getWorkspaceManager().findViewDefinition(viewEditorStateId);
            LOGGER.debug( "getViewEditorState:found %d ViewEditorStates", //$NON-NLS-1$
                              viewEditorState == null ? 0 : 1 );

            if (viewEditorState == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            LOGGER.debug("getViewEditorStates:ViewEditorState %s entity was constructed", viewEditorState.getName()); //$NON-NLS-1$
            //for consistency/simplicity, just call validate - but it would be cheaper to lookup the validation
            //off of the vdb.  This return effectively mixes in the status/viewdefinition
            RestViewDefinitionStatus status = validateViewDefinition(viewEditorState);
            status.setViewDefinition(viewEditorState);
            return status;
        });
    }

    /**
     * upsert a view definition
     * @return saved/validate view definition
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation( value = "Store view editor state", response = StatusObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public RestViewDefinitionStatus upsertViewDefinition(
            @ApiParam(required = true) @RequestBody final io.syndesis.dv.model.ViewDefinition restViewEditorState)
            throws Exception {
        if (StringUtils.isBlank(restViewEditorState.getName())) {
            throw forbidden(Messages.Error.VIEW_DEFINITION_MISSING_NAME);
        }

        if (StringUtils.isBlank(restViewEditorState.getDataVirtualizationName())) {
            throw forbidden(Messages.Error.VIEW_DEFINITION_MISSING_DATAVIRTUALIZATIONNAME);
        }

        //validate before saving as it can save us a preview vdb refresh
        //TODO: further consolidation - that is we can go back to the logic that
        //had captured the inputs
        RestViewDefinitionStatus validated = validateViewDefinition(restViewEditorState);

        ViewDefinition vd = repositoryManager.runInTransaction(false, ()->{
            return upsertViewEditorState(restViewEditorState);
        });

        validated.setViewDefinition(vd);

        return validated;
    }

    public RestViewDefinitionStatus validateViewDefinition(
            @ApiParam(required = true) @RequestBody final ViewDefinition restViewDefinition) throws Exception {
        LOGGER.debug("Validating view : %s", restViewDefinition.getName()); //$NON-NLS-1$

        RestViewDefinitionStatus viewDefnStatus = new RestViewDefinitionStatus();

        String viewName = restViewDefinition.getName();
        String viewDdl = restViewDefinition.getDdl();

        if (StringUtils.isBlank(viewName)) {
            viewDefnStatus.setStatus(ERROR);
            viewDefnStatus.setMessage(Messages.getString(Messages.Error.VIEW_DEFINITION_MISSING_NAME));
            return viewDefnStatus;
        }

        if (StringUtils.isBlank(restViewDefinition.getDataVirtualizationName())) {
            viewDefnStatus.setStatus(ERROR);
            viewDefnStatus.setMessage(Messages.getString(Messages.Error.VIEW_DEFINITION_MISSING_DATAVIRTUALIZATIONNAME));
            return viewDefnStatus;
        }

        if (StringUtils.isBlank(viewDdl)) {
            viewDefnStatus.setStatus(ERROR);
            viewDefnStatus.setMessage(Messages.getString(Messages.Error.VIEW_DEFINITION_MISSING_DDL));
            return viewDefnStatus;
        }

        TeiidVdb vdb = metadataService.updatePreviewVdb(restViewDefinition.getDataVirtualizationName());

        if (vdb == null || !vdb.hasLoaded()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        ValidationResult result = vdb.validate(restViewDefinition.getDdl());
        ValidatorReport report = result.getReport();

        if (result.getMetadataException() != null) {
            viewDefnStatus.setStatus(ERROR);
            viewDefnStatus.setMessage("Metadata Error:" + result.getMetadataException().getMessage()); //$NON-NLS-1$
            return viewDefnStatus;
        }

        Table t = result.getSchema().getTables().get(viewName);

        if (definesMultipleObjects(result)) {
            viewDefnStatus.setStatus(ERROR);
            viewDefnStatus.setMessage("DDL defines more than one object"); //$NON-NLS-1$
            return viewDefnStatus;
        }

        // If names do not match, create an error status
        if(t == null) {
            String errorMsg = Messages.getString(Messages.Error.VALIDATE_VIEW_DEFINITION_NAME_MATCH_ERROR, viewName);
            viewDefnStatus.setStatus(ERROR);
            viewDefnStatus.setMessage(errorMsg);
        } else {
            restViewDefinition.getSourcePaths().clear();
            for (AbstractMetadataRecord r : t.getIncomingObjects()) {
                if (r instanceof Table) {
                    //TODO: should system stuff be filtered
                    FullyQualifiedName fqn = new FullyQualifiedName(Schema.getTypeName(), r.getParent().getName());
                    fqn.append(Schema.getChildType(Table.class), r.getName());
                    restViewDefinition.addSourcePath(fqn.toString());
                }
            }
            String error = report.getFailureMessage();
            if (report.hasItems() && !error.isEmpty()) {
                viewDefnStatus.setStatus(ERROR);
                viewDefnStatus.setMessage(error);
            } else {
                viewDefnStatus.setStatus(SUCCESS);
                viewDefnStatus.setMessage("View DDL was parsed/validated successfully"); //$NON-NLS-1$
            }
        }
        return viewDefnStatus;
    }

    private boolean definesMultipleObjects(ValidationResult result) {
        return result.getSchema().getTables().size() > 1
                || !result.getSchema().getProcedures().isEmpty()
                || !result.getSchema().getFunctions().isEmpty();
    }

    /**
     * Upserts the view editor state
     * @param restViewDefn the state
     * @return the ViewDefinition repo object
     * @throws Exception exception if a problem is encountered
     *
     * TODO: could refactor to directly save / merge, rather than copy
     */
    ViewDefinition upsertViewEditorState(final ViewDefinition restViewDefn) throws Exception {

        ViewDefinition viewDefn = null;

        if (restViewDefn.getId() != null) {
            viewDefn = getWorkspaceManager().findViewDefinition(restViewDefn.getId());
            if (viewDefn == null) {
                throw notFound(restViewDefn.getId());
            }
            if (restViewDefn.getVersion() == null) {
                //Optimistic locking not used
                LOGGER.warn("Version was not specificed for view definition, the latest state may be overwritten"); //$NON-NLS-1$
            } else if (!Objects.equals(restViewDefn.getVersion(), viewDefn.getVersion())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        } else {
            viewDefn = getWorkspaceManager().findViewDefinitionByNameIgnoreCase(restViewDefn.getDataVirtualizationName(), restViewDefn.getName());
        }

        boolean pathsSame = false;
        boolean updateDv = false;
        // Add a new ViewDefinition
        if (viewDefn == null) {
            updateDv = true;
            viewDefn = getWorkspaceManager().createViewDefiniton(restViewDefn.getDataVirtualizationName(), restViewDefn.getName());
        } else {
            if (!restViewDefn.getName().equals(viewDefn.getName()) || !restViewDefn.getDataVirtualizationName().equals(viewDefn.getDataVirtualizationName())) {
                throw new IllegalArgumentException("view name / dv name does not match the persistent state"); //$NON-NLS-1$
            }
            pathsSame = restViewDefn.getSourcePaths().equals(viewDefn.getSourcePaths());
            viewDefn.clearState();
        }

        String oldDdl = viewDefn.getDdl();
        // Set ViewDefinition of the ViewEditorState
        viewDefn.setDdl(restViewDefn.getDdl());

        viewDefn.setDescription(restViewDefn.getDescription());

        for (String restSourcePath: restViewDefn.getSourcePaths()) {
            viewDefn.addSourcePath(restSourcePath);
        }
        viewDefn.setComplete(restViewDefn.isComplete());
        viewDefn.setUserDefined(restViewDefn.isUserDefined());

        if (viewDefn.isComplete()) {
            if (!viewDefn.isUserDefined()) {
                //regenerate if needed
                if (viewDefn.getDdl() == null || !pathsSame || !viewDefn.isParsable()) {
                    String ddl = new ServiceVdbGenerator(metadataService).getODataViewDdl(viewDefn);
                    viewDefn.setDdl(ddl);
                    viewDefn.setParsable(true);
                    updateDv = true;
                }
                // else we're trusting the ui
            } else if (viewDefn.getDdl() != null && !Objects.equals(oldDdl, viewDefn.getDdl())) {

                //TODO: could pro-actively validate if we're in a good state
                viewDefn.getSourcePaths().clear();
                ValidationResult result = metadataInstance.parse(viewDefn.getDdl());

                //if there's partial metadata we can still work with that
                Table t = result.getSchema().getTables().get(viewDefn.getName());
                if (t != null && !definesMultipleObjects(result)) {
                    //TODO: it's not clear here what the user's intent is
                    //as they could have altered the description text box as well
                    String ddlDescr = t.getAnnotation();
                    if (ddlDescr != null) {
                        viewDefn.setDescription(ddlDescr);
                    }
                    if (result.getMetadataException() == null) {
                        viewDefn.setParsable(true);
                        //determine if this can just change the view definition
                        //for now we'll redo everything
                        updateDv = true;
                    }
                } else {
                    //not actually usable - perhaps come other ddl statement
                    if (viewDefn.isParsable()) {
                        updateDv = true;
                    }
                    viewDefn.setParsable(false);
                }
            }

            if (updateDv) {
                DataVirtualization dv = getWorkspaceManager().findDataVirtualization(viewDefn.getDataVirtualizationName());
                dv.touch();
            }
        }

        return viewDefn;
    }

    /**
     * @return a JSON document representing the results of the removal
     * @throws Exception
     */
    @RequestMapping(value = V1Constants.ID_PLACEHOLDER, method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation( value = "Remove a view editor state from the user's profile", response = StatusObject.class )
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred."),
        @ApiResponse(code = 204, message = "id not found")
    })
    public StatusObject removeViewDefinition(
            @ApiParam(value = "Id of the view editor state to remove", required = true)
            final @PathVariable(V1Constants.ID) String viewEditorStateId)
            throws Exception {
        return repositoryManager.runInTransaction(false, ()-> {
            ViewDefinition vd = getWorkspaceManager().findViewDefinition(viewEditorStateId);
            if (vd == null) {
                throw notFound(viewEditorStateId);
            }
            if (vd.isComplete() && vd.isParsable()) {
                DataVirtualization dv = getWorkspaceManager().findDataVirtualization(vd.getDataVirtualizationName());
                dv.touch();
            }

            getWorkspaceManager().deleteViewDefinition(viewEditorStateId);
            StatusObject kso = new StatusObject("Delete Status"); //$NON-NLS-1$
            kso.addAttribute(viewEditorStateId, "Successfully deleted"); //$NON-NLS-1$

            return kso;
        });
    }
}
