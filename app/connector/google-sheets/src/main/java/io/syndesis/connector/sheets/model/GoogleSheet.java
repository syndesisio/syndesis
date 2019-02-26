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

public class GoogleSheet {

    private Integer index;
    private Integer sheetId;
    private String title;

    public Integer getIndex() {
        return index;
    }

    /**
     * Specifies the index.
     *
     * @param index
     */
    public void setIndex(Integer index) {
        this.index = index;
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

    @Override
    public String toString() {
        return String.format("%s [sheetId=%s, index=%s, title=%s]", GoogleSheet.class.getSimpleName(), sheetId, index, title);
    }
}
