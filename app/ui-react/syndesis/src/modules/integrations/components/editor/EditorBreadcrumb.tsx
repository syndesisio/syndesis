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
      {desc}
    </>
  );
};

export interface IEditorBreadcrumbProps {
  currentFlowId?: string;
  integration: IntegrationOverview;
  rootHref: H.LocationDescriptor;
  // getApiProviderEditorHref: (specification: string) => H.LocationDescriptor;
}
export const EditorBreadcrumb: React.FunctionComponent<
  IEditorBreadcrumbProps
> = ({
  currentFlowId,
  integration,
  rootHref,
  // getApiProviderEditorHref
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
          <ButtonLink href={'#todo'} as={'link'}>
            View/Edit API Definition <i className="fa fa-external-link" />
          </ButtonLink>
        ) : (
          undefined
        )
      }
    >
      <Link to={rootHref}>{integration.name}</Link>
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
          Lorem ipsum dolor sit amet, consectetur adipisicing elit. A autem
          dicta dolores dolorum ducimus esse fugiat hic illum laudantium, minima
          nisi nulla omnis quidem quod, ratione sed sunt vel voluptatum!
        </OperationsDropdown>
      )}
      {children}
    </Breadcrumb>
  );
};
