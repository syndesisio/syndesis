import { Bullseye } from '@patternfly/react-core';
import * as React from 'react';
import { Loader } from './Loader';
import { PageSection } from './Page';

export class PageLoader extends React.PureComponent {
  public render() {
    return (
      <PageSection>
        <Bullseye>
          <Loader size={'lg'} inline={true} />
        </Bullseye>
      </PageSection>
    );
  }
}
