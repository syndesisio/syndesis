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

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.PivotTable;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import io.syndesis.connector.sheets.model.GooglePivotTable;
import org.apache.camel.Exchange;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class GoogleSheetsAddPivotTableCustomizerTest extends AbstractGoogleSheetsCustomizerTestSupport {

    private GoogleSheetsAddPivotTableCustomizer customizer;

    @Before
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

        Assert.assertEquals(GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName(), options.get("apiName"));
        Assert.assertEquals("batchUpdate", options.get("methodName"));

        Assert.assertNotNull(inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));
        Assert.assertEquals(getSpreadsheetId(), inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader("CamelGoogleSheets.batchUpdateSpreadsheetRequest");
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assert.assertEquals("pivotTable", updateCellsRequest.getFields());
        Assert.assertNotNull(updateCellsRequest.getStart());
        Assert.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getSheetId());
        Assert.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getColumnIndex());
        Assert.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();
        Assert.assertNotNull(pivotTable);
        Assert.assertNull(pivotTable.getSource());
        Assert.assertNull(pivotTable.getRows());
        Assert.assertNull(pivotTable.getColumns());
        Assert.assertNull(pivotTable.getValues());
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        Exchange inbound = new DefaultExchange(createCamelContext());

        GooglePivotTable model = new GooglePivotTable();
        model.setSpreadsheetId(getSpreadsheetId());
        model.setSourceSheetId(0);
        model.setSheetId(1);

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

        Assert.assertNotNull(inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));
        Assert.assertEquals(model.getSpreadsheetId(), inbound.getIn().getHeader("CamelGoogleSheets.spreadsheetId"));

        BatchUpdateSpreadsheetRequest batchUpdateRequest = (BatchUpdateSpreadsheetRequest) inbound.getIn().getHeader("CamelGoogleSheets.batchUpdateSpreadsheetRequest");
        Assert.assertEquals(1, batchUpdateRequest.getRequests().size());

        UpdateCellsRequest updateCellsRequest = batchUpdateRequest.getRequests().get(0).getUpdateCells();

        Assert.assertEquals(Integer.valueOf(1), updateCellsRequest.getStart().getSheetId());
        Assert.assertEquals(Integer.valueOf(8), updateCellsRequest.getStart().getColumnIndex());
        Assert.assertEquals(Integer.valueOf(0), updateCellsRequest.getStart().getRowIndex());

        PivotTable pivotTable = updateCellsRequest.getRows().get(0).getValues().get(0).getPivotTable();
        Assert.assertEquals("HORIZONTAL", pivotTable.getValueLayout());

        Assert.assertEquals(Integer.valueOf(0), pivotTable.getSource().getStartRowIndex());
        Assert.assertEquals(Integer.valueOf(10), pivotTable.getSource().getEndRowIndex());
        Assert.assertEquals(Integer.valueOf(0), pivotTable.getSource().getStartColumnIndex());
        Assert.assertEquals(Integer.valueOf(7), pivotTable.getSource().getEndColumnIndex());

        Assert.assertEquals(1, pivotTable.getRows().size());
        Assert.assertEquals("RowGROUP", pivotTable.getRows().get(0).getLabel());
        Assert.assertEquals(Integer.valueOf(0), pivotTable.getRows().get(0).getSourceColumnOffset());
        Assert.assertEquals("DESCENDING", pivotTable.getRows().get(0).getSortOrder());
        Assert.assertFalse(pivotTable.getRows().get(0).getShowTotals());
        Assert.assertNotNull(pivotTable.getRows().get(0).getValueBucket());
        Assert.assertEquals("Bucket", pivotTable.getRows().get(0).getValueBucket().getBuckets().get(0).getStringValue());


        Assert.assertEquals(1, pivotTable.getColumns().size());
        Assert.assertEquals("ColumnGROUP", pivotTable.getColumns().get(0).getLabel());
        Assert.assertEquals(Integer.valueOf(4), pivotTable.getColumns().get(0).getSourceColumnOffset());
        Assert.assertEquals("ASCENDING", pivotTable.getColumns().get(0).getSortOrder());
        Assert.assertTrue(pivotTable.getColumns().get(0).getShowTotals());
        Assert.assertNull(pivotTable.getColumns().get(0).getValueBucket());

        Assert.assertEquals(1, pivotTable.getValues().size());
        Assert.assertEquals("SUM", pivotTable.getValues().get(0).getSummarizeFunction());
        Assert.assertEquals(Integer.valueOf(3), pivotTable.getValues().get(0).getSourceColumnOffset());

    }
}
