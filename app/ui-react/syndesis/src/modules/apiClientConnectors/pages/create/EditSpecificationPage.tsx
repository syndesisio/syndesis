import { ApicurioAdapter } from '@syndesis/apicurio-adapter';
import * as H from '@syndesis/history';
import { Breadcrumb, ButtonLink, PageSection } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface IEditSpecificationRouteState {
  specification: string;
}

export const EditSpecificationPage: React.FunctionComponent = () => {
  const { state } = useRouteData<null, IEditSpecificationRouteState>();

  const [updatedSpecification, setUpdatedSpecification] = React.useState();

  const onSpecification = (newSpec: any) => {
    setUpdatedSpecification(JSON.stringify(newSpec.spec));
  };

  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {t => (
        <WithLeaveConfirmation
          i18nTitle={t('apiClientConnectors:create:unsavedChangesTitle')}
          i18nConfirmationMessage={t(
            'apiClientConnectors:create:unsavedChangesMessage'
          )}
          shouldDisplayDialog={(location: H.LocationDescriptor) => {
            const url =
              typeof location === 'string' ? location : location.pathname!;
            return !url.startsWith(routes.create.root);
          }}
        >
          {() => (
            <>
              <PageTitle
                title={t('apiClientConnectors:create:specification:title')}
              />
              <Breadcrumb
                actions={
                  <>
                    <ButtonLink
                      data-testid={
                        'api-connector-creator-specification-cancel-button'
                      }
                      href={resolvers.create.review(state)}
                      className={'wizard-pf-cancel'}
                    >
                      {t('shared:Cancel')}
                    </ButtonLink>
                    &nbsp;
                    <ButtonLink
                      data-testid={
                        'api-connector-creator-specification-save-button'
                      }
                      href={resolvers.create.review({
                        specification: updatedSpecification,
                      })}
                      as={'primary'}
                      disabled={updatedSpecification === undefined}
                    >
                      {t('shared:Save')}
                    </ButtonLink>
                  </>
                }
              >
                <Link
                  data-testid={'api-connector-creator-specification-back-link'}
                  to={resolvers.create.review(state)}
                >
                  &lt; {t('shared:Back')}
                </Link>
                <span>
                  {t('apiClientConnectors:create:specification:title')}
                </span>
              </Breadcrumb>
              <PageSection
                variant={'light'}
                noPadding={true}
                style={{ overflow: 'hidden' }}
              >
                <ApicurioAdapter
                  specification={updatedSpecification || state.specification!}
                  onSpecification={onSpecification}
                />
              </PageSection>
            </>
          )}
        </WithLeaveConfirmation>
      )}
    </Translation>
  );
};
