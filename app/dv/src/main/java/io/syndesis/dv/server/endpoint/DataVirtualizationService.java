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

import static io.syndesis.dv.server.Messages.Error.DATASERVICE_SERVICE_SERVICE_NAME_ERROR;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.dv.KException;
import io.syndesis.dv.StringConstants;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.model.Edition;
import io.syndesis.dv.model.SourceSchema;
import io.syndesis.dv.model.TablePrivileges;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.model.export.v1.DataVirtualizationV1Adapter;
import io.syndesis.dv.model.export.v1.SourceV1;
import io.syndesis.dv.model.export.v1.TablePrivilegeV1Adapter;
import io.syndesis.dv.model.export.v1.ViewDefinitionV1Adapter;
import io.syndesis.dv.openshift.BuildStatus;
import io.syndesis.dv.openshift.BuildStatus.RouteStatus;
import io.syndesis.dv.openshift.BuildStatus.Status;
import io.syndesis.dv.openshift.DeploymentStatus;
import io.syndesis.dv.openshift.ProtocolType;
import io.syndesis.dv.openshift.PublishConfiguration;
import io.syndesis.dv.openshift.SyndesisHttpClient;
import io.syndesis.dv.openshift.TeiidOpenShiftClient;
import io.syndesis.dv.openshift.VirtualizationStatus;
import io.syndesis.dv.server.DvService;
import io.syndesis.dv.server.Messages;
import io.syndesis.dv.server.SSOConfigurationProperties;
import io.syndesis.dv.server.V1Constants;
import io.syndesis.dv.server.endpoint.RoleInfo.Operation;
import io.syndesis.dv.utils.PathUtils;
import io.syndesis.dv.utils.StringNameValidator;
import io.syndesis.dv.utils.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.core.util.Assertion;
import org.teiid.metadata.Schema;
import org.teiid.metadata.Table;
import org.teiid.util.FullyQualifiedName;

/**
 * A REST service for obtaining virtualization information.
 */
@RestController
@RequestMapping(value = V1Constants.APP_PATH
        + StringConstants.FS + V1Constants.VIRTUALIZATIONS_SEGMENT)
@Api(tags = { V1Constants.VIRTUALIZATIONS_SEGMENT })
@SuppressWarnings("PMD.ExcessiveClassLength") // TODO refactor
public final class DataVirtualizationService extends DvService {

    private static final String DV_JSON = "dv.json"; //$NON-NLS-1$

    private static final String DV_VDB_XML = "dv-vdb.xml"; //$NON-NLS-1$

