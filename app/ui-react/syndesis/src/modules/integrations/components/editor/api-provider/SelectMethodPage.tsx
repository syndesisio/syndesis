import * as React from 'react';
import { ApiProviderSelectMethod, PageSection } from '@syndesis/ui';
import { PageTitle } from '../../../../../shared';

/**
 * The very first page of the API Provider editor, where you decide
 * if you want to provide an OpenAPI Spec file via drag and drop, or
 * if you a URL of an OpenAPI spec
 */
export class SelectMethodPage extends React.Component {
  public render() {
    return (
      <PageSection>
        <PageTitle title={'Start integration with an API call'} />
        <ApiProviderSelectMethod
          i18nTitle={'Start integration with an API call'}
          i18nDescription={
            'Execute this integration when a client invokes an operation defined by this API.'
          }
        />
      </PageSection>
    );
  }
}
