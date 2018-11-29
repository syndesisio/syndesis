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

package io.syndesis.connector.sheets.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Christoph Deppisch
 */
public class GoogleChart {

    private String spreadsheetId;
    private Integer sheetId;
    private String title;
    private String subtitle;

    private BasicChart basicChart;

    public static class ChartSource {
        private Integer sheetId;
        private Integer fromRow;
        private Integer toRow;
        private Integer columnIndex;
        private String targetAxis;

        public Integer getSheetId() {
            return sheetId;
        }

        /**
         * Specifies the sheetId.
         *
         * @param sheetId
         */
        public void setSheetId(Integer sheetId) {
            this.sheetId = sheetId;
        }

        public Integer getFromRow() {
            return fromRow;
        }

        /**
         * Specifies the fromRow.
         *
         * @param fromRow
         */
        public void setFromRow(Integer fromRow) {
            this.fromRow = fromRow;
        }

        public Integer getToRow() {
            return toRow;
        }

        /**
         * Specifies the toRow.
         *
         * @param toRow
         */
        public void setToRow(Integer toRow) {
            this.toRow = toRow;
        }

        public Integer getColumnIndex() {
            return columnIndex;
        }

        /**
         * Specifies the columnIndex.
         *
         * @param columnIndex
         */
        public void setColumnIndex(Integer columnIndex) {
            this.columnIndex = columnIndex;
        }

        public String getTargetAxis() {
            return targetAxis;
        }

        /**
         * Specifies the targetAxis.
         *
         * @param targetAxis
         */
        public void setTargetAxis(String targetAxis) {
            this.targetAxis = targetAxis;
        }
    }

    public static class BasicChart {
        private String type = "COLUMN";
        private String axisTitleBottom;
        private String axisTitleLeft;

        private ChartSource domainSource;
        private List<ChartSource> dataSources = new ArrayList<>();

        public String getAxisTitleBottom() {
            return axisTitleBottom;
        }

        /**
         * Specifies the axisTitleBottom.
         *
         * @param axisTitleBottom
         */
        public void setAxisTitleBottom(String axisTitleBottom) {
            this.axisTitleBottom = axisTitleBottom;
        }

        public String getAxisTitleLeft() {
            return axisTitleLeft;
        }

        /**
         * Specifies the axisTitleLeft.
         *
         * @param axisTitleLeft
         */
        public void setAxisTitleLeft(String axisTitleLeft) {
            this.axisTitleLeft = axisTitleLeft;
        }

        public ChartSource getDomainSource() {
            return domainSource;
        }

        /**
         * Specifies the domainSource.
         *
         * @param domainSource
         */
        public void setDomainSource(ChartSource domainSource) {
            this.domainSource = domainSource;
        }

        public String getType() {
            return type;
        }

        /**
         * Specifies the chart type. Default is "COLUMN".
         *
         * @param type
         */
        public void setType(String type) {
            this.type = type;
        }

        public List<ChartSource> getDataSources() {
            return dataSources;
        }

        /**
         * Specifies the dataSources.
         *
         * @param dataSources
         */
        public void setDataSources(List<ChartSource> dataSources) {
            this.dataSources = dataSources;
        }
    }

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    /**
     * Specifies the spreadsheetId.
     *
     * @param spreadsheetId
     */
    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public Integer getSheetId() {
        return sheetId;
    }

    /**
     * Specifies the sheetId.
     *
     * @param sheetId
     */
    public void setSheetId(Integer sheetId) {
        this.sheetId = sheetId;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Specifies the title.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Specifies the subtitle.
     *
     * @param subtitle
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public BasicChart getBasicChart() {
        return basicChart;
    }

    /**
     * Specifies the basicChart.
     *
     * @param basicChart
     */
    public void setBasicChart(BasicChart basicChart) {
        this.basicChart = basicChart;
    }

    @Override
    public String toString() {
        return String.format("%s [spreadsheetId=%s, sheetId=%s, title=%s, subtitle=%s]",
                                GoogleChart.class.getSimpleName(),
                                spreadsheetId,
                                Optional.ofNullable(sheetId).map(Objects::toString).orElse("new"),
                                title,
                                subtitle);
    }
}
