import { makeResolver, makeResolverNoParams } from '@syndesis/utils';

/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Connector } from '@syndesis/models';
import { IDetailsPageRouteState } from './pages/create/DetailsPage';
import { IEditSpecificationRouteState } from './pages/create/EditSpecificationPage';
import { IReviewActionsRouteState } from './pages/create/ReviewActionsPage';
import { ISecurityPageRouteState } from './pages/create/SecurityPage';
import { IServicePortRouteState } from './pages/create/ServicePortPage';
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
    >(
      routes.create.review,
      ({ specification, url, connectorTemplateId, configured }) => ({
        state: {
          configured,
          connectorTemplateId,
          specification,
          url,
        },
      })
    ),
    specification: makeResolver<
      IEditSpecificationRouteState,
      null,
      IEditSpecificationRouteState
    >(routes.create.specification, ({ specification }) => ({
      state: {
        specification,
      },
    })),
    servicePort: makeResolver<
      IServicePortRouteState,
      null,
      IServicePortRouteState
    >(
      routes.create.servicePort,
      ({
        apiSummary,
        connectorTemplateId,
        configured,
        specification,
        url,
      }) => ({
        state: {
          apiSummary,
          configured,
          connectorTemplateId,
          specification,
          url,
        },
      })
    ),
    security: makeResolver<
      ISecurityPageRouteState,
      null,
      ISecurityPageRouteState
    >(
      routes.create.security,
      ({
        configured,
        connectorTemplateId,
        apiSummary,
        specification,
        url,
      }) => ({
        state: {
          configured,
          connectorTemplateId,
          apiSummary,
          specification,
          url,
        },
      })
    ),
    save: makeResolver<IDetailsPageRouteState, null, IDetailsPageRouteState>(
      routes.create.save,
      ({
        configured,
        connectorTemplateId,
        apiSummary,
        specification,
        url,
      }) => ({
        state: {
          configured,
          connectorTemplateId,
          apiSummary,
          specification,
          url,
        },
      })
    ),
  },
  list: makeResolverNoParams(routes.list),
};
