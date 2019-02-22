import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationEditorChooseConnection {
  /**
   * The main title of the content, shown before the connections.
   */
  i18nTitle: string;
  /**
   * The description of the content, shown before the connections.
   */
  i18nSubtitle: string;
}

/**
 * A component to render a list of connections, to be used in the integration
 * editor.
 *
 * @see [i18nTitle]{@link IIntegrationEditorChooseConnection#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationEditorChooseConnection#i18nSubtitle}
 */
export class IntegrationEditorChooseConnection extends React.Component<
  IIntegrationEditorChooseConnection
> {
  public render() {
    return (
      <>
        <div className="container-fluid">
          <h1>{this.props.i18nTitle}</h1>
          <p>{this.props.i18nSubtitle}</p>
        </div>
        <div className={'container-fluid'}>
          <ListView>{this.props.children}</ListView>
        </div>
      </>
    );
  }
}
