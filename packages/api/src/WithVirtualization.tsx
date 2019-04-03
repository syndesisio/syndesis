import { RestDataService } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';

export interface IWithVirtualizationProps {
  virtualizationId: string;
  initialValue?: RestDataService;
  children(props: IFetchState<RestDataService>): any;
}

/**
 * A component that fetches the virtualization with the specified identifier.
 * @see [virtualizationId]{@link IWithVirtualizationProps#virtualizationId}
 */
export class WithVirtualization extends React.Component<
  IWithVirtualizationProps
> {
  public render() {
    return (
      <DVFetch<RestDataService>
        url={`workspace/dataservices/${this.props.virtualizationId}`}
        defaultValue={{
          connections: 0,
          drivers: 0,
          keng___links: [],
          keng__baseUri: '',
          keng__dataPath: '',
          keng__hasChildren: false,
          keng__id: '',
          keng__kType: '',
          publishedState: 'NOTFOUND',
          serviceVdbName: '',
          serviceVdbVersion: '',
          serviceViewDefinitions: [],
          serviceViewModel: '',
          tko__description: '',
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </DVFetch>
    );
  }
}
