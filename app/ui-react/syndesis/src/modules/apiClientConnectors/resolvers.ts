/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connector } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import { IEditSpecificationRouteState } from './pages/create/EditSpecificationPage';
import { IReviewActionsRouteState } from './pages/create/ReviewActionsPage';
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
      IReviewActionsRouteState
    >(routes.create.specification, ({ specification }) => ({
      state: {
        specification,
      },
    })),
    security: makeResolverNoParams(routes.create.security),
    save: makeResolverNoParams(routes.create.save),
  },
  list: makeResolverNoParams(routes.list),
};
