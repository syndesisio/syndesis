import { DataList } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../Layout';

/**
 * A component to render a list of actions, to be used in the integration
 * editor.
 */
export class IntegrationEditorChooseAction extends React.Component {
  public render() {
    return (
      <PageSection>
        <DataList aria-label={'integration editor choose action list'}>
          {this.props.children}
        </DataList>
      </PageSection>
    );
  }
}
