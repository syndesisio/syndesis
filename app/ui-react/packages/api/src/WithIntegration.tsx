import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithIntegrationProps {
  integrationId: string;
  initialValue?: IIntegrationOverviewWithDraft;
  children(props: IFetchState<IIntegrationOverviewWithDraft>): any;
}

/**
 * A component that fetches the integration with the specified identifier.
 * @see [integrationId]{@link IWithIntegrationProps#integrationId}
 */
export class WithIntegration extends React.Component<IWithIntegrationProps> {
  public render() {
    return (
      <SyndesisFetch<IIntegrationOverviewWithDraft>
        url={`/integrations/${this.props.integrationId}`}
        defaultValue={{
          name: '',
          isDraft: true,
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
