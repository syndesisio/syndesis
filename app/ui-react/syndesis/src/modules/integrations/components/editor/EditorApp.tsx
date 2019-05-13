/* tslint:disable:object-literal-sort-keys */
import * as H from '@syndesis/history';
import { Integration } from '@syndesis/models';
import * as React from 'react';
import resolvers, { RouteResolver } from '../../resolvers';
import { ReviewPage } from './api-provider/EditPage';
import { EditPage } from './api-provider/ReviewPage';
import { UploadPage } from './api-provider/UploadPage';
import { EditorRoutes } from './EditorRoutes';
import { EditorSidebar } from './EditorSidebar';
import { ConfigureActionPage } from './endpoint/ConfigureActionPage';
import { SelectActionPage } from './endpoint/SelectActionPage';
import {
  IConfigureActionRouteParams,
  IConfigureActionRouteState,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  ITemplateStepRouteParams,
  ITemplateStepRouteState,
  stepRoutes,
} from './interfaces';
import { SelectConnectionPage } from './SelectConnectionPage';
import { ConfigureStepPage } from './step/ConfigureStepPage';
import { TemplateStepPage } from './template/TemplateStepPage';

const TODO: React.FunctionComponent = () => <>TODO</>;

export interface IEditorApp {
  mode: 'adding' | 'editing';
  appStepRoutes: typeof stepRoutes;
  appResolvers: RouteResolver<typeof stepRoutes>;
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
      apiProviderHref={appResolvers.apiProvider.upload}
      connectionHref={(connection, params, state) =>
        appResolvers.connection.selectAction({
          connection,
          ...params,
          ...state,
        })
      }
      filterHref={appResolvers.basicFilter}
      mapperHref={appResolvers.dataMapper}
      templateHref={appResolvers.template}
      stepHref={(step, params, state) =>
        resolvers.create.finish.step({
          step,
          ...params,
          ...state,
        })
      }
      sidebar={props => <EditorSidebar {...props} />}
    />
  );

  const selectActionPage = (
    <SelectActionPage
      cancelHref={appResolvers.selectStep}
      sidebar={props => <EditorSidebar {...props} />}
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
      cancelHref={resolvers.list}
      mode={mode}
      nextStepHref={(p, s) =>
        appResolvers.connection.configureAction({
          ...p,
          ...s,
        })
      }
      sidebar={props => <EditorSidebar {...props} />}
      postConfigureHref={postConfigureHref}
    />
  );

  const templateStepPage = (
    <TemplateStepPage
      mode={mode}
      cancelHref={cancelHref}
      sidebar={props => <EditorSidebar {...props} />}
      postConfigureHref={postConfigureHref}
    />
  );

  const configureStepPage = (
    <ConfigureStepPage
      cancelHref={cancelHref}
      mode={mode}
      sidebar={props => <EditorSidebar {...props} />}
      postConfigureHref={postConfigureHref}
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
          describeDataChildren: TODO,
        }}
        apiProvider={{
          uploadPath: appStepRoutes.apiProvider.upload,
          uploadChildren: <UploadPage />,
          reviewPath: appStepRoutes.apiProvider.review,
          reviewChildren: <ReviewPage />,
          editPath: appStepRoutes.apiProvider.edit,
          editChildren: <EditPage />,
        }}
        template={{
          templatePath: appStepRoutes.template,
          templateChildren: templateStepPage,
        }}
        dataMapper={{
          mapperPath: appStepRoutes.dataMapper,
          mapperChildren: TODO,
        }}
        basicFilter={{
          filterPath: appStepRoutes.basicFilter,
          filterChildren: TODO,
        }}
        step={{
          configurePath: appStepRoutes.step,
          configureChildren: configureStepPage,
        }}
        extension={{
          configurePath: appStepRoutes.extension,
          configureChildren: TODO,
        }}
      />
    </>
  );
};
