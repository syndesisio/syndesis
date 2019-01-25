import { ConnectionOverview } from '@syndesis/models';
import { IntegrationsListSkeleton } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as H from 'history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationEditorChooseConnection {
  error: boolean;
  loading: boolean;
  connections: ConnectionOverview[];
  i18nTitle: string;
  i18nSubtitle: string;
  getConnectionHref(connection: ConnectionOverview): H.LocationDescriptor;
}

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
          <ListView>
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
                    <ListView.Item
                      key={idx}
                      heading={c.name}
                      description={c.description}
                      leftContent={<img src={c.icon} width={24} height={24} />}
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
                  <ListView.Item
                    actions={
                      <Link to={'#'} className={'btn btn-default'}>
                        Create connection
                      </Link>
                    }
                  />
                </>
              )}
            </WithLoader>
          </ListView>
        </div>
      </>
    );
  }
}
