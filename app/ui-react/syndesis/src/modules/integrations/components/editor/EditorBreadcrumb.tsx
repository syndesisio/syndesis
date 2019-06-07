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
  ButtonLink,
  ConditionsBackButtonItem,
  ConditionsDropdown,
  ConditionsDropdownBody,
  ConditionsDropdownHeader,
  ConditionsDropdownItem,
  HttpMethodColors,
  OperationsDropdown,
} from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IApiProviderOperationItemProps {
  description: string;
}
export const ApiProviderOperationItem: React.FunctionComponent<
  IApiProviderOperationItemProps
> = ({ description }) => {
  const [method, desc] = (description || '').split(' ');
  return (
    <>
      <HttpMethodColors method={method} />
      &nbsp;{desc}
    </>
  );
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
> = ({
  currentFlowId,
  integration,
  rootHref,
  apiProviderEditorHref,
  getFlowHref,
  children,
}) => {
  const isApiProvider = isIntegrationApiProvider(integration);
  const isMultiflow =
    integration.flows && integration.flows.filter(f => f.name).length > 0;
  const currentFlow = currentFlowId
    ? getFlow(integration, currentFlowId)
    : undefined;
  if (!currentFlow) {
    return <></>;
  }
  const isPrimary = isPrimaryFlow(currentFlow!);
  const primaryFlow = isPrimary
    ? currentFlow
    : getFlow(
        integration,
        getMetadataValue<string>('primaryFlowId', currentFlow!.metadata)!
      );
  const flowGroups = getConditionalFlowGroupsFor(integration, primaryFlow!.id!);
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
    >
      <Link
        to={rootHref}
        title={integration.name}
        style={{
          maxWidth: 200,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
        }}
        onClick={(ev: React.MouseEvent) => {
          if (!currentFlow || (currentFlow.steps || []).length < 2) {
            ev.stopPropagation();
            ev.preventDefault();
          }
        }}
      >
        {integration.name || 'New integration'}
      </Link>
      {primaryFlow && isApiProvider && isMultiflow && (
        <>
          <span>Operation&nbsp;&nbsp;</span>
          <OperationsDropdown
            selectedOperation={
              getMetadataValue<string>(
                EXCERPT_METADATA_KEY,
                primaryFlow.metadata
              ) ? (
                <ApiProviderOperationItem
                  description={primaryFlow.description!}
                />
              ) : (
                primaryFlow.name
              )
            }
          >
            {getApiProviderFlows(integration).map(f => (
              <Link to={getFlowHref(f.id!)} key={f.id}>
                <ApiProviderOperationItem description={f.description!} />
                <div>
                  <strong>{f.name}</strong>
                </div>
              </Link>
            ))}
          </OperationsDropdown>
        </>
      )}
      {!isPrimary && flowGroups.length > 0 && (
        <>
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
                <ConditionsBackButtonItem
                  title={
                    isApiProvider
                      ? 'Back to Operation Flow'
                      : 'Back to Primary Flow'
                  }
                  href={getFlowHref(primaryFlow!.id!)}
                />
              )}
              {flowGroups.map((group, groupIndex) => (
                <ConditionsDropdownHeader
                  key={groupIndex}
                  title={`${groupIndex + 1} Conditional Flow Step`}
                >
                  {group.flows.map(f => (
                    <ConditionsDropdownItem
                      key={`${group.id} ${f.id}`}
                      name={getFlowName(f)}
                      description={f.description!}
                      condition={isDefaultFlow(f) ? 'OTHERWISE' : 'WHEN'}
                      link={getFlowHref(f.id!)}
                    />
                  ))}
                </ConditionsDropdownHeader>
              ))}
            </>
          </ConditionsDropdown>
        </>
      )}
      {children}
    </Breadcrumb>
  );
};
