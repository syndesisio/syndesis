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

import java.util.Objects;
import java.util.Optional;

/**
 * @author Christoph Deppisch
 */
public class GoogleChart {

    private String spreadsheetId;
    private String title;
    private String subtitle;

    private String overlayPosition;

    private Integer sheetId;
    private Integer sourceSheetId;

    private BasicChart basicChart;
    private PieChart pieChart;

    public static class PieChart {
        private String domainRange;
        private String dataRange;

        private String legendPosition = "RIGHT_LEGEND";

        public String getDomainRange() {
            return domainRange;
        }

        /**
         * Specifies the domainRange.
         *
         * @param domainRange
         */
        public void setDomainRange(String domainRange) {
            this.domainRange = domainRange;
        }

        public String getDataRange() {
            return dataRange;
        }

        /**
         * Specifies the dataRange.
         *
         * @param dataRange
         */
        public void setDataRange(String dataRange) {
            this.dataRange = dataRange;
        }

        public String getLegendPosition() {
            return legendPosition;
        }

        /**
         * Specifies the legendPosition.
         *
         * @param legendPosition
         */
        public void setLegendPosition(String legendPosition) {
            this.legendPosition = legendPosition;
        }
    }

    public static class BasicChart {
        private String type = "COLUMN";
        private String axisTitleBottom;
        private String axisTitleLeft;

        private String domainRange;
        private String dataRange;

        public String getDomainRange() {
            return domainRange;
        }

        /**
         * Specifies the domainRange.
         *
         * @param domainRange
         */
        public void setDomainRange(String domainRange) {
            this.domainRange = domainRange;
        }

        public String getDataRange() {
            return dataRange;
        }

        /**
         * Specifies the dataRange.
         *
         * @param dataRange
         */
        public void setDataRange(String dataRange) {
            this.dataRange = dataRange;
        }

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

    public String getOverlayPosition() {
        return overlayPosition;
    }

    /**
     * Specifies the overlayPosition in A1 notation representing a target cell as anchor. If set
     * the chart is placed as overlayPosition on the same sheet next to the anchor cell.
     *
     * @param overlayPosition
     */
    public void setOverlayPosition(String overlayPosition) {
        this.overlayPosition = overlayPosition;
    }

    public Integer getSourceSheetId() {
        return sourceSheetId;
    }

    /**
     * Specifies the sourceSheetId.
     *
     * @param sourceSheetId
     */
    public void setSourceSheetId(Integer sourceSheetId) {
        this.sourceSheetId = sourceSheetId;
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

    public PieChart getPieChart() {
        return pieChart;
    }

    /**
     * Specifies the pieChart.
     *
     * @param pieChart
     */
    public void setPieChart(PieChart pieChart) {
        this.pieChart = pieChart;
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
