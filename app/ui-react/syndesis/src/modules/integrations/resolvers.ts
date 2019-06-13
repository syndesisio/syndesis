/* tslint:disable:object-literal-sort-keys no-empty-interface */
import {
  getEmptyIntegration,
  getFlow,
  isIntegrationApiProvider,
} from '@syndesis/api';
import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import {
  makeResolver,
  makeResolverNoParams,
  makeResolverNoParamsWithDefaults,
} from '@syndesis/utils';
import {
  IBaseFlowRouteParams,
  IBaseRouteParams,
  IBaseRouteState,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
} from './components/editor/interfaces';
import {
  IEditorBase,
  IEditorIndex,
  IEditorWithOptionalFlow,
  makeEditorResolvers,
} from './components/editor/makeEditorResolvers';
import {
  IDetailsRouteParams,
  IDetailsRouteState,
} from './pages/detail/interfaces';
import routes from './routes';

export const configureIndexMapper = ({ flowId, integration }: IEditorIndex) => {
  flowId =
    flowId && getFlow(integration, flowId) ? flowId : integration.flows![0].id!;
  return {
    params: {
      flowId,
      integrationId: integration.id!,
    } as IBaseFlowRouteParams,
    state: {
      integration,
    } as IBaseRouteState,
  };
};

export const configureIndexOrApiProviderMapper = (
  indexRoute: string,
  apiProviderRoute: string
) => ({ flowId, integration }: IEditorWithOptionalFlow) => {
  return isIntegrationApiProvider(integration!)
    ? {
        params: {
          integrationId: integration.id!,
        } as IBaseFlowRouteParams,
        route: apiProviderRoute,
        state: {
          integration,
        } as IBaseRouteState,
      }
    : {
        params: {
          flowId: flowId ? flowId : integration.flows![0].id!,
          integrationId: integration.id!,
        } as IBaseFlowRouteParams,
        route: indexRoute,
        state: {
          integration,
        } as IBaseRouteState,
      };
};

export const configureSaveMapper = ({
  flowId,
  integration,
}: IEditorWithOptionalFlow) => ({
  params: {
    integrationId: integration.id!,
  } as ISaveIntegrationRouteParams,
  state: {
    flowId,
    integration,
  } as ISaveIntegrationRouteState,
});

export const configureApiProviderOperationsMapper = ({
  integration,
}: IEditorBase) => ({
  params: {
    integrationId: integration.id,
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
            integrationId: integration.id!,
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
      entryPoint: makeResolver<
        IEditorWithOptionalFlow,
        IBaseFlowRouteParams,
        IBaseRouteState
      >(
        routes.create.configure.index,
        configureIndexOrApiProviderMapper(
          routes.create.configure.index,
          routes.create.configure.operations
        )
      ),
      index: makeResolver<IEditorIndex, IBaseFlowRouteParams, IBaseRouteState>(
        routes.create.configure.index,
        configureIndexMapper
      ),
      operations: makeResolver<IEditorBase, IBaseRouteParams, IBaseRouteState>(
        routes.create.configure.operations,
        configureApiProviderOperationsMapper
      ),
      addStep: makeEditorResolvers(routes.create.configure.addStep),
      editStep: makeEditorResolvers(routes.create.configure.editStep),
      saveAndPublish: makeResolver<
        IEditorWithOptionalFlow,
        ISaveIntegrationRouteParams,
        ISaveIntegrationRouteState
      >(routes.create.configure.saveAndPublish, configureSaveMapper),
    },
  },
  integration: {
    root: makeResolverNoParams(routes.integration.root),
    activity: integrationActivityResolver,
    details: integrationDetailsResolver,
    edit: {
      root: makeResolver<IEditorIndex, IBaseFlowRouteParams, IBaseRouteState>(
        routes.integration.edit.root,
        configureIndexMapper
      ),
      entryPoint: makeResolver<
        IEditorWithOptionalFlow,
        IBaseFlowRouteParams,
        IBaseRouteState
      >(
        routes.integration.edit.index,
        configureIndexOrApiProviderMapper(
          routes.integration.edit.index,
          routes.integration.edit.operations
        )
      ),
      index: makeResolver<IEditorIndex, IBaseFlowRouteParams, IBaseRouteState>(
        routes.integration.edit.index,
        configureIndexMapper
      ),
      operations: makeResolver<IEditorBase, IBaseRouteParams, IBaseRouteState>(
        routes.integration.edit.operations,
        configureApiProviderOperationsMapper
      ),
      addStep: makeEditorResolvers(routes.integration.edit.addStep),
      editStep: makeEditorResolvers(routes.integration.edit.editStep),
      saveAndPublish: makeResolver<
        IEditorWithOptionalFlow,
        ISaveIntegrationRouteParams,
        ISaveIntegrationRouteState
      >(routes.integration.edit.saveAndPublish, configureSaveMapper),
    },
    metrics: metricsResolver,
  },
  import: makeResolverNoParams(routes.import),
};

export default resolvers;
