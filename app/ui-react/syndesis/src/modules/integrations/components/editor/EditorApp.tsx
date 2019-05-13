import * as React from 'react';
import { Route, Switch } from 'react-router';
import { ReviewPage } from './api-provider/EditPage';
import { EditPage } from './api-provider/ReviewPage';
import { UploadPage } from './api-provider/UploadPage';
import { ConfigureActionPage } from './endpoint/ConfigureActionPage';
import { SelectActionPage } from './endpoint/SelectActionPage';
import { TemplateStepPage } from './template/TemplateStepPage';

export interface IEndpointEditorAppProps {
  selectActionPath: string;
  selectActionChildren: React.ReactElement<SelectActionPage>;
  configureActionPath: string;
  configureActionChildren: React.ReactElement<ConfigureActionPage>;
  describeDataPath: string;
  describeDataChildren: React.ReactNode;
}
export const EndpointEditorApp: React.FunctionComponent<
  IEndpointEditorAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.selectActionPath}
        exact={true}
        children={props.selectActionChildren}
      />
      <Route
        path={props.configureActionPath}
        exact={true}
        children={props.configureActionChildren}
      />
      <Route
        path={props.describeDataPath}
        exact={true}
        children={props.describeDataChildren}
      />
    </Switch>
  );
};

export interface IApiProviderAppProps {
  uploadPath: string;
  uploadChildren: React.ReactElement<UploadPage>;
  reviewPath: string;
  reviewChildren: React.ReactElement<ReviewPage>;
  editPath: string;
  editChildren: React.ReactElement<EditPage>;
}
export const ApiProviderApp: React.FunctionComponent<
  IApiProviderAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.uploadPath}
        exact={true}
        children={props.uploadChildren}
      />
      <Route
        path={props.reviewPath}
        exact={true}
        children={props.reviewChildren}
      />
      <Route path={props.editPath} exact={true} children={props.editChildren} />
    </Switch>
  );
};

export interface ITemplateAppProps {
  templatePath: string;
  templateChildren: React.ReactElement<TemplateStepPage>;
}
export const TemplateApp: React.FunctionComponent<
  ITemplateAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.templatePath}
        exact={true}
        children={props.templateChildren}
      />
    </Switch>
  );
};

export interface IBasicFilterAppProps {
  filterPath: string;
  filterChildren: React.ReactNode;
}
export const BasicFilterApp: React.FunctionComponent<
  IBasicFilterAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.filterPath}
        exact={true}
        children={props.filterChildren}
      />
    </Switch>
  );
};

export interface IDataMapperAppProps {
  mapperPath: string;
  mapperChildren: React.ReactNode;
}
export const DataMapperApp: React.FunctionComponent<
  IDataMapperAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.mapperPath}
        exact={true}
        children={props.mapperChildren}
      />
    </Switch>
  );
};

export interface IStepAppProps {
  configurePath: string;
  configureChildren: React.ReactNode;
}
export const StepApp: React.FunctionComponent<IStepAppProps> = props => {
  return (
    <Switch>
      <Route
        path={props.configurePath}
        exact={true}
        children={props.configureChildren}
      />
    </Switch>
  );
};

export interface IExtensionAppProps {
  configurePath: string;
  configureChildren: React.ReactNode;
}
export const ExtensionApp: React.FunctionComponent<
  IExtensionAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.configurePath}
        exact={true}
        children={props.configureChildren}
      />
    </Switch>
  );
};

export interface IEditorAppProps {
  selectStepPath?: string;
  selectStepChildren?: React.ReactNode;
  endpointEditor: IEndpointEditorAppProps;
  apiProvider: IApiProviderAppProps;
  template: ITemplateAppProps;
  dataMapper: IDataMapperAppProps;
  basicFilter: IBasicFilterAppProps;
  step: IStepAppProps;
  extension: IExtensionAppProps;
}
export const EditorApp: React.FunctionComponent<IEditorAppProps> = props => {
  return (
    <Switch>
      {props.selectStepPath && props.selectStepChildren ? (
        <Route
          path={props.selectStepPath}
          exact={true}
          children={props.selectStepChildren}
        />
      ) : null}

      <Route path={props.endpointEditor.selectActionPath}>
        <EndpointEditorApp
          selectActionPath={props.endpointEditor.selectActionPath}
          selectActionChildren={props.endpointEditor.selectActionChildren}
          configureActionPath={props.endpointEditor.configureActionPath}
          configureActionChildren={props.endpointEditor.configureActionChildren}
          describeDataPath={props.endpointEditor.describeDataPath}
          describeDataChildren={props.endpointEditor.describeDataChildren}
        />
      </Route>
      <Route path={props.apiProvider.uploadPath}>
        <ApiProviderApp
          uploadPath={props.apiProvider.uploadPath}
          uploadChildren={props.apiProvider.uploadChildren}
          reviewPath={props.apiProvider.reviewPath}
          reviewChildren={props.apiProvider.reviewChildren}
          editPath={props.apiProvider.editPath}
          editChildren={props.apiProvider.editChildren}
        />
      </Route>
      <Route path={props.template.templatePath}>
        <TemplateApp
          templatePath={props.template.templatePath}
          templateChildren={props.template.templateChildren}
        />
      </Route>
      <Route path={props.dataMapper.mapperPath}>
        <DataMapperApp
          mapperPath={props.dataMapper.mapperPath}
          mapperChildren={props.dataMapper.mapperChildren}
        />
      </Route>
      <Route path={props.basicFilter.filterPath}>
        <BasicFilterApp
          filterPath={props.basicFilter.filterPath}
          filterChildren={props.basicFilter.filterChildren}
        />
      </Route>
      <Route path={props.step.configurePath}>
        <StepApp
          configurePath={props.step.configurePath}
          configureChildren={props.step.configureChildren}
        />
      </Route>
      <Route path={props.extension.configurePath}>
        <ExtensionApp
          configurePath={props.extension.configurePath}
          configureChildren={props.extension.configureChildren}
        />
      </Route>
    </Switch>
  );
};
