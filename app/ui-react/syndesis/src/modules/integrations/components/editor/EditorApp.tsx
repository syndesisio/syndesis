/* tslint:disable:object-literal-sort-keys */
import { NEW_INTEGRATION_ID } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration } from '@syndesis/models';
import * as React from 'react';
import { EditSpecificationPage } from './apiProvider/EditSpecificationPage';
import { ReviewActionsPage } from './apiProvider/ReviewActionsPage';
import { SelectMethodPage } from './apiProvider/SelectMethodPage';
import { ChoiceStepPage } from './choice/ChoiceStepPage';
import { DescribeChoiceDataShapePage } from './choice/DescribeChoiceDataShapePage';
import { SelectChoiceConfigModePage } from "./choice/SelectChoiceConfigModePage";
import { DataMapperPage } from './dataMapper/DataMapperPage';
import { EditorRoutes } from './EditorRoutes';
import { EditorSidebar } from './EditorSidebar';
import { ConfigureActionPage } from './endpoint/ConfigureActionPage';
import { DescribeDataShapePage } from './endpoint/DescribeDataShapePage';
import { SelectActionPage } from './endpoint/SelectActionPage';
import {
  IBaseFlowRouteParams,
  IBaseRouteState,
  IChoiceStepRouteParams,
  IChoiceStepRouteState,
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  IDescribeDataShapeRouteParams,
  IDescribeDataShapeRouteState,
  IPageWithEditorBreadcrumb,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  stepRoutes,
} from './interfaces';
import { makeEditorResolvers } from './makeEditorResolvers';
import { RuleFilterStepPage } from './ruleFilter/RuleFilterStepPage';
import { SelectConnectionPage } from './SelectConnectionPage';
import { ConfigureStepPage } from './step/ConfigureStepPage';
import { TemplateStepPage } from './template/TemplateStepPage';

export interface IEditorApp extends IPageWithEditorBreadcrumb {
  mode: 'adding' | 'editing';
  appStepRoutes: typeof stepRoutes;
  appResolvers: ReturnType<typeof makeEditorResolvers>;
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  postConfigureHref: (
    integration: Integration,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState,
    isApiProvider?: boolean
  ) => H.LocationDescriptorObject;
}

