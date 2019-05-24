import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithIntegrationTagsProps {
  integrationId: string;
  initialValue?: Map<string, any>;
  children(props: IFetchState<Map<string, any>>): any;
}

export class WithIntegrationTags extends React.Component<
  IWithIntegrationTagsProps
> {
  public render() {
    return (
      <SyndesisFetch<Map<string, any>>
        url={`/public/integrations/${this.props.integrationId}/tags`}
        defaultValue={{} as Map<string, any>}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
