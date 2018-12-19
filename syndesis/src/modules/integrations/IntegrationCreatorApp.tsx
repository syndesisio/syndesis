import * as React from 'react';
import { Route, Switch } from 'react-router';
import IntegrationCreatorStep0Page from './pages/IntegrationCreatorStep0Page';

export interface IIntegrationCreatorAppProps {
  baseurl: string;
}

export default class IntegrationCreatorApp extends React.Component<
  IIntegrationCreatorAppProps
> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.baseurl}
          exact={true}
          component={IntegrationCreatorStep0Page}
        />
      </Switch>
    );
  }
}
