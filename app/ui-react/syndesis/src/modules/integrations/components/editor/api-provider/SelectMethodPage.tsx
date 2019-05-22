import * as React from 'react';
import { PageTitle } from '../../../../../shared';

/**
 * The very first page of the API Provider editor, where you decide
 * if you want to provide an OpenAPI Spec file via drag and drop, or
 * if you a URL of an OpenAPI spec
 */
export class SelectMethodPage extends React.Component {
  public render() {
    return (
      <>
        <PageTitle title={'Start integration with an API call'} />
        <p>
          Lorem ipsum dolor sit amet, consectetur adipisicing elit. Blanditiis
          illo, iusto nesciunt nostrum omnis pariatur rerum vero voluptates.
          Accusamus aliquid corporis deleniti ea earum ipsa optio, quidem quod
          ut! Placeat.
        </p>
      </>
    );
  }
}
