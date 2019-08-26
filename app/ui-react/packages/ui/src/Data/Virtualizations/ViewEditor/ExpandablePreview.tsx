// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { Button, Expandable } from '@patternfly/react-core';
import { SyncIcon } from '@patternfly/react-icons';
import * as React from 'react';
import { TextEditor } from '../../../Shared';
import './ExpandablePreview.css';
import { IColumn, PreviewResults } from './PreviewResults';

/**
 * @param i18nHidePreview - text for hide preview toggle control
 * @param i18nShowPreview - text for show preview toggle control
 * @param initialExpanded - 'true' if preview is to be expanded initially
 * @param onPreviewExpandedChanged - handle changes in expansion
 */
export interface IExpandablePreviewProps {
  i18nEmptyResultsTitle: string;
  i18nEmptyResultsMsg: string;
  i18nHidePreview: string;
  i18nShowPreview: string;
  i18nSelectSqlText: string;
  i18nSelectPreviewText: string;
  initialExpanded?: boolean;
  initialPreviewButtonSelection?: PreviewButtonSelection;
  onPreviewExpandedChanged: (
    previewExpanded: boolean
  ) => void;
  onPreviewButtonSelectionChanged: (
    previewButtonSelection: PreviewButtonSelection
  ) => void;
  onRefreshResults: () => void;
  /**
   * Array of column info for the query results.  (The column id and display label)
   * Example:
   * [ { id: 'fName', label: 'First Name'},
   *   { id: 'lName', label: 'Last Name'},
   *   { id: 'country', label: 'Country' }
   * ]
   */
  queryResultCols: IColumn[];
  /**
   * Array of query result rows - must match up with column ids
   * Example:
   * [ { fName: 'Jean', lName: 'Frissilla', country: 'Italy' },
   *   { fName: 'John', lName: 'Johnson', country: 'US' },
   *   { fName: 'Juan', lName: 'Bautista', country: 'Brazil' },
   *   { fName: 'Jordan', lName: 'Dristol', country: 'Ontario' }
   * ]
   */
  queryResultRows: Array<{}>;
  viewDdl?: string;
}

/**
 * The 'button' selection - SQL or Preview
 */
export enum PreviewButtonSelection {
  SQL = 'SQL',
  PREVIEW = 'PREVIEW',
}

/**
 * Expandable component for display of preview data
 */
export const ExpandablePreview: React.FunctionComponent<
  IExpandablePreviewProps
> = ({
  i18nEmptyResultsTitle,
  i18nEmptyResultsMsg,
  i18nHidePreview,
  i18nShowPreview,
  i18nSelectSqlText,
  i18nSelectPreviewText,
  initialExpanded = true,
  initialPreviewButtonSelection = PreviewButtonSelection.PREVIEW,
  onPreviewExpandedChanged,
  onPreviewButtonSelectionChanged,
  onRefreshResults,
  queryResultCols,
  queryResultRows,
  viewDdl
}: IExpandablePreviewProps) => {

  const [expanded, setExpanded] = React.useState(initialExpanded);
  const [previewButtonSelection, setPreviewButtonSelection] = React.useState(initialPreviewButtonSelection); 
  const [refreshDisabled, setRefreshDisabled] = React.useState(initialPreviewButtonSelection === PreviewButtonSelection.SQL); 

  const toggleExpanded = () => {
    setExpanded(!expanded);
    onPreviewExpandedChanged(!expanded);
  };

  const handleShowSql = () => {
    if(previewButtonSelection === PreviewButtonSelection.PREVIEW) {
      setPreviewButtonSelection(PreviewButtonSelection.SQL);
      setRefreshDisabled(true);
      onPreviewButtonSelectionChanged(PreviewButtonSelection.SQL);
    }
  };

  const handleShowPreview = () => {
    if(previewButtonSelection === PreviewButtonSelection.SQL) {
      setPreviewButtonSelection(PreviewButtonSelection.PREVIEW);
      setRefreshDisabled(false);
      onPreviewButtonSelectionChanged(PreviewButtonSelection.PREVIEW);
    }
  };

  const editorOptions = {
    autofocus: true,
    extraKeys: { 'Ctrl-Space': 'autocomplete' },
    gutters: ['CodeMirror-lint-markers'],
    lineNumbers: true,
    lineWrapping: true,
    matchBrackets: true,
    mode: 'text/x-mysql',
    readOnly: true,
    showCursorWhenSelecting: true,
    styleActiveLine: true,
    tabSize: 2,
  };

  return (
    <Expandable toggleText={expanded ? i18nHidePreview : i18nShowPreview} onToggle={toggleExpanded} isExpanded={expanded}>
      <Button 
        variant="secondary" 
        className="expandable-preview__button" 
        onClick={handleShowSql} 
        isActive={previewButtonSelection === PreviewButtonSelection.SQL}>
        {i18nSelectSqlText}
      </Button>
      <Button 
        variant="secondary" 
        className="expandable-preview__button" 
        onClick={handleShowPreview} 
        isActive={previewButtonSelection === PreviewButtonSelection.PREVIEW}>
        {i18nSelectPreviewText}
      </Button>
      <Button 
        variant="plain" 
        aria-label="Action" 
        onClick={onRefreshResults} 
        isDisabled={refreshDisabled}>
        <SyncIcon />
      </Button>
      <div className="expandable-preview__content" />
      {previewButtonSelection === PreviewButtonSelection.PREVIEW ?
        <PreviewResults
          queryResultRows={queryResultRows}
          queryResultCols={queryResultCols}
          i18nEmptyResultsTitle={i18nEmptyResultsTitle}
          i18nEmptyResultsMsg={i18nEmptyResultsMsg}
        />
        : <TextEditor
          value={viewDdl}
          options={editorOptions}
        />
      }
    </Expandable>
    );
  };
