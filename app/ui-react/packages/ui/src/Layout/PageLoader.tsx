import * as React from 'react';
import { Loader } from './Loader';
import { PageSection } from './Page';

export class PageLoader extends React.PureComponent {
  public render() {
    return (
      <PageSection>
        <Loader size={'lg'} />
      </PageSection>
    );
  }
}
