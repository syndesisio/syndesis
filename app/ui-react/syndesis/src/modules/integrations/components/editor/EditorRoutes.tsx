import * as React from 'react';
import { Route, Switch } from 'react-router';
import { IEditSpecificationPageProps } from './apiProvider/EditSpecificationPage';
import { IReviewActionsPageProps } from './apiProvider/ReviewActionsPage';
import { SelectMethodPage } from './apiProvider/SelectMethodPage';
import { DescribeChoiceDataShapePage } from "./choice/DescribeChoiceDataShapePage";
import { IDataMapperPageProps } from './dataMapper/DataMapperPage';
import { ConfigureActionPage } from './endpoint/ConfigureActionPage';
import { DescribeDataShapePage } from './endpoint/DescribeDataShapePage';
import { SelectActionPage } from './endpoint/SelectActionPage';
import { ITemplateStepPageProps } from './template/TemplateStepPage';

export interface IEndpointEditorAppProps {
  selectActionPath: string;
  selectActionChildren: React.ReactElement<SelectActionPage>;
  configureActionPath: string;
  configureActionChildren: React.ReactElement<ConfigureActionPage>;
  describeDataPath: string;
  describeDataChildren: React.ReactElement<DescribeDataShapePage>;
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
  selectMethodPath: string;
  selectMethodChildren: React.ReactElement<SelectMethodPage>;
  reviewActionsPath: string;
  reviewActionsChildren: React.ReactElement<IReviewActionsPageProps>;
  editSpecificationPath: string;
  editSpecificationChildren: React.ReactElement<IEditSpecificationPageProps>;
}
export const ApiProviderApp: React.FunctionComponent<
  IApiProviderAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.selectMethodPath}
        exact={true}
        children={props.selectMethodChildren}
      />
      <Route
        path={props.reviewActionsPath}
        exact={true}
        children={props.reviewActionsChildren}
      />
      <Route
        path={props.editSpecificationPath}
        exact={true}
        children={props.editSpecificationChildren}
      />
    </Switch>
  );
};

export interface ITemplateAppProps {
  templatePath: string;
  templateChildren: React.ReactElement<ITemplateStepPageProps>;
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
  basicFilterPath: string;
  basicFilterChildren: React.ReactElement<IBasicFilterAppProps>;
}
export const BasicFilterApp: React.FunctionComponent<
  IBasicFilterAppProps
> = props => {
  return (
    <Switch>
      <Route
        path={props.basicFilterPath}
        exact={true}
        children={props.basicFilterChildren}
      />
    </Switch>
  );
};

export interface IChoiceAppProps {
  configurePath: string;
  configureChildren: React.ReactElement<IChoiceAppProps>;
  describeDataPath: string;
  describeDataChildren: React.ReactElement<DescribeChoiceDataShapePage>;
  selectModePath: string;
  selectModeChildren: React.ReactElement<IChoiceAppProps>;
}
export const ChoiceApp: React.FunctionComponent<IChoiceAppProps> = props => {
  return (
    <Switch>
      <Route
        path={props.configurePath}
        exact={true}
        children={props.configureChildren}
      />
      <Route
        path={props.describeDataPath}
        exact={true}
        children={props.describeDataChildren}
      />
      <Route
        path={props.selectModePath}
        exact={true}
        children={props.selectModeChildren}
      />
    </Switch>
  );
};

export interface IDataMapperAppProps {
  mapperPath: string;
  mapperChildren: React.ReactElement<IDataMapperPageProps>;
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
  choice: IChoiceAppProps;
  step: IStepAppProps;
  extension: IExtensionAppProps;
}
export const EditorRoutes: React.FunctionComponent<IEditorAppProps> = props => {
  return (
    <Switch>
      {props.selectStepPath && props.selectStepChildren ? (
        <Route
          path={props.selectStepPath}
          exact={true}
          children={props.selectStepChildren}
        />
      ) : null}

      <Route path={props.apiProvider.selectMethodPath}>
        <ApiProviderApp
          selectMethodPath={props.apiProvider.selectMethodPath}
          selectMethodChildren={props.apiProvider.selectMethodChildren}
          reviewActionsPath={props.apiProvider.reviewActionsPath}
          reviewActionsChildren={props.apiProvider.reviewActionsChildren}
          editSpecificationPath={props.apiProvider.editSpecificationPath}
          editSpecificationChildren={
            props.apiProvider.editSpecificationChildren
          }
        />
      </Route>

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
      <Route path={props.basicFilter.basicFilterPath}>
        <BasicFilterApp
          basicFilterPath={props.basicFilter.basicFilterPath}
          basicFilterChildren={props.basicFilter.basicFilterChildren}
        />
      </Route>
      <Route path={props.choice.selectModePath}>
        <ChoiceApp
          configurePath={props.choice.configurePath}
          configureChildren={props.choice.configureChildren}
          describeDataPath={props.choice.describeDataPath}
          describeDataChildren={props.choice.describeDataChildren}
          selectModePath={props.choice.selectModePath}
          selectModeChildren={props.choice.selectModeChildren}
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
