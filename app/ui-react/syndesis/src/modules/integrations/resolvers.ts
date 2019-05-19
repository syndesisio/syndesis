/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getEmptyIntegration } from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  makeResolver,
  makeResolverNoParams,
  makeResolverNoParamsWithDefaults,
} from '@syndesis/utils';
import {
  IBaseRouteParams,
  IBaseRouteState,
  IEditorIndex,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
} from './components/editor/interfaces';
import { makeEditorResolvers } from './components/editor/makeEditorResolvers';
import {
  IDetailsRouteParams,
  IDetailsRouteState,
} from './pages/detail/interfaces';
import routes from './routes';

export const configureIndexMapper = ({
  flowId,
  integration,
}: IEditorIndex) => ({
  params: {
    flowId: flowId ? flowId : integration.flows![0].id!,
    ...(integration && integration.id ? { integrationId: integration.id } : {}),
  } as IBaseRouteParams,
  state: {
    integration,
  } as IBaseRouteState,
});

// TODO: unit test every single one of these resolvers 😫

export const listResolver = makeResolverNoParams(routes.list);

export const manageCicdResolver = makeResolverNoParams(routes.manageCicd.root);

export const integrationActivityResolver = makeResolver<
  { integrationId: string; integration?: IIntegrationOverviewWithDraft },
  IDetailsRouteParams,
  IDetailsRouteState
>(routes.integration.activity, ({ integrationId, integration }) => ({
  params: {
    integrationId,
  },
  state: {
    integration,
  },
}));

export const integrationDetailsResolver = makeResolver<
  { integrationId: string; integration?: IIntegrationOverviewWithDraft },
  IDetailsRouteParams,
  IDetailsRouteState
>(routes.integration.details, ({ integrationId, integration }) => ({
  params: {
    integrationId,
  },
  state: {
    integration,
  },
}));

export const metricsResolver = makeResolver<
  { integrationId: string; integration?: IIntegrationOverviewWithDraft },
  IDetailsRouteParams,
  IDetailsRouteState
>(routes.integration.metrics, ({ integrationId, integration }) => ({
  params: {
    integrationId,
  },
  state: {
    integration,
  },
}));

const resolvers = {
  list: listResolver,
  manageCicd: {
    root: manageCicdResolver,
  },
  create: {
    root: makeResolverNoParams(routes.create.root),
    start: {
      ...makeEditorResolvers(routes.create.start),
      selectStep: makeResolverNoParamsWithDefaults<
        ISelectConnectionRouteParams,
        ISelectConnectionRouteState
      >(routes.create.start.selectStep, () => {
        const integration = getEmptyIntegration();
        return {
          params: {
            flowId: integration.flows![0].id!,
            position: '0',
          },
          state: {
            integration,
          },
        };
      }),
    },
    finish: makeEditorResolvers(routes.create.finish),
    configure: {
      root: makeResolverNoParams(routes.create.configure.root),
      index: makeResolver<IEditorIndex, IBaseRouteParams, IBaseRouteState>(
        routes.create.configure.index,
        configureIndexMapper
      ),
      addStep: makeEditorResolvers(routes.create.configure.addStep),
      editStep: makeEditorResolvers(routes.create.configure.editStep),
      saveAndPublish: makeResolver<
        IEditorIndex,
        ISaveIntegrationRouteParams,
        ISaveIntegrationRouteState
      >(routes.create.configure.saveAndPublish, configureIndexMapper),
    },
  },
  integration: {
    root: makeResolverNoParams(routes.integration.root),
    activity: integrationActivityResolver,
    details: integrationDetailsResolver,
    edit: {
      root: makeResolver<IEditorIndex, IBaseRouteParams, IBaseRouteState>(
        routes.integration.edit.root,
        configureIndexMapper
      ),
      index: makeResolver<IEditorIndex, IBaseRouteParams, IBaseRouteState>(
        routes.integration.edit.index,
        configureIndexMapper
      ),
      addStep: makeEditorResolvers(routes.integration.edit.addStep),
      editStep: makeEditorResolvers(routes.integration.edit.editStep),
      saveAndPublish: makeResolver<
        IEditorIndex,
        ISaveIntegrationRouteParams,
        ISaveIntegrationRouteState
      >(routes.integration.edit.saveAndPublish, configureIndexMapper),
    },
    metrics: metricsResolver,
  },
  import: makeResolverNoParams(routes.import),
};

export default resolvers;
