import { IntegrationWithMonitoring } from "@syndesis/models";
import {
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListSkeleton,
  IListViewToolbarProps,
  ListViewToolbar
} from "@syndesis/ui";
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationsListViewProps extends IListViewToolbarProps {
  match: any;
  loading: boolean;
  monitoredIntegrations: IntegrationWithMonitoring[];
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
              to={`${this.props.match}/import`}
              className={'btn btn-default'}
            >
              Import
            </Link>
            <Link
              to={`${this.props.match}/new`}
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
                (mi: IntegrationWithMonitoring, index) => (
                  <IntegrationsListItem
                    integrationId={mi.integration.id!}
                    integrationName={mi.integration.name!}
                    currentState={mi.integration.currentState!}
                    targetState={mi.integration.targetState!}
                    isConfigurationRequired={
                      !!(
                        mi.integration!.board!.warnings ||
                        mi.integration!.board!.errors ||
                        mi.integration!.board!.notices
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
