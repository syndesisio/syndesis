// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Button,
  Expandable,
  PageSection,
  Split,
  SplitItem,
  Stack,
  StackItem,
  Text,
  TextContent,
  TextVariants,
  Title,
} from '@patternfly/react-core';
import { SyncIcon } from '@patternfly/react-icons';
import * as React from 'react';
import './ExpandablePreview.css';
import { IColumn, PreviewResults } from './PreviewResults';

/**
 * @param i18nEmptyResultsTitle - text for empty results title
 * @param i18nEmptyResultsMsg - text for empty results message
 * @param i18nHidePreview - text for hide preview toggle control
 * @param i18nLoadingQueryResults - text for spinner when query results are loading
 * @param i18nShowPreview - text for show preview toggle control
 * @param i18nTitle - title for the component
 * @param initialExpanded - 'true' if preview is to be expanded initially
 * @param onPreviewExpandedChanged - handle changes in expansion
 * @param onRefreshResults - handle results refresh
 * @param queryResultCols - the result columns
 * @param queryResultRows - the result rows
 */
export interface IExpandablePreviewProps {
  i18nEmptyResultsTitle: string;
  i18nEmptyResultsMsg: string;
  i18nHidePreview: string;
  i18nLoadingQueryResults: string;
  i18nRowTotalLabel: string;
  i18nShowPreview: string;
  i18nTitle: string;
  initialExpanded?: boolean;
  isLoadingPreview: boolean;
  onPreviewExpandedChanged: (previewExpanded: boolean) => void;
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
  i18nLoadingQueryResults,
  i18nRowTotalLabel,
  i18nShowPreview,
  i18nTitle,
  initialExpanded = true,
  isLoadingPreview = false,
  onPreviewExpandedChanged,
  onRefreshResults,
  queryResultCols,
  queryResultRows,
}: IExpandablePreviewProps) => {
  const [expanded, setExpanded] = React.useState(initialExpanded);
  const toggleExpanded = () => {
    setExpanded(!expanded);
    onPreviewExpandedChanged(!expanded);
  };

  return (
    <PageSection
      className={'expandable-preview__section'}
      isFilled={expanded}
      variant="light"
    >
      <Expandable
        toggleText={expanded ? i18nHidePreview : i18nShowPreview}
        onToggle={toggleExpanded}
        isExpanded={expanded}
      >
        <Split style={{ alignItems: 'center' }}>
          <SplitItem isFilled={false}>
            <Title headingLevel="h5" size="lg">
              {i18nTitle}
            </Title>
          </SplitItem>
          <SplitItem isFilled={false}>
            <Button
              variant="plain"
              aria-label="Action"
              onClick={onRefreshResults}
              isDisabled={false}
            >
              <SyncIcon />
            </Button>
          </SplitItem>
        </Split>
        <Stack gutter={'sm'}>
          {queryResultRows.length > 0 && (
            <StackItem isFilled={false}>
              <TextContent>
                <Text component={TextVariants.small}>
                  {i18nRowTotalLabel} {queryResultRows.length}
                </Text>
              </TextContent>
            </StackItem>
          )}
          <StackItem isFilled={true}>
            <PreviewResults
              queryResultRows={queryResultRows}
              queryResultCols={queryResultCols}
              i18nEmptyResultsTitle={i18nEmptyResultsTitle}
              i18nEmptyResultsMsg={i18nEmptyResultsMsg}
              i18nLoadingQueryResults={i18nLoadingQueryResults}
              isLoadingPreview={isLoadingPreview}
            />
          </StackItem>
        </Stack>
      </Expandable>
    </PageSection>
  );
};
