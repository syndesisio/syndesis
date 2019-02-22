import { ConnectionOverview } from '@syndesis/models';
import { IntegrationsListSkeleton } from '@syndesis/ui';
import {
  IntegrationEditorConnectionsList,
  IntegrationEditorConnectionsListItem,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationEditorChooseConnection {
  /**
   * Set to true if there were errors while loading the data.
   */
  error: boolean;
  /**
   * Set to true if the data is being loaded and show a skeleton loader.
   */
  loading: boolean;
  /**
   * The list of connections to render.
   */
  connections: ConnectionOverview[];
  /**
   * The main title of the content, shown before the connections.
   */
  i18nTitle: string;
  /**
   * The description of the content, shown before the connections.
   */
  i18nSubtitle: string;

  /**
   * The callback that's invoked to retrieve the `LocationDescriptor` used to
   * render the Select button link for each connection rendered.
   * @param connection
   */
  getConnectionHref(connection: ConnectionOverview): H.LocationDescriptor;
}

/**
 * A component to render a list of connections, to be used in the integration
 * editor.
 *
 * @see [error]{@link IIntegrationEditorChooseConnection#error}
 * @see [loading]{@link IIntegrationEditorChooseConnection#loading}
 * @see [connections]{@link IIntegrationEditorChooseConnection#connections}
 * @see [i18nTitle]{@link IIntegrationEditorChooseConnection#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationEditorChooseConnection#i18nSubtitle}
 * @see [getConnectionHref]{@link IIntegrationEditorChooseConnection#getConnectionHref}
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
          <IntegrationEditorConnectionsList>
            <WithLoader
              error={this.props.error}
              loading={this.props.loading}
              loaderChildren={
                <>
                  <div className={'list-group-item'}>
                    <div>
                      <IntegrationsListSkeleton />
                    </div>
                  </div>
                  <div className={'list-group-item'}>
                    <div>
                      <IntegrationsListSkeleton />
                    </div>
                  </div>
                  <div className={'list-group-item'}>
                    <div>
                      <IntegrationsListSkeleton />
                    </div>
                  </div>
                </>
              }
              errorChildren={<div>TODO</div>}
            >
              {() => (
                <>
                  {this.props.connections.map((c, idx) => (
                    <IntegrationEditorConnectionsListItem
                      key={idx}
                      integrationName={c.name}
                      integrationDescription={
                        c.description || 'No description available.'
                      }
                      icon={<img src={c.icon} width={24} height={24} />}
                      actions={
                        <Link
                          to={this.props.getConnectionHref(c)}
                          className={'btn btn-default'}
                        >
                          Select
                        </Link>
                      }
                    />
                  ))}
                  <IntegrationEditorConnectionsListItem
                    integrationName={''}
                    integrationDescription={''}
                    icon={''}
                    actions={
                      <Link to={'#'} className={'btn btn-default'}>
                        Create connection
                      </Link>
                    }
                  />
                </>
              )}
            </WithLoader>
          </IntegrationEditorConnectionsList>
        </div>
      </>
    );
  }
}
