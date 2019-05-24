import * as React from 'react';
import { Route, Switch } from 'react-router';
import { SupportPage } from './pages/SupportPage';
import routes from './routes';

export class SupportModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.root} exact={true} component={SupportPage} />
      </Switch>
    );
  }
}
