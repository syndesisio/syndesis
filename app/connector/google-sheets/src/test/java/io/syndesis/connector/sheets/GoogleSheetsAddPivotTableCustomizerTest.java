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

package io.syndesis.connector.sheets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.impl.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.PivotTable;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import io.syndesis.connector.sheets.model.GooglePivotTable;
import io.syndesis.connector.support.util.ConnectorOptions;

public class GoogleSheetsAddPivotTableCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsAddPivotTableCustomizer customizer;

    @BeforeEach
    public void setupCustomizer() {
        customizer = new GoogleSheetsAddPivotTableCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("spreadsheetId", getSpreadsheetId());

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), ConnectorOptions.extractOption(options, "apiName"));
        Assertions.assertEquals("batchUpdate", ConnectorOptions.extractOption(options, "methodName"));

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assertions.assertEquals("pivotTable", updateCellsRequest.getFields());
        Assertions.assertNotNull(updateCellsRequest.getStart());
        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getSheetId());
        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getColumnIndex());
        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();
        Assertions.assertNotNull(pivotTable);
        Assertions.assertNull(pivotTable.getSource());
        Assertions.assertNull(pivotTable.getRows());
        Assertions.assertNull(pivotTable.getColumns());
        Assertions.assertNull(pivotTable.getValues());
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GooglePivotTable model = new GooglePivotTable();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setSourceSheetId(0);
        model.setSheetId(0);

        model.setSourceRange("A1:G10");

        GooglePivotTable.PivotGroup rowGroup = new GooglePivotTable.PivotGroup();
        rowGroup.setLabel("RowGROUP");
        rowGroup.setSourceColumn("A");
        rowGroup.setValueBucket("Bucket");
        rowGroup.setShowTotals(false);
        rowGroup.setSortOrder("DESCENDING");

        model.setRowGroups(Collections.singletonList(rowGroup));

        GooglePivotTable.PivotGroup columnGroup = new GooglePivotTable.PivotGroup();
        columnGroup.setLabel("ColumnGROUP");
        columnGroup.setSourceColumn("E");

        model.setColumnGroups(Collections.singletonList(columnGroup));

        GooglePivotTable.ValueGroup valueGroup = new GooglePivotTable.ValueGroup();
        valueGroup.setSourceColumn("D");
        model.setValueGroups(Collections.singletonList(valueGroup));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getSheetId());
        Assertions.assertEquals(Integer.valueOf(8), updateCellsRequest.getStart().getColumnIndex());
        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();
        Assertions.assertEquals("HORIZONTAL", pivotTable.getValueLayout());

        Assertions.assertEquals(Integer.valueOf(0), pivotTable.getSource().getStartRowIndex());
        Assertions.assertEquals(Integer.valueOf(10), pivotTable.getSource().getEndRowIndex());
        Assertions.assertEquals(Integer.valueOf(0), pivotTable.getSource().getStartColumnIndex());
        Assertions.assertEquals(Integer.valueOf(7), pivotTable.getSource().getEndColumnIndex());

        Assertions.assertEquals(1, pivotTable.getRows().size());
        Assertions.assertEquals("RowGROUP", pivotTable.getRows().get(0).getLabel());
        Assertions.assertEquals(Integer.valueOf(0), pivotTable.getRows().get(0).getSourceColumnOffset());
        Assertions.assertEquals("DESCENDING", pivotTable.getRows().get(0).getSortOrder());
        Assertions.assertFalse(pivotTable.getRows().get(0).getShowTotals());
        Assertions.assertNotNull(pivotTable.getRows().get(0).getValueBucket());
        Assertions.assertEquals("Bucket", pivotTable.getRows().get(0).getValueBucket().getBuckets().get(0).getStringValue());


        Assertions.assertEquals(1, pivotTable.getColumns().size());
        Assertions.assertEquals("ColumnGROUP", pivotTable.getColumns().get(0).getLabel());
        Assertions.assertEquals(Integer.valueOf(4), pivotTable.getColumns().get(0).getSourceColumnOffset());
        Assertions.assertEquals("ASCENDING", pivotTable.getColumns().get(0).getSortOrder());
        Assertions.assertTrue(pivotTable.getColumns().get(0).getShowTotals());
        Assertions.assertNull(pivotTable.getColumns().get(0).getValueBucket());

        Assertions.assertEquals(1, pivotTable.getValues().size());
        Assertions.assertEquals("SUM", pivotTable.getValues().get(0).getSummarizeFunction());
        Assertions.assertEquals(Integer.valueOf(3), pivotTable.getValues().get(0).getSourceColumnOffset());
    }

    @Test
    public void testDivergingSourceAndTargetSheets() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GooglePivotTable model = new GooglePivotTable();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setSourceSheetId(0);
        model.setSheetId(1);

        model.setSourceRange("A1:D10");
        model.setValueGroups(Collections.singletonList(sampleValueGroup()));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assertions.assertEquals(Integer.valueOf(1), updateCellsRequest.getStart().getSheetId());
        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getColumnIndex());
        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();
        assertSampleValueGroup(pivotTable);
    }

    @Test
    public void testDivergingSourceAndTargetSheetsWithExplicitStart() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GooglePivotTable model = new GooglePivotTable();
        model.setStart("E10");
        model.setSpreadsheetId(getSpreadsheetId());
        model.setSourceSheetId(1);
        model.setSheetId(0);

        model.setSourceRange("A1:D10");
        model.setValueGroups(Collections.singletonList(sampleValueGroup()));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getSheetId());
        Assertions.assertEquals(Integer.valueOf(4), updateCellsRequest.getStart().getColumnIndex());
        Assertions.assertEquals(Integer.valueOf(9), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();
        assertSampleValueGroup(pivotTable);
    }

    @Test
    public void testExplicitPivotTableStart() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GooglePivotTable model = new GooglePivotTable();
        model.setStart("C12");
        model.setSpreadsheetId(getSpreadsheetId());

        model.setSourceRange("A1:D10");
        model.setValueGroups(Collections.singletonList(sampleValueGroup()));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getSheetId());
        Assertions.assertEquals(Integer.valueOf(2), updateCellsRequest.getStart().getColumnIndex());
        Assertions.assertEquals(Integer.valueOf(11), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();
        assertSampleValueGroup(pivotTable);
    }

    @Test
    public void testCustomValueGroupFunction() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GooglePivotTable model = new GooglePivotTable();
        model.setStart("C12");
        model.setSpreadsheetId(getSpreadsheetId());

        model.setSourceRange("A1:D10");

        GooglePivotTable.ValueGroup valueGroup = new GooglePivotTable.ValueGroup();
        valueGroup.setSourceColumn("E");
        valueGroup.setFunction("AVERAGE");
        model.setValueGroups(Collections.singletonList(valueGroup));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getSheetId());
        Assertions.assertEquals(Integer.valueOf(2), updateCellsRequest.getStart().getColumnIndex());
        Assertions.assertEquals(Integer.valueOf(11), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();

        Assertions.assertEquals(1, pivotTable.getValues().size());
        Assertions.assertEquals("AVERAGE", pivotTable.getValues().get(0).getSummarizeFunction());
        Assertions.assertEquals(Integer.valueOf(4), pivotTable.getValues().get(0).getSourceColumnOffset());
    }

    @Test
    public void testCustomValueGroupFormula() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GooglePivotTable model = new GooglePivotTable();
        model.setSpreadsheetId(getSpreadsheetId());

        model.setSourceRange("A1:D10");

        GooglePivotTable.ValueGroup valueGroup = new GooglePivotTable.ValueGroup();
        valueGroup.setSourceColumn("A");
        valueGroup.setFormula("=Cost*SUM(Quantity)");
        model.setValueGroups(Collections.singletonList(valueGroup));

        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Assertions.assertNotNull(inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));
        Assertions.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest");
        Assertions.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getSheetId());
        Assertions.assertEquals(Integer.valueOf(5), updateCellsRequest.getStart().getColumnIndex());
        Assertions.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();

        Assertions.assertEquals(1, pivotTable.getValues().size());
        Assertions.assertEquals("CUSTOM", pivotTable.getValues().get(0).getSummarizeFunction());
        Assertions.assertEquals("=Cost*SUM(Quantity)", pivotTable.getValues().get(0).getFormula());
        Assertions.assertNull(pivotTable.getValues().get(0).getSourceColumnOffset());
    }

    private static GooglePivotTable.ValueGroup sampleValueGroup() {
        GooglePivotTable.ValueGroup valueGroup = new GooglePivotTable.ValueGroup();
        valueGroup.setSourceColumn("C");
        return valueGroup;
    }

    private static void assertSampleValueGroup(PivotTable pivotTable) {
        Assertions.assertEquals(1, pivotTable.getValues().size());
        Assertions.assertEquals("SUM", pivotTable.getValues().get(0).getSummarizeFunction());
        Assertions.assertEquals(Integer.valueOf(2), pivotTable.getValues().get(0).getSourceColumnOffset());
    }
}
