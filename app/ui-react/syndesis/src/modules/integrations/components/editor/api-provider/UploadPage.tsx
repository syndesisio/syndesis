import { ApicurioAdapter } from '@syndesis/apicurio-adapter';
import { PageSection } from '@syndesis/ui';
import * as React from 'react';
import { PageTitle } from '../../../../../shared';

export class UploadPage extends React.Component {
  public render() {
    return (
      <PageSection>
        <PageTitle title={'Start integration with an API call'} />
        <ApicurioAdapter
          specification={''}
          onSpecification={
            // tslint:disable-next-line
            s => console.log(s)
          }
        />
      </PageSection>
    );
  }
}
