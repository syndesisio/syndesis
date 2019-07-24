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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.PivotGroup;
import com.google.api.services.sheets.v4.model.PivotGroupSortValueBucket;
import com.google.api.services.sheets.v4.model.PivotTable;
import com.google.api.services.sheets.v4.model.PivotValue;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import io.syndesis.connector.sheets.model.CellCoordinate;
import io.syndesis.connector.sheets.model.GooglePivotTable;
import io.syndesis.connector.sheets.model.RangeCoordinate;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsApiCollection;
import org.apache.camel.component.google.sheets.internal.GoogleSheetsConstants;
import org.apache.camel.component.google.sheets.internal.SheetsSpreadsheetsApiMethod;
import org.apache.camel.component.google.sheets.stream.GoogleSheetsStreamConstants;
import org.apache.camel.util.ObjectHelper;

public class GoogleSheetsAddPivotTableCustomizer implements ComponentProxyCustomizer {

    private String spreadsheetId;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        setApiMethod(options);
        component.setBeforeProducer(this::beforeProducer);
    }

    private void setApiMethod(Map<String, Object> options) {
        spreadsheetId = ConnectorOptions.extractOption(options, "spreadsheetId");

        options.put("apiName",
                GoogleSheetsApiCollection.getCollection().getApiName(SheetsSpreadsheetsApiMethod.class).getName());
        options.put("methodName", "batchUpdate");
    }

    private void beforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        final GooglePivotTable model = exchange.getIn().getBody(GooglePivotTable.class);

        if (model != null && ObjectHelper.isNotEmpty(model.getSpreadsheetId())) {
            spreadsheetId = model.getSpreadsheetId();
        }

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setIncludeSpreadsheetInResponse(true);
        batchUpdateRequest.setRequests(new ArrayList<>());

        UpdateCellsRequest updateCellsRequest = new UpdateCellsRequest();
        updateCellsRequest.setFields("pivotTable");
        batchUpdateRequest.getRequests().add(new Request().setUpdateCells(updateCellsRequest));

        RowData rowData = new RowData();
        PivotTable pivotTable = new PivotTable();

        if (model != null) {
            Integer defaultSheetId = Optional.ofNullable(model.getSheetId()).orElse(0);

            pivotTable.setValueLayout(model.getValueLayout());

            GridRange sourceRange = new GridRange();
            sourceRange.setSheetId(Optional.ofNullable(model.getSourceSheetId()).orElse(defaultSheetId));
            RangeCoordinate coordinate = RangeCoordinate.fromRange(model.getSourceRange());
            sourceRange.setStartRowIndex(coordinate.getRowStartIndex());
            sourceRange.setEndRowIndex(coordinate.getRowEndIndex());
            sourceRange.setStartColumnIndex(coordinate.getColumnStartIndex());
            sourceRange.setEndColumnIndex(coordinate.getColumnEndIndex());
            pivotTable.setSource(sourceRange);


            addRowGroups(pivotTable, model);
            addColumnGroups(pivotTable, model);
            addValueGroups(pivotTable, model);
        }

        updateCellsRequest.setStart(getStartCoordinate(pivotTable, model));

        rowData.setValues(Collections.singletonList(new CellData().setPivotTable(pivotTable)));
        updateCellsRequest.setRows(Collections.singletonList(rowData));

        in.setHeader(GoogleSheetsStreamConstants.SPREADSHEET_ID, spreadsheetId);
        in.setHeader(GoogleSheetsConstants.PROPERTY_PREFIX + "batchUpdateSpreadsheetRequest", batchUpdateRequest);
    }

    private GridCoordinate getStartCoordinate(PivotTable pivotTable, GooglePivotTable model) {
        Integer sheetId = Optional.ofNullable(model)
                                    .map(GooglePivotTable::getSheetId)
                                    .orElse(0);

        Integer sourceSheetId = Optional.ofNullable(model)
                                    .map(GooglePivotTable::getSourceSheetId)
                                    .orElse(sheetId);

        if (model != null && model.getStart() != null) {
            CellCoordinate start = CellCoordinate.fromCellId(model.getStart());
            return new GridCoordinate()
                    .setSheetId(sheetId)
                    .setColumnIndex(start.getColumnIndex())
                    .setRowIndex(start.getRowIndex());
        } else if (pivotTable.getSource() != null && sheetId.equals(sourceSheetId)) {
            return new GridCoordinate()
                    .setSheetId(sheetId)
                    .setColumnIndex(pivotTable.getSource().getEndColumnIndex() + 1)
                    .setRowIndex(pivotTable.getSource().getStartRowIndex());
        } else {
            return new GridCoordinate()
                    .setSheetId(sheetId)
                    .setColumnIndex(0)
                    .setRowIndex(0);
        }
    }

    private void addRowGroups(PivotTable pivotTable, GooglePivotTable model) {
        if (ObjectHelper.isEmpty(model.getRowGroups())) {
            return;
        }

        List<PivotGroup> groups = getPivotGroups(model.getRowGroups());
        if (ObjectHelper.isNotEmpty(groups)) {
            pivotTable.setRows(groups);
        }
    }

    private void addColumnGroups(PivotTable pivotTable, GooglePivotTable model) {
        if (ObjectHelper.isEmpty(model.getColumnGroups())) {
            return;
        }

        List<PivotGroup> groups = getPivotGroups(model.getColumnGroups());
        if (ObjectHelper.isNotEmpty(groups)) {
            pivotTable.setColumns(groups);
        }
    }

    private List<PivotGroup> getPivotGroups(List<GooglePivotTable.PivotGroup> model) {
        List<PivotGroup> groups = new ArrayList<>();
        model.forEach(group -> {
            PivotGroup pivotGroup = new PivotGroup();
            pivotGroup.setLabel(group.getLabel());
            pivotGroup.setShowTotals(group.isShowTotals());
            pivotGroup.setSortOrder(group.getSortOrder());
            pivotGroup.setSourceColumnOffset(CellCoordinate.fromCellId(group.getSourceColumn()).getColumnIndex());

            if (ObjectHelper.isNotEmpty(group.getValueBucket()) ||
                    ObjectHelper.isNotEmpty(group.getValueGroupIndex())) {
                PivotGroupSortValueBucket valueBucket = new PivotGroupSortValueBucket();
                valueBucket.setValuesIndex(group.getValueGroupIndex());
                valueBucket.setBuckets(Collections.singletonList(new ExtendedValue().setStringValue(group.getValueBucket())));
                pivotGroup.setValueBucket(valueBucket);
            }

            groups.add(pivotGroup);
        });

        return groups;
    }

    private void addValueGroups(PivotTable pivotTable, GooglePivotTable model) {
        List<PivotValue> values = new ArrayList<>();
        model.getValueGroups().forEach(group -> {
            PivotValue pivotValue = new PivotValue();
            pivotValue.setName(group.getName());

            if (ObjectHelper.isNotEmpty(group.getFormula())) {
                pivotValue.setFormula(group.getFormula());
                pivotValue.setSummarizeFunction("CUSTOM");
            } else {
                pivotValue.setSourceColumnOffset(CellCoordinate.fromCellId(group.getSourceColumn()).getColumnIndex());
                pivotValue.setSummarizeFunction(group.getFunction());
            }

            values.add(pivotValue);
        });

        if (ObjectHelper.isNotEmpty(values)) {
            pivotTable.setValues(values);
        }
    }

}
