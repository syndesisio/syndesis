import * as React from 'react';

interface IIntegrationEditorLabelsProps {
  labels: { [key: string]: string };
}

export const IntegrationEditorLabels: React.FunctionComponent<IIntegrationEditorLabelsProps> =
  ({ labels }) => {
    // tslint:disable:no-console
    console.log('labels: ' + JSON.stringify(labels));
    return <></>;
  };
