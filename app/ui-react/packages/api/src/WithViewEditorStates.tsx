import { ViewEditorState } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';

export interface IWithViewEditorStatesRenderProps
  extends IFetchState<ViewEditorState[]> {
  read(): Promise<void>;
}

export interface IWithViewEditorStatesProps {
  idPattern?: string;
  children(props: IWithViewEditorStatesRenderProps): any;
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
        {({ read, response }) => this.props.children({ ...response, read })}
      </DVFetch>
    );
  }
}