export const EditorApp: React.FunctionComponent<IEditorApp> = ({
  mode,
  appStepRoutes,
  appResolvers,
  cancelHref,
  postConfigureHref,
  getBreadcrumb,
}) => {
  const selectStepChildren = (
    <SelectConnectionPage
      cancelHref={cancelHref}
      apiProviderHref={(step, params, state) =>
        appResolvers.apiProvider.selectMethod({
          ...params,
          ...state,
        })
      }
      connectionHref={(connection, params, state) =>
        appResolvers.connection.selectAction({
          connection,
          ...params,
          ...state,
        })
      }
      filterHref={(step, params, state) =>
        appResolvers.basicFilter({
          step,
          ...params,
          ...state,
        })
      }
      choiceHref={(step, params, state) =>
        appResolvers.choice.selectMode({
          step,
          ...params,
          ...state,
        })
      }
      mapperHref={(step, params, state) =>
        appResolvers.dataMapper({
          step,
          ...params,
          ...state,
        })
      }
      templateHref={(step, params, state) =>
        appResolvers.template({
          step,
          ...params,
          ...state,
        })
      }
      stepHref={(step, params, state) =>
        appResolvers.step({
          step,
          ...params,
          ...state,
        })
      }
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      getBreadcrumb={getBreadcrumb}
      isAdding={mode === 'adding'}
    />
  );

  const selectActionPage = (
    <SelectActionPage
      cancelHref={(p, s) => appResolvers.selectStep({ ...p, ...s })}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      selectHref={(actionId, p, s) =>
        appResolvers.connection.configureAction({
          actionId,
          ...p,
          ...s,
        })
      }
      getBreadcrumb={getBreadcrumb}
    />
  );

  const configureActionPage = (
    <ConfigureActionPage
      backHref={(p, s) => appResolvers.connection.selectAction({ ...p, ...s })}
      cancelHref={cancelHref}
      mode={mode}
      nextPageHref={(p, s) =>
        appResolvers.connection.configureAction({
          ...p,
          ...s,
        })
      }
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      postConfigureHref={(requiresDataShape, integration, p, s) => {
        if (requiresDataShape) {
          return appResolvers.connection.describeData({
            integration,
            ...(p as IDescribeDataShapeRouteParams),
            ...(s as IDescribeDataShapeRouteState),
          });
        } else {
          return postConfigureHref(
            integration,
            p as IConfigureActionRouteParams,
            s as IConfigureActionRouteState
          );
        }
      }}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const describeDataShapePage = (
    <DescribeDataShapePage
      mode={mode}
      cancelHref={cancelHref}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      backHref={(page, p, s) =>
        page === 'configureAction'
          ? appResolvers.connection.configureAction({
              ...(p as IConfigureActionRouteParams),
              ...(s as IConfigureActionRouteState),
            })
          : appResolvers.connection.describeData({
              ...(p as IDescribeDataShapeRouteParams),
              ...(s as IDescribeDataShapeRouteState),
            })
      }
      postConfigureHref={(page, integration, p, s) =>
        page === 'describeData'
          ? appResolvers.connection.describeData({
              ...(p as IDescribeDataShapeRouteParams),
              ...(s as IDescribeDataShapeRouteState),
            })
          : postConfigureHref(
              integration,
              p as IConfigureActionRouteParams,
              s as IConfigureActionRouteState
            )
      }
      getBreadcrumb={getBreadcrumb}
    />
  );

  const describeChoiceDataShapePage = (
    <DescribeChoiceDataShapePage
      cancelHref={cancelHref}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      backHref={(p, s) => {
        return appResolvers.choice.configure({
          ...(p as IChoiceStepRouteParams),
          ...(s as IChoiceStepRouteState),
        });
      }}
      postConfigureHref={postConfigureHref}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const templateStepPage = (
    <TemplateStepPage
      mode={mode}
      cancelHref={cancelHref}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      postConfigureHref={postConfigureHref}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const configureStepPage = (
    <ConfigureStepPage
      cancelHref={cancelHref}
      mode={mode}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      postConfigureHref={postConfigureHref}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const dataMapperPage = (
    <DataMapperPage
      cancelHref={cancelHref}
      mode={mode}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      postConfigureHref={postConfigureHref}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const basicFilterPage = (
    <RuleFilterStepPage
      cancelHref={cancelHref}
      mode={mode}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      postConfigureHref={postConfigureHref}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const choicePage = (
    <ChoiceStepPage
      backHref={(p, s) => {
        return appResolvers.choice.selectMode({
          ...(p as IChoiceStepRouteParams),
          ...(s as IChoiceStepRouteState),
        });
      }}
      cancelHref={cancelHref}
      mode={mode}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      postConfigureHref={(integration, p, s) => {
        return appResolvers.choice.describeData({
          integration,
          ...(p as IChoiceStepRouteParams),
          ...(s as IChoiceStepRouteState),
        });
      }}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const selectChoiceConfigModePage = (
    <SelectChoiceConfigModePage
      cancelHref={cancelHref}
      sidebar={props => (
        <EditorSidebar {...props} isAdding={mode === 'adding'} />
      )}
      selectHref={(p, s) => {
        return appResolvers.choice.configure({
          ...(p as IChoiceStepRouteParams),
          ...(s as IChoiceStepRouteState),
        });
      }}
      getBreadcrumb={getBreadcrumb}
    />
  );

  const selectMethodPage = (
    <SelectMethodPage
      cancelHref={(params, state) =>
        appResolvers.selectStep({ ...params, ...state })
      }
      getReviewHref={(specification, params, state) =>
        appResolvers.apiProvider.reviewActions({
          specification,
          ...params,
          ...state,
        })
      }
      getEditorHref={(specification, params, state) =>
        appResolvers.apiProvider.editSpecification({
          specification,
          ...params,
          ...state,
        })
      }
      getBreadcrumb={getBreadcrumb}
    />
  );

  const reviewActionsPage = (
    <ReviewActionsPage
      cancelHref={(params, state) =>
        appResolvers.apiProvider.selectMethod({ ...params, ...state })
      }
      editHref={(params, state) =>
        appResolvers.apiProvider.editSpecification({ ...params, ...state })
      }
      nextHref={(integration, params, state) =>
        postConfigureHref(
          integration,
          {
            ...params,
            ...state,
          },
          state,
          true
        )
      }
      getBreadcrumb={getBreadcrumb}
    />
  );

  const editSpecificationPage = (
    <EditSpecificationPage
      cancelHref={(params, state) =>
        state.integration.id === NEW_INTEGRATION_ID
          ? appResolvers.apiProvider.selectMethod({ ...params, ...state })
          : cancelHref(params, state)
      }
      saveHref={(params, state) =>
        appResolvers.apiProvider.reviewActions({ ...params, ...state })
      }
      getBreadcrumb={getBreadcrumb}
    />
  );

  return (
    <EditorRoutes
      selectStepPath={appStepRoutes.selectStep}
      selectStepChildren={selectStepChildren}
      endpointEditor={{
        selectActionPath: appStepRoutes.connection.selectAction,
        selectActionChildren: selectActionPage,
        configureActionPath: appStepRoutes.connection.configureAction,
        configureActionChildren: configureActionPage,
        describeDataPath: appStepRoutes.connection.describeData,
        describeDataChildren: describeDataShapePage,
      }}
      apiProvider={{
        selectMethodPath: appStepRoutes.apiProvider.selectMethod,
        selectMethodChildren: selectMethodPage,
        reviewActionsPath: appStepRoutes.apiProvider.reviewActions,
        reviewActionsChildren: reviewActionsPage,
        editSpecificationPath: appStepRoutes.apiProvider.editSpecification,
        editSpecificationChildren: editSpecificationPage,
      }}
      template={{
        templatePath: appStepRoutes.template,
        templateChildren: templateStepPage,
      }}
      dataMapper={{
        mapperPath: appStepRoutes.dataMapper,
        mapperChildren: dataMapperPage,
      }}
      basicFilter={{
        basicFilterPath: appStepRoutes.basicFilter,
        basicFilterChildren: basicFilterPage,
      }}
      choice={{
        configurePath: appStepRoutes.choice.configure,
        configureChildren: choicePage,
        describeDataPath: appStepRoutes.choice.describeData,
        describeDataChildren: describeChoiceDataShapePage,
        selectModePath: appStepRoutes.choice.selectMode,
        selectModeChildren: selectChoiceConfigModePage,
      }}
      step={{
        configurePath: appStepRoutes.step,
        configureChildren: configureStepPage,
      }}
      extension={{
        configurePath: appStepRoutes.extension,
        configureChildren: configureStepPage,
      }}
    />
  );
};
