import { Activity, IntegrationDeployment } from '@syndesis/models';
import * as React from 'react';
import { SyndesisFetch } from './SyndesisFetch';

export interface IIntegrationDeploymentResponse {
  items: IntegrationDeployment[];
  totalCount: number;
}

export interface IActivitiesAndDeploymentsChildrenProps {
  activities: Activity[];
  deployments: IntegrationDeployment[];
  fetchDeployments: () => Promise<void>;
  fetchActivities: () => Promise<void>;
}

export interface IWithActivitiesProps {
  integrationId: string;
  children(props: IActivitiesAndDeploymentsChildrenProps): any;
}

export class WithActivities extends React.Component<IWithActivitiesProps> {
  public render() {
    return (
      <SyndesisFetch<IIntegrationDeploymentResponse>
        url={`/integrations/${this.props.integrationId}/deployments`}
        defaultValue={{ items: [], totalCount: 0 }}
      >
        {({ read: fetchDeployments, response: deployments }) => (
          <SyndesisFetch<Activity[]>
            url={`/activity/integrations/${this.props.integrationId}`}
            defaultValue={[]}
          >
            {({ read: fetchActivities, response: activities }) => {
              return this.props.children({
                activities: activities.data,
                deployments: deployments.data.items,
                fetchActivities,
                fetchDeployments,
              });
            }}
          </SyndesisFetch>
        )}
      </SyndesisFetch>
    );
  }
}
