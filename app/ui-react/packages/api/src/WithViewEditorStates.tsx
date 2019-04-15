import { ViewEditorState } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';

export interface IWithViewEditorStatesProps {
  children(props: IFetchState<ViewEditorState[]>): any;
}

export class WithViewEditorStates extends React.Component<
  IWithViewEditorStatesProps
> {
  public render() {
    return (
      <DVFetch<ViewEditorState[]>
        url={'service/userProfile/viewEditorState'}
        defaultValue={[]}
      >
        {({ response }) => this.props.children(response)}
      </DVFetch>
    );
  }
}
