/* tslint:disable:object-literal-sort-keys */
import * as H from '@syndesis/history';
import { Integration } from '@syndesis/models';
import * as React from 'react';
import { EditSpecificationPage } from './apiProvider/EditSpecificationPage';
import { ReviewActionsPage } from './apiProvider/ReviewActionsPage';
import { ReviewOperationsPage } from './apiProvider/ReviewOperationsPage';
import { SelectMethodPage } from './apiProvider/SelectMethodPage';
import { SetInfoPage } from './apiProvider/SetInfoPage';
import { DataMapperPage } from './dataMapper/DataMapperPage';
import { EditorRoutes } from './EditorRoutes';
import { EditorSidebar } from './EditorSidebar';
import { ConfigureActionPage } from './endpoint/ConfigureActionPage';
import { DescribeDataShapePage } from './endpoint/DescribeDataShapePage';
import { SelectActionPage } from './endpoint/SelectActionPage';
import {
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  IDescribeDataShapeRouteParams,
  IDescribeDataShapeRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
  stepRoutes,
} from './interfaces';
import { makeEditorResolvers } from './makeEditorResolvers';
import { RuleFilterStepPage } from './ruleFilter/RuleFilterStepPage';
import { SelectConnectionPage } from './SelectConnectionPage';
import { ConfigureStepPage } from './step/ConfigureStepPage';
import { TemplateStepPage } from './template/TemplateStepPage';

export interface IEditorApp {
  mode: 'adding' | 'editing';
  appStepRoutes: typeof stepRoutes;
  appResolvers: ReturnType<typeof makeEditorResolvers>;
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  postConfigureHref: (
    integration: Integration,
    p: ITemplateStepRouteParams | IConfigureActionRouteParams,
    s: ITemplateStepRouteState | IConfigureActionRouteState
  ) => H.LocationDescriptorObject;
}

export const EditorApp: React.FunctionComponent<IEditorApp> = ({
  mode,
  appStepRoutes,
  appResolvers,
  cancelHref,
  postConfigureHref,
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
    />
  );

  const configureActionPage = (
    <ConfigureActionPage
      backHref={(p, s) => appResolvers.connection.selectAction({ ...p, ...s })}
      cancelHref={cancelHref}
      mode={mode}
      nextStepHref={(p, s) =>
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
    />
  );

  const reviewActionsPage = (
    <ReviewActionsPage
      cancelHref={(params, state) =>
        appResolvers.apiProvider.selectMethod({ ...params, ...state })
      }
    />
  );

  return (
    <>
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
          editSpecificationChildren: <EditSpecificationPage />,
          setInfoPath: appStepRoutes.apiProvider.setInfo,
          setInfoChildren: <SetInfoPage />,
          reviewOperationsPath: appStepRoutes.apiProvider.reviewOperations,
          reviewOperationsChildren: <ReviewOperationsPage />,
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
        step={{
          configurePath: appStepRoutes.step,
          configureChildren: configureStepPage,
        }}
        extension={{
          configurePath: appStepRoutes.extension,
          configureChildren: configureStepPage,
        }}
      />
    </>
  );
};
