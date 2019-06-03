/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connector } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import { IDetailsPageRouteState } from './pages/create/DetailsPage';
import { IEditSpecificationRouteState } from './pages/create/EditSpecificationPage';
import { IReviewActionsRouteState } from './pages/create/ReviewActionsPage';
import { ISecurityPageRouteState } from './pages/create/SecurityPage';
import routes from './routes';

export default {
  apiConnector: {
    details: makeResolver<{ apiConnector: Connector }>(
      routes.apiConnector.details,
      ({ apiConnector }) => ({
        params: {
          apiConnectorId: apiConnector.id,
        },
        state: {
          apiConnector,
        },
      })
    ),
    edit: makeResolver<{ apiConnector: Connector }>(
      routes.apiConnector.edit,
      ({ apiConnector }) => ({
        params: {
          apiConnectorId: apiConnector.id,
        },
        state: {
          apiConnector,
        },
      })
    ),
  },
  create: {
    upload: makeResolverNoParams(routes.create.upload),
    review: makeResolver<
      IReviewActionsRouteState,
      null,
      IReviewActionsRouteState
    >(routes.create.review, ({ specification }) => ({
      state: {
        specification,
      },
    })),
    specification: makeResolver<
      IEditSpecificationRouteState,
      null,
      IEditSpecificationRouteState
    >(routes.create.specification, ({ specification }) => ({
      state: {
        specification,
      },
    })),
    security: makeResolver<
      ISecurityPageRouteState,
      null,
      ISecurityPageRouteState
    >(routes.create.security, ({ specification }) => ({
      state: {
        specification,
      },
    })),
    save: makeResolver<IDetailsPageRouteState, null, IDetailsPageRouteState>(
      routes.create.save,
      ({
        authenticationType,
        authorizationEndpoint,
        specification,
        tokenEndpoint,
      }) => ({
        state: {
          authenticationType,
          authorizationEndpoint,
          specification,
          tokenEndpoint,
        },
      })
    ),
  },
  list: makeResolverNoParams(routes.list),
};