    /**
     * To be a valid schema name we don't allow .
     * Since we'll add the dv- prefix, we don't char what it starts with,
     * but we're still required to end with a letter/number
     */
    private static final Pattern DATAVIRTUALIZATION_PATTERN = Pattern.compile("[-a-z0-9]*[a-z0-9]", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

    private static final StringNameValidator VALIDATOR = new StringNameValidator();

    @Autowired
    private TeiidOpenShiftClient openshiftClient;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private EditorService utilService;

    @Autowired
    private SSOConfigurationProperties ssoConfigurationProperties;

    /**
     * Get the virtualizations from the repository
     * @return a JSON document representing all the virtualizations
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, produces= { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Return the collection of data services",
        response = RestDataVirtualization.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 403, message = "An error has occurred.") })
    public List<RestDataVirtualization> getDataVirtualizations() {

        Iterable<? extends DataVirtualization> virtualizations = repositoryManager.runInTransaction(true, ()->{
            return getWorkspaceManager().findDataVirtualizations();
        });

        final List<RestDataVirtualization> entities = new ArrayList<>();
        for (final DataVirtualization virtualization : virtualizations) {
            RestDataVirtualization entity = createRestDataVirtualization(virtualization);
            entities.add(entity);
        }
        return entities;
    }

    private RestDataVirtualization createRestDataVirtualization(final DataVirtualization virtualization) {
        RestDataVirtualization entity = new RestDataVirtualization(virtualization);

        //working state
        entity.setEmpty(this.getWorkspaceManager().findViewDefinitionsNames(virtualization.getName()).isEmpty());
        entity.setEditionCount(this.getWorkspaceManager().getEditionCount(virtualization.getName()));
        entity.setSecured(this.getWorkspaceManager().hasRoles(virtualization.getName()));

        // Set published status of virtualization
        VirtualizationStatus status = this.openshiftClient.getVirtualizationStatus(virtualization.getName());
        if (status != null) { //can be null for unit tests
            BuildStatus buildStatus = status.getBuildStatus();
            DeploymentStatus deploymentStatus = status.getDeploymentStatus();

            //publish/build state
            entity.setPublishPodName(buildStatus.getPublishPodName());
            entity.setPodNamespace(buildStatus.getNamespace());
            entity.setPublishedRevision(buildStatus.getVersion());

            if (buildStatus.getStatus() == Status.COMPLETE) {
                entity.setPublishedState(deploymentStatus.getStatus().name());
                entity.setPublishedMessage(deploymentStatus.getStatusMessage());
            } else {
                entity.setPublishedState(buildStatus.getStatus().name());
                entity.setPublishedMessage(buildStatus.getStatusMessage());
            }

            //deployment state
            entity.setOdataHostName(getOdataHost(deploymentStatus));
            entity.setUsedBy(deploymentStatus.getUsedBy());
            entity.setDeployedRevision(deploymentStatus.getVersion());
            entity.setDeployedState(deploymentStatus.getStatus().name());
            entity.setDeployedMessage(deploymentStatus.getStatusMessage());
        }

        return entity;
    }

    /**
     * @param virtualization the name of the virtualization being retrieved (cannot be empty)
     * @return the JSON representation of the virtualization (never <code>null</code>)
     * @throws Exception
     */
    @RequestMapping(value = V1Constants.VIRTUALIZATION_PLACEHOLDER, method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Find virtualization by name", response = RestDataVirtualization.class)
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No virtualization could be found with name"),
            @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public RestDataVirtualization getDataVirtualization(
            @ApiParam(value = "name of the virtualization to be fetched - ignoring case",
            required = true) final @PathVariable(VIRTUALIZATION) String virtualization) {

        DataVirtualization dv = repositoryManager.runInTransaction(true, () -> {
             return getWorkspaceManager().findDataVirtualizationByNameIgnoreCase(virtualization);
        });

        if (dv == null) {
            String validationMessage = getValidationMessage(virtualization);
            if (validationMessage != null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, validationMessage);
            }

            // check for duplicate name
            final boolean inUse = repositoryManager.runInTransaction(true, () -> {
                //from the pattern validation, there's no escaping necessary
                return getWorkspaceManager().isNameInUse(virtualization);
            });

            // name is a duplicate
            if (inUse) {
                //we should be setting a location header here, but we don't really care about the redirection
                throw new ResponseStatusException(HttpStatus.SEE_OTHER, "the name matches an existing connection name"); //$NON-NLS-1$
            }

            throw notFound( virtualization );
        }

        return createRestDataVirtualization(dv);
    }

    /**
     * Create a new virtualization in the repository
     *
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.POST,
            produces= { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Create a virtualization")
    @ApiResponses(value = { @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    @SuppressWarnings("PMD.PreserveStackTrace") // no way to preserve
    public ResponseEntity<String> createDataVirtualization(
            @ApiParam(required = true) @RequestBody final RestDataVirtualization restDataVirtualization) {

        final String restName = restDataVirtualization.getName();
        // Error if the name is missing from the supplied json body
        if (StringUtils.isBlank(restName)) {
            throw forbidden(Messages.Error.DATASERVICE_SERVICE_MISSING_NAME);
        }

        String message = getValidationMessage(restName);
        if (message != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }

        // create new virtualization
        try {
            return repositoryManager.runInTransaction(false, () -> {
                final DataVirtualization dv = getWorkspaceManager().createDataVirtualization(restName);
                dv.setDescription(restDataVirtualization.getDescription());
                return ResponseEntity.ok(restName + " Successfully created"); //$NON-NLS-1$
            });
        } catch (DataIntegrityViolationException ignored) {
            throw error(HttpStatus.CONFLICT, Messages.Error.DATASERVICE_SERVICE_CREATE_ALREADY_EXISTS);
        }
    }

    /**
     * Delete the specified restDv from the repository
     *
     * @param virtualization the name of the virtualization to remove (cannot be <code>null</code>)
     * @return a JSON document representing the results of the removal
     * @throws Exception
     */
    @RequestMapping(value = V1Constants.VIRTUALIZATION_PLACEHOLDER, method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Delete a virtualization")
    @ApiResponses(value = { @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public StatusObject deleteDataVirtualization(@ApiParam(value = "Name of the virtualization to be deleted", required = true) final @PathVariable(VIRTUALIZATION) String virtualization) {

        StatusObject kso = repositoryManager.runInTransaction(false, ()->{
            // Delete the virtualization. The view definitions will cascade
            if (!repositoryManager.deleteDataVirtualization(virtualization)) {
                throw notFound(virtualization);
            }

            StatusObject status = new StatusObject("Delete Status"); //$NON-NLS-1$
            status.addAttribute(virtualization, "Successfully deleted"); //$NON-NLS-1$
            return status;
        });

        //deleted/txn committed, update runtime
        //there is a small chance that a dv with the same name was recreated in the meantime,
        //but since this vdb is created on-demand we're good
        try {
            metadataService.removeVdb(DataVirtualization.getPreviewVdbName(virtualization));
        } catch (KException e) {
            LOGGER.debug("error removing preview vdb", e); //$NON-NLS-1$
        }
        return kso;
    }

    private static String getValidationMessage(final String virtualization) {
        final String errorMsg = VALIDATOR.checkValidName(virtualization);

        if (errorMsg != null) {
            return errorMsg;
        }

        if (!DATAVIRTUALIZATION_PATTERN.matcher(virtualization).matches()) {
            return "Must match pattern " + DATAVIRTUALIZATION_PATTERN.pattern(); //$NON-NLS-1$
        }

        TreeSet<String> taken = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        taken.addAll(ModelMetaData.getReservedNames());

        if (taken.contains(virtualization)) {
            return virtualization + " is a reserved name."; //$NON-NLS-1$
        }

        return null;
    }

    @RequestMapping(value = StringConstants.FS + V1Constants.VIRTUALIZATION_PLACEHOLDER +
            StringConstants.FS + V1Constants.IMPORT + StringConstants.FS
            + V1Constants.TEIID_SOURCE_PLACEHOLDER, method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Import views from a given source", response = StatusObject.class)
    @ApiResponses(value = { @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public StatusObject importViews(@ApiParam(value = "Name of the virtualization", required = true)
            final @PathVariable(V1Constants.VIRTUALIZATION)
            String virtualization,

            @ApiParam( value = "Name of the teiid source", required = true )
            final @PathVariable(V1Constants.TEIID_SOURCE)
            String teiidSourceName,

            @ApiParam(value = "Import Payload", required = true)
            @RequestBody
            final ImportPayload importPayload) {

        return repositoryManager.runInTransaction(false, () -> {
            DataVirtualization dataservice = getWorkspaceManager().findDataVirtualization(virtualization);
            if (dataservice == null) {
                throw notFound( virtualization );
            }

            Schema s = metadataService.findConnectionSchema(teiidSourceName);

            if (s == null) {
                throw notFound( teiidSourceName );
            }

            ServiceVdbGenerator serviceVdbGenerator = new ServiceVdbGenerator(metadataService);

            List<ViewDefinition> toSave = new ArrayList<>();
            for (String name : importPayload.getTables()) {
                Table t = s.getTable(name);
                if (t == null) {
                    //could be an error/warning
                    continue;
                }

                ViewDefinition viewDefn = getWorkspaceManager().findViewDefinitionByNameIgnoreCase(virtualization, name);
                if (viewDefn != null) {
                    //sanity check
                    if (!name.equalsIgnoreCase(viewDefn.getName())) {
                        throw new AssertionError("imported view name conflicts with an existing view name");
                    }

                    //reuse the same id
                    viewDefn.clearState();
                    viewDefn.setUserDefined(false);
                    viewDefn.setDdl(null);
                    viewDefn.setDescription(null);
                } else {
                    viewDefn = new ViewDefinition(virtualization, name);
                }
                viewDefn.setComplete(true);
                FullyQualifiedName fqn = new FullyQualifiedName(Schema.getTypeName(), teiidSourceName);
                fqn.append(Schema.getChildType(t.getClass()), t.getName());
                viewDefn.addSourcePath(fqn.toString());

                String ddl = serviceVdbGenerator.getODataViewDdl(viewDefn);
                viewDefn.setDdl(ddl);
                viewDefn.setParsable(true);
                toSave.add(viewDefn);
            }

            StatusObject result = new StatusObject("Import Status"); //$NON-NLS-1$
            for (ViewDefinition vd : getWorkspaceManager().saveAllViewDefinitions(toSave)) {
                result.addAttribute(vd.getName(), vd.getId());
            }

            dataservice.touch();

            return result;
        });
    }

    /**
     * Get OData hostname from the deployment status
     * @param status
     * @return the odata hostname
     */
    private static String getOdataHost(final DeploymentStatus status) {
        String odataHost = null;
        List<RouteStatus> routeStatuses = status.getRoutes();
        if(!routeStatuses.isEmpty()) {
            // Find Odata route if it exists
            for(RouteStatus routeStatus: routeStatuses) {
                if(routeStatus.getProtocol() == ProtocolType.ODATA) {
                    odataHost = routeStatus.getHost();
                    break;
                }
            }
        }
        return odataHost;
    }

    /**
     * Update the specified virtualization from the repository
     * @param virtualization the virtualization name (cannot be empty)
     * @return a JSON representation of the new connection (never <code>null</code>)
     * @throws Exception
     */
    @RequestMapping(value = StringConstants.FS + V1Constants.VIRTUALIZATION_PLACEHOLDER, method = RequestMethod.PUT, produces = {
            MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Update data service")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "An error has occurred.") })
    public StatusObject updateDataVirtualization(
            @ApiParam(value = "Name of the data service", required = true)
            final @PathVariable(V1Constants.VIRTUALIZATION) String virtualization,
            @ApiParam(required = true) @RequestBody final RestDataVirtualization restDataVirtualization) {

        final String restName = restDataVirtualization.getName();
        // Error if the name is missing from the supplied json body
        if (StringUtils.isBlank(restName)) {
            throw forbidden(Messages.Error.DATASERVICE_SERVICE_MISSING_NAME);
        }

        // Error if the name parameter is different than JSON name
        final boolean namesMatch = virtualization.equals(restName);
        if (!namesMatch) {
            throw forbidden(DATASERVICE_SERVICE_SERVICE_NAME_ERROR, virtualization, restName);
        }

        return repositoryManager.runInTransaction(false, () -> {
            // Error if the repo already contains a virtualization with the supplied name.
            DataVirtualization existing = getWorkspaceManager().findDataVirtualization(restDataVirtualization.getName());
            if (existing == null) {
                throw notFound( virtualization );
            }

            existing.setDescription(restDataVirtualization.getDescription());
            StatusObject kso = new StatusObject("Update Dataservice Status"); //$NON-NLS-1$
            kso.addAttribute(virtualization, "Dataservice successfully updated"); //$NON-NLS-1$

            return kso;
        });
    }

    /**
     * Export the virtualization to a zip file
     * @param virtualization
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {VIRTUALIZATION_PLACEHOLDER + StringConstants.FS + "export",
            VIRTUALIZATION_PLACEHOLDER + StringConstants.FS + "export" + StringConstants.FS + REVISION_PLACEHOLDER}, method = RequestMethod.GET, produces = {
            MediaType.MULTIPART_FORM_DATA_VALUE })
    @ApiOperation(value = "Export virtualization by name.  Without a revision number, the current working state is exported.", response = RestDataVirtualization.class)
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No virtualization could be found with name"),
            @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public ResponseEntity<StreamingResponseBody> exportDataVirtualization(
            @ApiParam(value = "name of the virtualization",
            required = true) final @PathVariable(VIRTUALIZATION) String virtualization,
            @ApiParam(value = "revision number",
            required = false) final @PathVariable(required = false, name = REVISION) Long revision)
            {

        StreamingResponseBody result = repositoryManager.runInTransaction(true, () -> {
            DataVirtualization dv = getWorkspaceManager().findDataVirtualization(virtualization);

            if (dv == null) {
                throw notFound(virtualization);
            }

            if (revision != null) {
                Edition e = repositoryManager.findEdition(virtualization, revision);
                if (e == null) {
                    throw notFound(String.valueOf(revision));
                }

                byte[] bytes = repositoryManager.findEditionExport(e);
                Assertion.isNotNull(bytes);

                return out -> {
                    out.write(bytes);
                };
            }

            return createExportStream(dv, null, null);
        });


        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\""+virtualization+(revision!=null?"-"+revision:"")+"-export.zip\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        return new ResponseEntity<StreamingResponseBody>(result, headers, HttpStatus.OK);
    }

    /**
     * Create an export of the current workspace.  Optionally including the full vdb.
     * @param dv
     * @param theVdb
     * @return
     * @throws KException
     */
    private StreamingResponseBody createExportStream(DataVirtualization dv, VDBMetaData theVdb, Long revision)
            {
        DataVirtualizationV1Adapter adapter = new DataVirtualizationV1Adapter(dv);

        List<? extends ViewDefinition> views = getWorkspaceManager().findViewDefinitions(dv.getName());

        Map<String, SourceV1> sources = new LinkedHashMap<>();

        for (ViewDefinition view : views) {
            adapter.getViews().add(new ViewDefinitionV1Adapter(view));
            for (String path : view.getSourcePaths()) {
                String connection = PathUtils.getOptions(path).get(0).getSecond();
                if (sources.containsKey(connection)) {
                    continue;
                }
                TeiidDataSource tds = this.metadataService.findTeiidDatasource(connection);
                if (tds != null) {
                    SourceV1 source = new SourceV1();
                    source.setSourceId(tds.getSyndesisId());
                    source.setName(tds.getName());
                    sources.put(connection, source);
                }
            }
        }

        List<TablePrivileges> tablePrivileges = getWorkspaceManager().findAllTablePrivileges(dv.getName());
        for (TablePrivileges privileges : tablePrivileges) {
            adapter.getTablePriveleges().add(new TablePrivilegeV1Adapter(privileges));
        }

        adapter.setSources(new ArrayList<>(sources.values()));

        return out -> {
            ZipOutputStream zos = new ZipOutputStream(out);

            JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            ObjectMapper mapper = new ObjectMapper(jsonFactory);

            zos.putNextEntry(new ZipEntry(DV_JSON));
            mapper.writerWithDefaultPrettyPrinter().writeValue(zos, adapter);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("dv-info.json")); //$NON-NLS-1$
            String json = String.format("{\"exportVersion\":%s,%n" //$NON-NLS-1$
                    + "\"revision\":%s,%n" //$NON-NLS-1$
                    + "\"entityVersion\":%s}", 1, //$NON-NLS-1$
                    revision == null ? "\"draft\"" : revision, dv.getVersion()); //$NON-NLS-1$
            zos.write(json.getBytes("UTF-8")); //$NON-NLS-1$
            zos.closeEntry();

            if (theVdb != null) {
                zos.putNextEntry(new ZipEntry(DV_VDB_XML));
                try {
                    VDBMetadataParser.marshall(theVdb, zos);
                } catch (XMLStreamException e) {
                    throw new IOException(e);
                }
                //the marshal closes automatically
                //zos.closeEntry();
            }

            zos.close();
        };
    }

    /**
     * Import a virtualization from a zip file
     * @param virtualization
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping()
    @ApiOperation(value = "Import a single data virtualization", response = StatusObject.class)
    public StatusObject importDataVirtualization(@ApiParam(value = "name of the virtualization")
            @RequestParam(name="virtualization", required=false) String virtualization,
            @RequestParam("file") MultipartFile file) throws IOException {

        return importDataVirtualization(virtualization, file, true);
    }

    @SuppressWarnings({"PMD.NPathComplexity", "PMD.PreserveStackTrace"}) // TODO refactor
    private StatusObject importDataVirtualization(String virtualization,
            InputStreamSource file, boolean createVirtualization) throws IOException {
        final DataVirtualizationV1Adapter dv;
        try (InputStream is = file.getInputStream();) {
            ZipInputStream zis = new ZipInputStream(is);

            ZipEntry ze = zis.getNextEntry();
            while (ze != null && !ze.getName().equals(DV_JSON)) {
                ze = zis.getNextEntry();
            }
            if (ze == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "zip does not contain dv.json"); //$NON-NLS-1$
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            dv = mapper.readValue(zis, DataVirtualizationV1Adapter.class);
        }

        final String dvName;
        DataVirtualization toImport = dv.getEntity();
        if (virtualization == null) {
            dvName = toImport.getName();
        } else {
            dvName = virtualization;
            toImport.setName(virtualization);
        }

        try {
            return repositoryManager.runInTransaction(false, () -> {
                StatusObject status = new StatusObject("import result"); //$NON-NLS-1$
                for (SourceV1 source : dv.getSources()) {
                    TeiidDataSource tds = metadataService.findTeiidDatasource(source.getName());
                    if (tds == null) {
                        //nothing with this name, check by id
                        SourceSchema schema = getWorkspaceManager().findSchemaBySourceId(source.getSourceId());
                        if (schema != null) {
                            status.addAttribute(source.getName(), "a syndesis connection exists with the given id, but does not match the name"); //$NON-NLS-1$
                        } else {
                            status.addAttribute(source.getName(), "no syndesis connection can be found"); //$NON-NLS-1$
                        }
                    } else {
                        if (!tds.getSyndesisId().equals(source.getSourceId())) {
                            status.addAttribute(source.getName(), "a syndesis connection with the same name exists, but the ids do not match"); //$NON-NLS-1$
                        }
                    }
                }
                DataVirtualization existing = null;
                if (createVirtualization) {
                    createDataVirtualization(new RestDataVirtualization(toImport));
                } else {
                    //revert
                    repositoryManager.deleteViewDefinitions(dv.getName());
                    existing = repositoryManager.findDataVirtualization(dv.getName());
                    if (existing == null) {
                        throw notFound(dv.getName());
                    }
                    existing.setDescription(dv.getDescription());
                }

                Map<String, String> viewMap = new HashMap<String, String>();

                //TODO: validate the uuid or assign a new one
                //for now we just assign new to all objects
                for (ViewDefinitionV1Adapter adapter : dv.getViews()) {
                    ViewDefinition vd = adapter.getEntity();
                    String oldId = vd.getId();
                    vd.setId(null);
                    vd.setDataVirtualizationName(dvName);
                    String newId = utilService.upsertViewEditorState(vd).getId();
                    viewMap.put(oldId, newId);
                }

                for (TablePrivilegeV1Adapter adapter : dv.getTablePriveleges()) {
                    String id = viewMap.get(adapter.getViewDefinitionId());
                    TablePrivileges privileges = repositoryManager.createTablePrivileges(id, adapter.getRole());
                    privileges.setGrantPrivileges(adapter.getGrantPrivileges());
                }

                if (existing != null) {
                    existing.setModified(false);
                }
                return status;
            });
        } catch (DataIntegrityViolationException e) {
            throw error(HttpStatus.CONFLICT, Messages.Error.DATASERVICE_SERVICE_CREATE_ALREADY_EXISTS);
        }
    }

    /**
     * Get all view editor states from the user's profile
     * @return a JSON document representing the view editor states in the user profile (never <code>null</code>)
     * @throws Exception
     */
    @RequestMapping( value = V1Constants.VIRTUALIZATION_PLACEHOLDER + StringConstants.FS + VIEWS_SEGMENT, method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Returns the view listings",
    response = ViewDefinition.class)
    @ApiResponses( value = {
            @ApiResponse( code = 400, message = "The URI cannot contain encoded slashes or backslashes." ),
            @ApiResponse( code = 403, message = "An unexpected error has occurred." ),
            @ApiResponse( code = 404, message = "No view could be found with name" )
    } )
    public List<ViewListing> getViewList(
            @ApiParam(value = "Name of the virtualization", required = true)
            final @PathVariable(VIRTUALIZATION) String virtualization) {
        // find view editor states
        return repositoryManager.runInTransaction(true, ()->{

            final List<? extends ViewDefinition> viewEditorStates = getWorkspaceManager().findViewDefinitions( virtualization );
            LOGGER.debug( "getViewEditorStates:found %d ViewEditorStates", viewEditorStates.size() ); //$NON-NLS-1$

            //TODO: paging / sorting can be pushed into the repository

            return createViewList(virtualization, viewEditorStates);
        });
    }

    private List<ViewListing> createViewList(final String virtualization,
            final List<? extends ViewDefinition> viewEditorStates) {
        TeiidVdb vdb = null;

        ArrayList<ViewListing> result = new ArrayList<>();

        for ( final ViewDefinition viewEditorState : viewEditorStates ) {
            ViewListing listing = new ViewListing();
            listing.setId(viewEditorState.getId());
            listing.setName(viewEditorState.getName());
            listing.setDescription(viewEditorState.getDescription());
            if (viewEditorState.isParsable()) {
                if (vdb == null) {
                    vdb = metadataService.updatePreviewVdb(virtualization);
                }
                listing.setValid(!vdb.hasValidationError(viewEditorState.getDataVirtualizationName(), viewEditorState.getName(), Schema.getChildType(Table.class)));
            } else {
                listing.setValid(false);
            }
            listing.setTablePrivileges(repositoryManager.findTablePrivileges(viewEditorState.getId()));
            result.add(listing);
        }
        return result;
    }

    @RequestMapping( value = V1Constants.VIRTUALIZATION_PLACEHOLDER + StringConstants.FS + VIEWS_SEGMENT + StringConstants.FS + VIEW_PLACEHOLDER, method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Returns the view listing with the given name",
    response = ViewDefinition.class)
    @ApiResponses( value = {
            @ApiResponse( code = 400, message = "The URI cannot contain encoded slashes or backslashes." ),
            @ApiResponse( code = 403, message = "An unexpected error has occurred." ),
            @ApiResponse( code = 404, message = "No view could be found with name" )
    } )
    public ViewListing getViewListing(
                                      @ApiParam(value = "Name of the virtualization", required = true)
                                      final @PathVariable(VIRTUALIZATION) String virtualization,
                                      @ApiParam(value = "Name of the view - not case sensitive", required = true)
                                      final @PathVariable(VIEW_NAME) String viewName ) {

        final String errorMsg = VALIDATOR.checkValidName( viewName );

        // a name validation error occurred
        if ( errorMsg != null ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMsg);
        }

        ViewDefinition vd = repositoryManager.runInTransaction(true, ()-> {
            return getWorkspaceManager().findViewDefinitionByNameIgnoreCase(virtualization, viewName);
        });

        if (vd == null) {
            throw notFound(viewName);
        }

        return createViewList(virtualization, Arrays.asList(vd)).get(0);
    }

    @RequestMapping(value = V1Constants.PUBLISH + StringConstants.FS
            + V1Constants.VIRTUALIZATION_PLACEHOLDER, method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Delete Virtualization Service by virtualization name",response = BuildStatus.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No virtualization could be found with name"),
        @ApiResponse(code = 406, message = "Only JSON returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public BuildStatus deletePublishedVirtualization(
            @ApiParam(value = "Name of the virtualization")
            final @PathVariable(value = "virtualization", required = true) String virtualization){
        return this.openshiftClient.deleteVirtualization(virtualization);
    }

    @RequestMapping(value = V1Constants.PUBLISH, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Publish Virtualization Service", response = StatusObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No Dataservice could be found with name"),
        @ApiResponse(code = 406, message = "Only JSON returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    @SuppressWarnings("PMD.NPathComplexity") // TODO refactor
    public StatusObject publishVirtualization(
            @ApiParam(value = "JSON properties:<br>" + StringConstants.OPEN_PRE_TAG + StringConstants.OPEN_BRACE + StringConstants.BR + StringConstants.NBSP
                    + "\"name\":      \"Name of the Dataservice\"" + StringConstants.BR
                    + "\"cpu-units\": \"(optional) Number of CPU units to allocate. 100 is 0.1 CPU (default 500)\"" + StringConstants.BR
                    + "\"memory\":    \"(optional) Amount memory to allocate in MB (default 1024)\"" + StringConstants.BR
                    + "\"disk-size\": \"(optional) Amount disk allocated in GB (default 20)\"" + StringConstants.BR
                    + "\"enable-odata\": \"(optional) Enable OData interface. true|false (default true)\"" + StringConstants.BR
                    + StringConstants.CLOSE_BRACE
                    + StringConstants.CLOSE_PRE_TAG) @RequestBody(required = true) final PublishRequestPayload payload) {
        //
        // Error if there is no name attribute defined
        //
        if (payload.getName() == null) {
            throw forbidden(Messages.Error.VDB_NAME_NOT_PROVIDED);
        }

        PublishConfiguration config = new PublishConfiguration();
        //for now everyone uses the same

        StatusObject status = new StatusObject();
        repositoryManager.runInTransaction(false, ()-> {
            DataVirtualization dataservice = getWorkspaceManager().findDataVirtualization(payload.getName());
            if (dataservice == null) {
                throw notFound(payload.getName());
            }

            List<? extends ViewDefinition> editorStates = getWorkspaceManager().findViewDefinitions(dataservice.getName());

            //check for unparsable - alternatively we could put this on the preview vdb
            for (ViewDefinition vd : editorStates) {
                if (vd.isComplete() && !vd.isParsable()) {
                    status.addAttribute("error", vd.getName() + " is not parsable");  //$NON-NLS-1$ //$NON-NLS-2$
                    return status;
                }
            }

            TeiidVdb vdb = metadataService.updatePreviewVdb(dataservice.getName());

            if (vdb == null || !vdb.hasLoaded()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
            }

            List<String> errors = vdb.getValidityErrors();
            if (!errors.isEmpty()) {
                status.addAttribute("error", errors.iterator().next());  //$NON-NLS-1$
                return status;
            }

            status.addAttribute("Publishing", "Operation initiated");  //$NON-NLS-1$//$NON-NLS-2$

            List<TablePrivileges> tablePrivileges = repositoryManager.findAllTablePrivileges(dataservice.getName());

            //use the preview vdb to build the needed metadata
            VDBMetaData theVdb = new ServiceVdbGenerator(metadataService).createServiceVdb(dataservice.getName(), vdb, editorStates, tablePrivileges);

            //create a new published edition with the saved workspace state
            Edition edition = repositoryManager.createEdition(dataservice.getName());

            StreamingResponseBody stream = createExportStream(dataservice, theVdb, edition.getRevision());
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                stream.writeTo(baos);
                repositoryManager.saveEditionExport(edition, baos.toByteArray());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            dataservice.setModified(false); //once we've published, we're not modified

            config.setVDB(theVdb);
            config.setPublishedRevision(edition.getRevision());

            status.addAttribute(REVISION, String.valueOf(edition.getRevision()));
            //
            // Return the status from this request. Otherwise, monitor using #getVirtualizations()
            //
            return status;
        });

        if (config.getVDB() != null) {
            //outside of the txn call to the openshift client
            submitPublish(config, status);
        }

        return status;
    }

    private void submitPublish(PublishConfiguration config, StatusObject status)
            {
        if (config.isSecurityEnabled()) {
            if (ssoConfigurationProperties.getAuthServerUrl() == null) {
                //error
                status.addAttribute("Build Status", BuildStatus.Status.FAILED.name()); //$NON-NLS-1$
                status.addAttribute("Build Status Message", "Virtualization is configured for SSO integration, but SSO is not configured"); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            config.setSsoConfigurationProperties(ssoConfigurationProperties);
        }


        BuildStatus buildStatus = openshiftClient.publishVirtualization(config);

        status.addAttribute("OpenShift Name", buildStatus.getOpenShiftName()); //$NON-NLS-1$
        status.addAttribute("Build Status", buildStatus.getStatus().name()); //$NON-NLS-1$
        status.addAttribute("Build Status Message", buildStatus.getStatusMessage()); //$NON-NLS-1$
    }

    /**
     * Get the editions from the repository
     * @return a JSON document representing all the editions
     * @throws Exception
     */
    @GetMapping(value = V1Constants.PUBLISH + StringConstants.FS
            + VIRTUALIZATION_PLACEHOLDER, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Return the collection of editions", response = Edition.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public List<Edition> getEditions(
            @ApiParam(value = "Name of the virtualization", required = true) final @PathVariable(VIRTUALIZATION) String virtualization)
            {
        return repositoryManager.runInTransaction(false, () -> {
            return repositoryManager
                    .findEditions(virtualization);
        });
    }

    /**
     * Get a single edition
     * @param virtualization
     * @param revision
     * @return
     * @throws Exception
     *
     * TODO: there's not yet more detail here than what is in the list
     */
    @GetMapping(value = V1Constants.PUBLISH + StringConstants.FS + VIRTUALIZATION_PLACEHOLDER
            + StringConstants.FS + REVISION_PLACEHOLDER, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Return an edition", response = Edition.class)
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public Edition getEdition(
            @ApiParam(value = "Name of the virtualization", required = true) final @PathVariable(VIRTUALIZATION) String virtualization,
            @ApiParam(value = "Revision number", required = true) final @PathVariable(REVISION) long revision)
            {
        return repositoryManager.runInTransaction(false, () -> {
            Edition e = repositoryManager.findEdition(virtualization, revision);

            if (e == null) {
                throw notFound(virtualization + " " + revision); //$NON-NLS-1$
            }

            return e;
        });
    }

    /**
     * Start (re-publish) the given revision
     * @param virtualization
     * @param revision
     * @return
     * @throws Exception
     */
    @PostMapping(value = V1Constants.PUBLISH + StringConstants.FS + VIRTUALIZATION_PLACEHOLDER
            + StringConstants.FS + REVISION_PLACEHOLDER + StringConstants.FS + START, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Start an edition", response = StatusObject.class)
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public StatusObject startEdition(
            @ApiParam(value = "Name of the virtualization to be deleted", required = true) final @PathVariable(VIRTUALIZATION) String virtualization,
            @ApiParam(value = "Revision number", required = true) final @PathVariable(REVISION) long revision)
            {
         PublishConfiguration publishConfig = repositoryManager.runInTransaction(true, () -> {
            Edition e = repositoryManager.findEdition(virtualization, revision);

            if (e == null) {
                throw notFound(virtualization + " " + revision); //$NON-NLS-1$
            }

            byte[] bytes = repositoryManager.findEditionExport(e);
            Assertion.isNotNull(bytes);

            final VDBMetaData theVdb;
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {

                ZipEntry ze = zis.getNextEntry();
                while (ze != null && !ze.getName().equals(DV_VDB_XML)) {
                    ze = zis.getNextEntry();
                }
                Assertion.isNotNull(ze);

                theVdb = VDBMetadataParser.unmarshall(zis);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            } catch (XMLStreamException xse) {
                throw new IllegalArgumentException("Unable to unmarshall VDB meta data", xse);
            }

            PublishConfiguration config = new PublishConfiguration();
            config.setVDB(theVdb);
            config.setPublishedRevision(e.getRevision());
            return config;
        });
        StatusObject status = new StatusObject();
        submitPublish(publishConfig, status);
        return status;
    }

    @PostMapping(value = V1Constants.PUBLISH + StringConstants.FS + VIRTUALIZATION_PLACEHOLDER
            + StringConstants.FS + REVISION_PLACEHOLDER + StringConstants.FS + REVERT, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Revert to an edition", response = StatusObject.class)
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public StatusObject revertToEdition(
            @ApiParam(value = "Name of the virtualization to be deleted", required = true) final @PathVariable(VIRTUALIZATION) String virtualization,
            @ApiParam(value = "Revision number", required = true) final @PathVariable(REVISION) long revision) {
        return repositoryManager.runInTransaction(false, () -> {
            Edition e = repositoryManager.findEdition(virtualization, revision);

            if (e == null) {
                throw notFound(virtualization + " " + revision); //$NON-NLS-1$
            }

            byte[] bytes = repositoryManager.findEditionExport(e);
            Assertion.isNotNull(bytes);

            try {
                return importDataVirtualization(virtualization, new ByteArrayResource(bytes), false);
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        });
    }

    /**
     * Get the published virtualization metrics
     * @param virtualization
     * @return
     * @throws IOException
     */
    @RequestMapping(value = {VIRTUALIZATION_PLACEHOLDER + StringConstants.FS + "metrics"}, method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Get the current metric values for the given virtualization.  Assumes only a single running pod.", response = PodMetrics.class)
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No virtualization could be found with name"),
            @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred."),
            @ApiResponse(code = 503, message = "Metrics are not available")})
    public PodMetrics getPublishedVirtualizationMetrics(
            @ApiParam(value = "name of the virtualization",
            required = true) final @PathVariable(VIRTUALIZATION) String virtualization) throws IOException
            {

        VirtualizationStatus status = this.openshiftClient.getVirtualizationStatus(virtualization);
        BuildStatus buildStatus = status.getBuildStatus();

        if (buildStatus.getStatus() == Status.NOTFOUND) {
            throw notFound(virtualization);
        }

        DeploymentStatus deploymentStatus = status.getDeploymentStatus();
        if (deploymentStatus.getStatus() != DeploymentStatus.Status.RUNNING) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }

        PodMetrics metrics = new PodMetrics();

        String startedAt = this.openshiftClient.getPodStartedAt(buildStatus.getNamespace(), buildStatus.getOpenShiftName());
        metrics.setStartedAt(startedAt);

        String baseUrl = String.format("http://%s:%s/jolokia/read/", buildStatus.getOpenShiftName(), ProtocolType.JOLOKIA.getTargetPort()); //$NON-NLS-1$

        String auth = "jolokia:jolokia"; //$NON-NLS-1$
        String authValue = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1)); //$NON-NLS-1$
        BasicHeader authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, authValue);

        try (SyndesisHttpClient client = new SyndesisHttpClient()){
            try (InputStream response = client.executeGET(baseUrl + "org.teiid:type=Runtime/Sessions", authHeader);) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode value = root.withArray("value");
                if (value != null) {
                    int sessionCount = value.size();
                    metrics.setSessions(sessionCount);
                }
            }

            try (InputStream response = client.executeGET(baseUrl + "org.teiid:type=Runtime/TotalRequestsProcessed", authHeader);) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode value = root.get("value");
                if (value != null) {
                    long requestCount = value.asLong();
                    metrics.setRequestCount(requestCount);
                }
            }

            try (InputStream response = client.executeGET(baseUrl + "org.teiid:type=Cache,name=ResultSet/HitRatio", authHeader);) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode value = root.get("value");
                if (value != null) {
                    double hitRatio = value.asDouble();
                    metrics.setResultSetCacheHitRatio(hitRatio);
                }
            }
        }

        return metrics;
    }

    @GetMapping(value = {
            VIRTUALIZATION_PLACEHOLDER + StringConstants.FS + ROLES }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Return all role information for the given virtualization", response = RoleInfo.class)
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public RoleInfo getRoles(
            @ApiParam(value = "name of the virtualization", required = true) final @PathVariable(VIRTUALIZATION) String virtualization)
            {
        List<TablePrivileges> privileges = repositoryManager
                .runInTransaction(false, () -> {
                    return repositoryManager
                            .findAllTablePrivileges(virtualization);
                });
        // we're using a parent container for if / when procedure and schema
        // level and
        // other such information is added.
        RoleInfo result = new RoleInfo();
        result.setTablePrivileges(privileges);
        return result;
    }

    @PutMapping(value = {
            VIRTUALIZATION_PLACEHOLDER + StringConstants.FS + ROLES }, produces = {
                    MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Apply the role changes to the given virtualization")
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.") })
    public void applyRoles(
            @ApiParam(value = "name of the virtualization", required = true) final @PathVariable(VIRTUALIZATION) String virtualization,
            @ApiParam(value = "role info", required = true) final @RequestBody RoleInfo roleInfo)
            {

        repositoryManager.runInTransaction(false, () -> {
            RoleInfo.Operation op = roleInfo.getOperation();
            for (TablePrivileges tablePrivileges : roleInfo
                    .getTablePrivileges()) {
                if (tablePrivileges.getRoleName() == null) {
                    if (op != Operation.REVOKE) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can only revoke / clear across all roles"); //$NON-NLS-1$
                    }
                    repositoryManager.deleteTablePrivileges(tablePrivileges.getViewDefinitionIds());
                    continue;
                }
                for (String viewId : tablePrivileges.getViewDefinitionIds()) {
                    TablePrivileges existing = repositoryManager
                            .findTablePrivileges(viewId,
                                    tablePrivileges.getRoleName());
                    switch (op) {
                    case GRANT:
                        if (existing == null) {
                            existing = repositoryManager.createTablePrivileges(
                                    viewId, tablePrivileges.getRoleName());
                        }
                        existing.getGrantPrivileges()
                                .addAll(tablePrivileges.getGrantPrivileges());
                        break;
                    case REVOKE:
                        if (existing != null) {
                            existing.getGrantPrivileges().removeAll(
                                    tablePrivileges.getGrantPrivileges());
                            if (existing.getGrantPrivileges().isEmpty()) {
                                repositoryManager.deleteTablePrivileges(existing);
                            }
                        } // else currently not implemented
                          // there are no schema level permissions to remove from
                        break;
                    default:
                        throw new AssertionError();
                    }
                }
            }
            return null;
        });
    }

}
