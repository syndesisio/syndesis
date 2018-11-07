import { IMonitoredIntegration } from '@syndesis/app/containers';
import {
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListSkeleton
} from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from './ListViewToolbar';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  match: any;
  loading: boolean;
  monitoredIntegrations: IMonitoredIntegration[];
}

export class IntegrationsListView extends React.Component<
  IIntegrationsListViewProps
> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <Link
              to={`${this.props.match.url}/import`}
              className={'btn btn-default'}
            >
              Import
            </Link>
            <Link
              to={`${this.props.match.url}/new`}
              className={'btn btn-primary'}
            >
              Create Integration
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>
          {this.props.loading ? (
            <IntegrationsListSkeleton
              width={800}
              style={{
                backgroundColor: '#FFF',
                marginTop: 30
              }}
            />
          ) : (
            <IntegrationsList>
              {this.props.monitoredIntegrations.map(
                (mi: IMonitoredIntegration, index) => (
                  <IntegrationsListItem
                    integrationId={mi.integration.id}
                    integrationName={mi.integration.name}
                    currentState={mi.integration.currentState}
                    targetState={mi.integration.targetState}
                    isConfigurationRequired={
                      !!(
                        mi.integration.board.warnings ||
                        mi.integration.board.errors ||
                        mi.integration.board.notices
                      )
                    }
                    monitoringValue={
                      mi.monitoring && mi.monitoring.detailedState.value
                    }
                    monitoringCurrentStep={
                      mi.monitoring && mi.monitoring.detailedState.currentStep
                    }
                    monitoringTotalSteps={
                      mi.monitoring && mi.monitoring.detailedState.totalSteps
                    }
                    key={index}
                  />
                )
              )}
            </IntegrationsList>
          )}
        </div>
      </>
    );
  }
}
