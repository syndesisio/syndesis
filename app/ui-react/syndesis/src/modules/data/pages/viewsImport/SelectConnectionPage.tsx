import { WithVirtualizationConnectionStatuses } from '@syndesis/api';
import { RestDataService } from '@syndesis/models';
import { ViewsImportLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import resolvers from '../../../resolvers';
import { DvConnectionsWithToolbar, ViewsImportSteps } from '../../shared';

/**
 * @param virtualizationId - the ID of the virtualization for the wizard
 */
export interface ISelectConnectionRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 */
export interface ISelectConnectionRouteState {
  virtualization: RestDataService;
}

export interface ISelectConnectionPageState {
  selectedConnection: any;
}

export class SelectConnectionPage extends React.Component<
  {},
  ISelectConnectionPageState
> {
  public constructor(props: {}) {
    super(props);
    this.state = {
      selectedConnection: '', // initial selected connection empty
    };
    this.handleConnectionSelectionChanged = this.handleConnectionSelectionChanged.bind(
      this
    );
  }

  public handleConnectionSelectionChanged(name: string, selected: boolean) {
    const selConn = selected ? name : '';
    this.setState({
      selectedConnection: selConn,
    });
  }

  public render() {
    const connectionId: string = this.state.selectedConnection;
    return (
      <WithRouteData<ISelectConnectionRouteParams, ISelectConnectionRouteState>>
        {({ virtualizationId }, { virtualization }, { history }) => (
          <ViewsImportLayout
            header={<ViewsImportSteps step={1} />}
            content={
              <WithVirtualizationConnectionStatuses>
                {({ data, hasData, error }) => (
                  <DvConnectionsWithToolbar
                    error={error}
                    loading={!hasData}
                    dvSourceStatuses={data}
                    onConnectionSelectionChanged={
                      this.handleConnectionSelectionChanged
                    }
                  />
                )}
              </WithVirtualizationConnectionStatuses>
            }
            cancelHref={resolvers.data.virtualizations.views.root({
              virtualization,
            })}
            nextHref={resolvers.data.virtualizations.views.importSource.selectViews(
              {
                connectionId,
                virtualization,
              }
            )}
            isNextDisabled={this.state.selectedConnection.length < 1}
            isNextLoading={false}
            isLastStep={false}
          />
        )}
      </WithRouteData>
    );
  }
}
