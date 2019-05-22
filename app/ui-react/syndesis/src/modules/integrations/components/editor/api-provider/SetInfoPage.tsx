import * as React from 'react';
import { PageTitle } from '../../../../../shared';

/**
 * The page where you define basic info such as the name and description of the integration.
 */
export class SetInfoPage extends React.Component {
  public render() {
    return (
      <>
        <PageTitle title={'Give this integration a name'} />
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
