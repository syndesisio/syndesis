import * as React from 'react';
import { ApiProviderReviewOperations, PageSection } from '@syndesis/ui';
import { PageTitle } from '../../../../../shared';

/**
 * This is usually the final step of the API Provider user flow.
 * This page shows the operations that have been previously defined
 * earlier in the user flow.
 */
export class ReviewOperationsPage extends React.Component {
  public render() {
    return (
      <PageSection>
        <PageTitle title={'Operations'} />
        <ApiProviderReviewOperations />
        <p>
          Lorem ipsum dolor sit amet, consectetur adipisicing elit. Blanditiis
          illo, iusto nesciunt nostrum omnis pariatur rerum vero voluptates.
          Accusamus aliquid corporis deleniti ea earum ipsa optio, quidem quod
          ut! Placeat.
        </p>
      </PageSection>
    );
  }
}
