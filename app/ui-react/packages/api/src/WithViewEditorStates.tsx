import { ViewEditorState } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';

export interface IWithViewEditorStatesProps {
  idPattern?: string;
  children(props: IFetchState<ViewEditorState[]>): any;
}

export class WithViewEditorStates extends React.Component<
  IWithViewEditorStatesProps
> {
  public render() {
    return (
      <DVFetch<ViewEditorState[]>
        url={
          'service/userProfile/viewEditorState' +
          (this.props.idPattern ? '?pattern=' + this.props.idPattern : '')
        }
        defaultValue={[]}
      >
        {({ response }) => this.props.children(response)}
      </DVFetch>
    );
  }
}
