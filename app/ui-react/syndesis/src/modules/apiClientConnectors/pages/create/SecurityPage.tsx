import * as H from '@syndesis/history';
import { APISummary } from '@syndesis/models/src';
import {
  ApiConnectorCreatorLayout,
  ButtonLink,
  PageSection,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorWizardSteps,
} from '../../components';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface ISecurityPageRouteState {
  specification: APISummary;
}

export const SecurityPage: React.FunctionComponent = () => {
  const { state, history } = useRouteData<null, ISecurityPageRouteState>();

  const onNext = () => {
    history.push(resolvers.create.save(state));
  };

  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {t => (
        <WithLeaveConfirmation
          i18nTitle={t('unsavedChangesTitle')}
          i18nConfirmationMessage={t('unsavedChangesMessage')}
          shouldDisplayDialog={(location: H.LocationDescriptor) => {
            const url =
              typeof location === 'string' ? location : location.pathname!;
            return !url.startsWith(routes.create.root);
          }}
        >
          {() => (
            <>
              <PageTitle
                title={t('apiClientConnectors:create:security:title')}
              />
              <ApiConnectorCreatorBreadcrumb cancelHref={resolvers.list()} />
              <ApiConnectorCreatorLayout
                header={<ApiConnectorCreatorWizardSteps step={3} />}
                content={
                  <PageSection>
                    <div>TODO</div>
                    <div>
                      <ButtonLink
                        href={resolvers.create.review({
                          specification: state.specification
                            .configuredProperties!.specification,
                        })}
                      >
                        Back
                      </ButtonLink>
                      &nbsp;
                      <ButtonLink onClick={onNext} as={'primary'}>
                        Next
                      </ButtonLink>
                    </div>
                  </PageSection>
                }
              />
            </>
          )}
        </WithLeaveConfirmation>
      )}
    </Translation>
  );
};
