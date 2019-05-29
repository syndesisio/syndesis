import { getFlow, isIntegrationApiProvider } from '@syndesis/api';
import * as H from '@syndesis/history';
import { IntegrationOverview } from '@syndesis/models';
import {
  Breadcrumb,
  ButtonLink,
  HttpMethodColors,
  OperationsDropdown,
} from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IApiProviderOperationProps {
  description: string;
}
export const ApiProviderOperation: React.FunctionComponent<
  IApiProviderOperationProps
> = ({ description }) => {
  const [method, desc] = (description || '').split(' ');
  return (
    <>
      <HttpMethodColors method={method} />
      &nbsp;{desc}
    </>
  );
};

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
      {currentFlow && isMultiflow && (
        <OperationsDropdown
          selectedOperation={
            currentFlow.metadata && currentFlow.metadata.excerpt ? (
              <ApiProviderOperation description={currentFlow.description!} />
            ) : (
              currentFlow.name
            )
          }
        >
          {integration
            .flows!.filter(f => f.id !== currentFlow.id)
            .map(f => (
              <Link to={getFlowHref(f.id!)} key={f.id}>
                <ApiProviderOperation description={f.description!} />
                <div>
                  <strong>{f.name}</strong>
                </div>
              </Link>
            ))}
        </OperationsDropdown>
      )}
      {children}
    </Breadcrumb>
  );
};
