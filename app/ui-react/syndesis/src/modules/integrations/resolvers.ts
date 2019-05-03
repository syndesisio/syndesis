/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { getEmptyIntegration, getStep } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
import { IIntegrationOverviewWithDraft } from '@syndesis/models/src';
import {
  makeResolver,
  makeResolverNoParams,
  makeResolverNoParamsWithDefaults,
} from '@syndesis/utils';
import {
  IBaseRouteParams,
  IBaseRouteState,
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState,
  ISelectActionRouteParams,
  ISelectActionRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
} from './components/editor/interfaces';
import {
  IDetailsRouteParams,
  IDetailsRouteState,
} from './pages/detail/interfaces';
import routes from './routes';

interface IEditorIndex {
  flowId: string;
  integration: Integration;
}

interface IEditorSelectConnection extends IEditorIndex {
  position: string;
}

interface IEditorSelectAction extends IEditorSelectConnection {
  connection: ConnectionOverview;
}

interface IEditorConfigureAction extends IEditorSelectAction {
  actionId: string;
  step?: string;
  updatedIntegration?: Integration;
}

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

export const configureSelectConnectionMapper = ({
  position,
  ...rest
}: IEditorSelectConnection) => {
  const { params, state } = configureIndexMapper(rest);
  return {
    params: {
      ...params,
      position,
    } as ISelectConnectionRouteParams,
    state,
  };
};

export const configureSelectActionMapper = ({
  connection,
  ...rest
}: IEditorSelectAction) => {
  const { params, state } = configureSelectConnectionMapper(rest);
  return {
    params: {
      ...params,
      connectionId: connection.id!,
    } as ISelectActionRouteParams,
    state: {
      ...state,
      connection,
    } as ISelectActionRouteState,
  };
};

export const configureConfigureActionMapper = ({
  actionId,
  flowId,
  step,
  integration,
  updatedIntegration,
  position,
  ...rest
}: IEditorConfigureAction) => {
  const { params, state } = configureSelectActionMapper({
    ...rest,
    flowId,
    integration,
    position,
  });
  const positionAsNumber = parseInt(position, 10);
  const stepObject = getStep(integration, flowId, positionAsNumber) || {};
  return {
    params: {
      ...params,
      actionId,
      step: `${step || 0}`,
    } as IConfigureActionRouteParams,
    state: {
      ...state,
      updatedIntegration,
      configuredProperties: stepObject.configuredProperties || {},
    } as IConfigureActionRouteState,
  };
};

// TODO: unit test every single one of these resolvers 😫

export const listResolver = makeResolverNoParams(routes.list);

export const manageCicdResolver = makeResolverNoParams(routes.manageCicd.root);

export const createRootResolver = makeResolverNoParams(routes.create.root);

export const createStartSelectStepResolver = makeResolverNoParamsWithDefaults<
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
});

export const createStartSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(routes.create.start.connection.selectAction, configureSelectActionMapper);

export const createStartConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.start.connection.configureAction,
  configureConfigureActionMapper
);

export const createFinishSelectStepResolver = makeResolver<
  IEditorSelectConnection,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState
>(routes.create.finish.selectStep, configureSelectConnectionMapper);

export const createFinishSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(routes.create.finish.connection.selectAction, configureSelectActionMapper);

export const createFinishConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.finish.connection.configureAction,
  configureConfigureActionMapper
);

export const createConfigureIndexResolver = makeResolver<
  IEditorIndex,
  IBaseRouteParams,
  IBaseRouteState
>(routes.create.configure.index, configureIndexMapper);

export const createConfigureAddStepSelectStepResolver = makeResolver<
  IEditorSelectConnection,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState
>(routes.create.configure.addStep.selectStep, configureSelectConnectionMapper);

export const createConfigureAddStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(
  routes.create.configure.addStep.connection.selectAction,
  configureSelectActionMapper
);

export const createConfigureAddStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.configure.addStep.connection.configureAction,
  configureConfigureActionMapper
);

export const createConfigureEditStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(
  routes.create.configure.editStep.connection.selectAction,
  configureSelectActionMapper
);

export const createConfigureEditStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.create.configure.editStep.connection.configureAction,
  configureConfigureActionMapper
);

export const createConfigureEditStepSaveAndPublishResolver = makeResolver<
  IEditorIndex,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState
>(routes.create.configure.saveAndPublish, configureIndexMapper);

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

export const integrationEditIndexResolver = makeResolver<
  IEditorIndex,
  IBaseRouteParams,
  IBaseRouteState
>(routes.integration.edit.index, configureIndexMapper);

export const integrationEditAddStepSelectStepResolver = makeResolver<
  IEditorSelectConnection,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState
>(routes.integration.edit.addStep.selectStep, configureSelectConnectionMapper);

export const integrationEditAddStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectConnectionRouteState
>(
  routes.integration.edit.addStep.connection.selectAction,
  configureSelectActionMapper
);

export const integrationEditAddStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.integration.edit.addStep.connection.configureAction,
  configureConfigureActionMapper
);

export const integrationEditEditStepSelectActionResolver = makeResolver<
  IEditorSelectAction,
  ISelectActionRouteParams,
  ISelectActionRouteState
