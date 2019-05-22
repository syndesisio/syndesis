import * as React from 'react';
import { ApiProviderReviewActions, PageSection } from '@syndesis/ui';
import { PageTitle } from '../../../../../shared';

/**
 * This is the page where a user reviews the actions that have been
 * extracted from the API specification previously created or provided
 * earlier in the API Provider editor.
 */
export class ReviewActionsPage extends React.Component {
  public render() {
    return (
      <PageSection>
        <PageTitle title={'Review Actions'} />
        <ApiProviderReviewActions />
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
