import {
  Expandable,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import { IColumn, PreviewResults } from '..';

export interface IPreviewDataProps {
  queryResultCols: IColumn[];
  queryResultRows: string[][];
  i18nEmptyResultsTitle: string;
  i18nEmptyResultsMsg: string;
  i18nLoadingQueryResults: string;
  i18nPreviewHeading: string;
  i18nHidePreview: string;
  i18nShowPreview: string;
  isLoadingPreview: boolean;
  isExpanded: boolean;
  connectionIcon: React.ReactNode;
  connectionName: string;
  onToggle: () => void;
}

export const PreviewData: React.FunctionComponent<IPreviewDataProps> = props => {
  return (
    <>
      <TextContent>
        <Text component={TextVariants.h2}>
          <span>{props.i18nPreviewHeading} (&nbsp;{props.connectionIcon}
                    &nbsp;{props.connectionName}&nbsp;)</span>
        </Text>
      </TextContent>
      <Expandable
        toggleText={props.isExpanded ? props.i18nHidePreview : props.i18nShowPreview}
        onToggle={props.onToggle}
        isExpanded={props.isExpanded}
        className={'view-create-layout_expandable'}
      >
        <PreviewResults
          queryResultCols={props.queryResultCols}
          queryResultRows={props.queryResultRows}
          i18nEmptyResultsTitle={props.i18nEmptyResultsTitle}
          i18nEmptyResultsMsg={props.i18nEmptyResultsMsg}
          i18nLoadingQueryResults={props.i18nLoadingQueryResults}
          isLoadingPreview={props.isLoadingPreview}
        />
      </Expandable>
    </>
  );
};