>(
  routes.integration.edit.editStep.connection.selectAction,
  configureSelectActionMapper
);

export const integrationEditEditStepConfigureActionResolver = makeResolver<
  IEditorConfigureAction,
  IConfigureActionRouteParams,
  IConfigureActionRouteState
>(
  routes.integration.edit.editStep.connection.configureAction,
  configureConfigureActionMapper
);

export const integrationEditSaveAndPublish = makeResolver<
  IEditorIndex,
  ISaveIntegrationRouteParams,
  ISaveIntegrationRouteState
>(routes.integration.edit.saveAndPublish, configureIndexMapper);

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

type RouteResolver<T> = {
  [K in keyof T]: T[K] extends string ? any : RouteResolver<T[K]>
};

const resolvers: RouteResolver<typeof routes> = {
  list: listResolver,
  manageCicd: {
    root: manageCicdResolver,
  },
  create: {
    root: createRootResolver,
    start: {
      selectStep: createStartSelectStepResolver,
      connection: {
        selectAction: createStartSelectActionResolver,
        configureAction: createStartConfigureActionResolver,
        describeData: () => 'describeData',
      },
      apiProvider: {
        upload: makeResolverNoParams(routes.create.start.apiProvider.upload),
        review: () => 'review',
        edit: () => 'edit',
      },
      basicFilter: () => 'basicFilter',
      dataMapper: () => 'dataMapper',
      template: () => 'template',
      step: () => 'step',
      extension: () => 'extension',
    },
    finish: {
      selectStep: createFinishSelectStepResolver,
      connection: {
        selectAction: createFinishSelectActionResolver,
        configureAction: createFinishConfigureActionResolver,
        describeData: () => 'describeData',
      },
      apiProvider: {
        upload: makeResolverNoParams(routes.create.finish.apiProvider.upload),
        review: () => 'review',
        edit: () => 'edit',
      },
      basicFilter: () => 'basicFilter',
      dataMapper: () => 'dataMapper',
      template: () => 'template',
      step: () => 'step',
      extension: () => 'extension',
    },
    configure: {
      root: createRootResolver,
      index: createConfigureIndexResolver,
      addStep: {
        selectStep: createConfigureAddStepSelectStepResolver,
        connection: {
          selectAction: createConfigureAddStepSelectActionResolver,
          configureAction: createConfigureAddStepConfigureActionResolver,
          describeData: () => 'describeData',
        },
        apiProvider: {
          upload: makeResolverNoParams(
            routes.create.configure.addStep.apiProvider.upload
          ),
          review: () => 'review',
          edit: () => 'edit',
        },
        basicFilter: () => 'basicFilter',
        dataMapper: () => 'dataMapper',
        template: () => 'template',
        step: () => 'step',
        extension: () => 'extension',
      },
      editStep: {
        selectStep: () => {
          throw new Error('this route should not be used');
        },
        connection: {
          selectAction: createConfigureEditStepSelectActionResolver,
          configureAction: createConfigureEditStepConfigureActionResolver,
          describeData: () => 'describeData',
        },
        apiProvider: {
          upload: makeResolverNoParams(
            routes.create.configure.editStep.apiProvider.upload
          ),
          review: () => 'review',
          edit: () => 'edit',
        },
        basicFilter: () => 'basicFilter',
        dataMapper: () => 'dataMapper',
        template: () => 'template',
        step: () => 'step',
        extension: () => 'extension',
      },
      saveAndPublish: createConfigureEditStepSaveAndPublishResolver,
    },
  },
  integration: {
    root: createRootResolver,
    activity: integrationActivityResolver,
    details: integrationDetailsResolver,
    edit: {
      root: createRootResolver,
      index: integrationEditIndexResolver,
      addStep: {
        selectStep: integrationEditAddStepSelectStepResolver,
        connection: {
          selectAction: integrationEditAddStepSelectActionResolver,
          configureAction: integrationEditAddStepConfigureActionResolver,
          describeData: () => 'describeData',
        },
        apiProvider: {
          upload: makeResolverNoParams(
            routes.integration.edit.addStep.apiProvider.upload
          ),
          review: () => 'review',
          edit: () => 'edit',
        },
        basicFilter: () => 'basicFilter',
        dataMapper: () => 'dataMapper',
        template: () => 'template',
        step: () => 'step',
        extension: () => 'extension',
      },
      editStep: {
        selectStep: () => {
          throw new Error('this route should not be used');
        },
        connection: {
          selectAction: integrationEditEditStepSelectActionResolver,
          configureAction: integrationEditEditStepConfigureActionResolver,
          describeData: () => 'describeData',
        },
        apiProvider: {
          upload: makeResolverNoParams(
            routes.integration.edit.editStep.apiProvider.upload
          ),
          review: () => 'review',
          edit: () => 'edit',
        },
        basicFilter: () => 'basicFilter',
        dataMapper: () => 'dataMapper',
        template: () => 'template',
        step: () => 'step',
        extension: () => 'extension',
      },
      saveAndPublish: integrationEditSaveAndPublish,
    },
    metrics: metricsResolver,
  },
  import: makeResolverNoParams(routes.import),
};

export default resolvers;
