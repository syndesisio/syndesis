import { IntegrationOverview } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithIntegrationProps {
  integrationId: string;
  initialValue?: IntegrationOverview;
  children(props: IFetchState<IntegrationOverview>): any;
}

/**
 * A component that fetches the integration with the specified identifier.
 * @see [integrationId]{@link IWithIntegrationProps#integrationId}
 */
export class WithIntegration extends React.Component<IWithIntegrationProps> {
  public render() {
    return (
      <SyndesisFetch<IntegrationOverview>
        url={`/integrations/${this.props.integrationId}`}
        defaultValue={{
          name: '',
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
