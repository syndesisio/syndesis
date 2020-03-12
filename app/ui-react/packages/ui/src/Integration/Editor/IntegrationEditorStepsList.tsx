import { DataList } from '@patternfly/react-core';
import * as React from 'react';
import './IntegrationEditorStepsList.css';

export const IntegrationEditorStepsList: React.FunctionComponent = ({
  children,
}) => (
  <DataList
    aria-label={'integration editor step list'}
    className={'integration-editor-steps-list'}
  >
    {children}
  </DataList>
);
