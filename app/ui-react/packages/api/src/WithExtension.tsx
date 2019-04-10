import { Extension } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithExtensionProps {
  extensionId: string;
  initialValue?: Extension;
  children(props: IFetchState<Extension>): any;
}

/**
 * A component that fetches the extension with the specified identifier.
 * @see [extensionId]{@link IWithExtensionProps#extensionId}
 */
export class WithExtension extends React.Component<IWithExtensionProps> {
  public render() {
    return (
      <SyndesisFetch<Extension>
        url={`/extensions/${this.props.extensionId}`}
        defaultValue={{
          actions: [],
          extensionType: 'Steps',
          name: '',
          schemaVersion: '',
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
