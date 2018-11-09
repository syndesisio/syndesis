import { IIntegration, IIntegrationMonitoring, IMonitoredIntegration } from "@syndesis/models";
import * as React from 'react';
import { IRestState } from "./Rest";
import { SyndesisRest } from "./SyndesisRest";
import { WithIntegrations } from "./WithIntegrations";

export interface IMonitoredIntegrationsResponse {
  items: IMonitoredIntegration[];
  totalCount: number;
}

export interface IWithMonitoredIntegrationsProps {
  children(props: IRestState<IMonitoredIntegrationsResponse>): any;
}

export class WithMonitoredIntegrations extends React.Component<
  IWithMonitoredIntegrationsProps
> {
  public render() {
    return (
      <WithIntegrations>
        {({ data: integrations, ...props }) => (
          <SyndesisRest<IIntegrationMonitoring[]>
            url={'/api/v1/monitoring/integrations'}
            poll={5000}
            defaultValue={[]}
          >
            {({ data: monitorings }) => {
              return this.props.children({
                ...props,
                data: {
                  items: integrations.items.map(
                    (i: IIntegration): IMonitoredIntegration => ({
                      integration: i,
                      monitoring: monitorings.find(
                        (m: IIntegrationMonitoring) => m.integrationId === i.id
                      )
                    })
                  ),
                  totalCount: integrations.totalCount
                }
              });
            }}
          </SyndesisRest>
        )}
      </WithIntegrations>
    );
  }
}
