import {
  EXCERPT_METADATA_KEY,
  getApiProviderFlows,
  getConditionalFlowGroupsFor,
  getFlow,
  getMetadataValue,
  isConditionalFlow,
  isDefaultFlow,
  isIntegrationApiProvider,
  isPrimaryFlow,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Flow, IntegrationOverview } from '@syndesis/models';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbItemProps,
  ButtonLink,
  ConditionsDropdown,
  ConditionsDropdownBody,
  ConditionsDropdownHeader,
  ConditionsDropdownItem,
  EditorToolbarDropdownBackButtonItem,
  HttpMethodColors,
  OperationsDropdown,
} from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import resolvers from '../../resolvers';

export interface IBaseBreadcrumbItemsProps extends IEditorBreadcrumbProps {
  currentFlow?: Flow;
  isApiProvider: boolean;
  operationsDropdown: React.ReactNode;
  flowsDropdown: React.ReactNode;
}
export const BaseBreadcrumbItems: React.FunctionComponent<
  IBaseBreadcrumbItemsProps
> = ({
  rootHref,
  integration,
  currentFlow,
  isApiProvider,
  apiProviderEditorHref,
  operationsDropdown,
  flowsDropdown,
  children,
}) => {
  const integrationName = integration.name || 'New integration';
  const canNavigateToRoot =
    isApiProvider && currentFlow && (currentFlow.steps || []).length > 1;
  const items: Array<React.ReactElement<BreadcrumbItemProps>> = [
    <BreadcrumbItem key={0}>
      <Link to={resolvers.list()}>Integrations</Link>
    </BreadcrumbItem>,
    <BreadcrumbItem key={1}>
      {canNavigateToRoot ? (
        <Link
          to={rootHref}
          title={integration.name}
          style={{
            maxWidth: 200,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {integrationName}
        </Link>
      ) : (
        integrationName
      )}
    </BreadcrumbItem>,
    operationsDropdown,
    flowsDropdown,
    <BreadcrumbItem key={'children'} isActive={true}>
      {children}
    </BreadcrumbItem>,
  ].filter(c => c) as Array<React.ReactElement<BreadcrumbItemProps>>;
  return (
    <Breadcrumb
      actions={
        isApiProvider ? (
          <ButtonLink href={apiProviderEditorHref} as={'link'}>
            View/Edit API Definition <i className="fa fa-external-link" />
          </ButtonLink>
        ) : (
          undefined
        )
      }
      items={items}
    />
  );
};

export interface IApiProviderOperationItemProps {
  description: string;
  isCurrent?: boolean;
}
export const ApiProviderOperationItem: React.FunctionComponent<
  IApiProviderOperationItemProps
> = ({ description, isCurrent }) => {
  const [method, desc] = (description || '').split(' ');
  return (
    <>
      <HttpMethodColors method={method} />
      &nbsp;{desc}
      {isCurrent && <i className="fa fa-check pull-right" />}
    </>
  );
};

export interface IApiProviderDropdownProps extends IEditorBreadcrumbProps {
  currentFlow: Flow;
  isApiProvider: boolean;
}
const ApiProviderDropdown: React.FunctionComponent<
  IApiProviderDropdownProps
> = ({ currentFlow, integration, getFlowHref, isApiProvider, rootHref }) => {
  const { t } = useTranslation(['integrations', 'shared']);
  const isPrimary = isPrimaryFlow(currentFlow);
  const primaryFlow = isPrimary
    ? currentFlow
    : getFlow(
        integration,
        getMetadataValue<string>('primaryFlowId', currentFlow!.metadata)!
      );
  const isMultiflow =
    integration.flows && integration.flows.filter(f => f.name).length > 0;

  return isApiProvider && primaryFlow && isMultiflow ? (
    <BreadcrumbItem>
      <span>Operation&nbsp;&nbsp;</span>
      <OperationsDropdown
        selectedOperation={
          getMetadataValue<string>(
            EXCERPT_METADATA_KEY,
            primaryFlow.metadata
          ) ? (
            <ApiProviderOperationItem description={primaryFlow.description!} />
          ) : (
            primaryFlow.name
          )
        }
      >
        <EditorToolbarDropdownBackButtonItem
          title={t('integrations:editor:BackToOperationList')}
          href={rootHref}
        />
        {getApiProviderFlows(integration).map(f => (
          <Link to={getFlowHref(f.id!)} key={f.id}>
            <ApiProviderOperationItem
              description={f.description!}
              isCurrent={f.id === currentFlow.id}
            />
            <div>
              <strong>{f.name}</strong>
            </div>
          </Link>
        ))}
      </OperationsDropdown>
    </BreadcrumbItem>
  ) : null;
};

export interface IFlowsDropdownProps extends IEditorBreadcrumbProps {
  currentFlow: Flow;
  isApiProvider: boolean;
}
const FlowsDropdown: React.FunctionComponent<IFlowsDropdownProps> = ({
  currentFlow,
  integration,
  isApiProvider,
  getFlowHref,
}) => {
  const { t } = useTranslation(['integrations', 'shared']);
  const isPrimary = isPrimaryFlow(currentFlow);
  const primaryFlow = isPrimary
    ? currentFlow
    : getFlow(
        integration,
        getMetadataValue<string>('primaryFlowId', currentFlow!.metadata)!
      );
  const flowGroups = getConditionalFlowGroupsFor(integration, primaryFlow!.id!);
  return !isPrimary && flowGroups.length > 0 ? (
    <BreadcrumbItem>
      <span>Flow&nbsp;&nbsp;</span>
      <ConditionsDropdown
        selectedFlow={
          <ConditionsDropdownBody
            description={currentFlow.description!}
            condition={isDefaultFlow(currentFlow) ? 'OTHERWISE' : 'WHEN'}
          />
        }
      >
        <>
          {!isPrimary && (
            <EditorToolbarDropdownBackButtonItem
              title={
                isApiProvider
                  ? t('integrations:editor:BackToOperationFlow')
                  : t('integrations:editor:BackToPrimaryFlow')
              }
              href={getFlowHref(primaryFlow!.id!)}
            />
          )}
          {flowGroups.map((group, groupIndex) => (
            <ConditionsDropdownHeader
              key={groupIndex}
              title={t('integrations:editor:ConditionalFlowStepGroup', {
                group: groupIndex + 1,
              })}
            >
              {group.flows.map(f => (
                <ConditionsDropdownItem
                  key={`${group.id} ${f.id}`}
                  name={getFlowName(f)}
                  description={f.description!}
                  isCurrent={f.id === currentFlow.id}
                  condition={
                    isDefaultFlow(f)
                      ? t('integrations:editor:OTHERWISE')
                      : t('integrations:editor:WHEN')
                  }
                  link={getFlowHref(f.id!)}
                />
              ))}
            </ConditionsDropdownHeader>
          ))}
        </>
      </ConditionsDropdown>
    </BreadcrumbItem>
  ) : null;
};

function getFlowName(flow: Flow) {
  if (typeof flow.name !== 'undefined' && flow.name !== '') {
    return flow.name;
  }
  if (isConditionalFlow(flow)) {
    return 'Conditional';
  }
  if (isDefaultFlow(flow)) {
    return 'Default';
  }
  if (isPrimaryFlow(flow)) {
    return 'Primary';
  }
  return 'Flow';
}

export interface IEditorBreadcrumbProps {
  currentFlowId?: string;
  integration: IntegrationOverview;
  rootHref: H.LocationDescriptor;
  apiProviderEditorHref: H.LocationDescriptor;
  getFlowHref: (flowId: string) => H.LocationDescriptor;
}
export const EditorBreadcrumb: React.FunctionComponent<
  IEditorBreadcrumbProps
> = props => {
  const { integration, currentFlowId } = props;
  const isApiProvider = isIntegrationApiProvider(integration);
  const currentFlow = currentFlowId
    ? getFlow(integration, currentFlowId)
    : undefined;

  return (
    <BaseBreadcrumbItems
      currentFlow={currentFlow}
      isApiProvider={isApiProvider}
      operationsDropdown={
        currentFlow && (
          <ApiProviderDropdown
            key={'operations'}
            currentFlow={currentFlow}
            isApiProvider={isApiProvider}
            {...props}
          />
        )
      }
      flowsDropdown={
        currentFlow && (
          <FlowsDropdown
            key={'flows'}
            currentFlow={currentFlow}
            isApiProvider={isApiProvider}
            {...props}
          />
        )
      }
      {...props}
    />
  );
};
