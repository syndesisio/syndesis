import { RestVdbModel } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';

export interface IWithVdbModelProps {
  vdbId: string;
  modelId: string;
  children(props: IFetchState<RestVdbModel>): any;
}

/**
 * A component that fetches the specified VDB model
 * @see [vdbId]{@link IWithVdbModelProps#vdbId}
 * @see [modelId]{@link IWithVdbModelProps#modelId}
 */
export class WithVdbModel extends React.Component<IWithVdbModelProps> {
  public render() {
    return (
      <DVFetch<RestVdbModel>
        url={`workspace/vdbs/${this.props.vdbId}/Models/${this.props.modelId}`}
        defaultValue={{
          keng__baseUri: '',
          keng__dataPath: '',
          keng__ddl: '',
          keng__hasChildren: false,
          keng__id: '',
          keng__kType: '',
          mmcore__modelType: '',
          vdb__metadataType: 'DDL',
          vdb__visible: false
        }}
      >
        {({ response }) => this.props.children({ ...response })}
      </DVFetch>
    );
  }
}
