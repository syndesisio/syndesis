import { WithUser } from '@syndesis/api';
import { IntegrationMonitoring } from '@syndesis/models';
import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { AppContext } from './AppContext';
import { UI } from './UI';
import { IConfigFile } from './WithConfig';

export type IAppRoutes = Array<IAppRoute | IAppRouteWithChildrens>;

export interface IAppRoute {
  component:
    | React.ComponentType<RouteComponentProps<any>>
    | React.ComponentType<any>;
  exact?: boolean;
  label: string;
  to: string;
  kind: 'route';
}
export interface IAppRouteWithChildrens {
  label: string;
  childrens: IAppRoute[];
  kind: 'route-with-childrens';
}

export interface IAppBaseProps {
  config: IConfigFile;
  routes: IAppRoutes;
}

export const App: React.FunctionComponent<IAppBaseProps> = ({
  config,
  routes,
}) => {
  const getPodLogUrl = (
    monitoring: IntegrationMonitoring | undefined
  ): string | undefined => {
    if (
      !config ||
      !monitoring ||
      !monitoring.linkType ||
      !monitoring.namespace ||
      !monitoring.podName
    ) {
      return undefined;
    }
    const podLogUrl = `${config.consoleUrl}/k8s/ns/${
      monitoring.namespace
    }/pods/${monitoring.podName}/logs`;
    return podLogUrl;
  };

  return (
    <WithUser>
      {({ data }) => (
        <AppContext.Provider
          value={{
            config,
            getPodLogUrl,
            user: data,
          }}
        >
          <UI routes={routes} />
        </AppContext.Provider>
      )}
    </WithUser>
  );
};
