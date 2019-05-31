import * as React from 'react';
import { Route, Switch } from 'react-router';
import { WithClosedNavigation } from '../../shared';
import { DetailsPage } from './pages/create/DetailsPage';
import { EditSpecificationPage } from './pages/create/EditSpecificationPage';
import { ReviewActionsPage } from './pages/create/ReviewActionsPage';
import { SecurityPage } from './pages/create/SecurityPage';
import { SelectMethodPage } from './pages/create/SelectMethodPage';
import routes from './routes';

export default class ApiConnectorCreatorApp extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <Switch>
          <Route
            path={routes.create.upload}
            exact={true}
            component={SelectMethodPage}
          />
          <Route
            path={routes.create.review}
            exact={true}
            component={ReviewActionsPage}
          />
          <Route
            path={routes.create.specification}
            exact={true}
            component={EditSpecificationPage}
          />
          <Route
            path={routes.create.security}
            exact={true}
            component={SecurityPage}
          />
          <Route
            path={routes.create.save}
            exact={true}
            component={DetailsPage}
          />
        </Switch>
      </WithClosedNavigation>
    );
  }
}
