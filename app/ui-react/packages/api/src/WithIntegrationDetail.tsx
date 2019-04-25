import { IIntegrationDetail } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithIntegrationDetailProps {
  integrationId: string;
  initialValue?: IIntegrationDetail;
  children(props: IFetchState<IIntegrationDetail>): any;
}

/**
 * A component that fetches the integration with the specified identifier.
 * @see [integrationId]{@link IWithIntegrationProps#integrationId}
 */
export class WithIntegrationDetail extends React.Component<
  IWithIntegrationDetailProps
> {
  public render() {
    return (
      <SyndesisFetch<IIntegrationDetail>
        url={`/integrations/${this.props.integrationId}`}
        defaultValue={{
          name: '',
          isDraft: false,
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
