import { WithVirtualizationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Breadcrumb, PageSection, toTestId } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { AppContext, UIContext } from '../../../app';
import i18n from '../../../i18n';
import resolvers from '../../resolvers';

export class VirtualizationCreatePage extends React.Component {
  public render() {
    const formDefinition = {
      virtDescription: {
        componentProperty: true,
        deprecated: false,
        displayName: i18n.t(
          'data:virtualization.virtualizationDescriptionDisplay'
        ),
        javaType: 'java.lang.String',
        kind: 'property',
        order: 1,
        required: false,
        secret: false,
        type: 'textarea',
      },
      virtName: {
        componentProperty: true,
        deprecated: false,
        displayName: i18n.t('data:virtualization.virtualizationNameDisplay'),
        javaType: 'java.lang.String',
        kind: 'property',
        order: 0,
        required: true,
        secret: false,
        type: 'string',
      },
    } as IFormDefinition;
    const validate = (v: { virtName: string }) => {
      const errors: any = {};
      // TODO Incorporate service call to validate virtualization name
      if (v.virtName.includes('?')) {
        errors.virtName = 'Virtualization name contains an illegal character';
      }
      return errors;
    };

    return (
      <Translation ns={['data', 'shared']}>
        {t => (
          <UIContext.Consumer>
            {({ pushNotification }) => {
              return (
                <WithRouteData<null, null>>
                  {(p, s, { history }) => (
                    <AppContext.Consumer>
                      {({ user }) => (
                        <WithVirtualizationHelpers>
                          {({ createVirtualization }) => {
                            const handleCreate = async (value: any) => {
                              const virtualization = await createVirtualization(
                                user.username || 'developer',
                                value.virtName,
                                value.virtDescription
                              );
                              pushNotification(
                                t(
                                  'virtualization.createVirtualizationSuccess',
                                  {
                                    name: value.virtName,
                                  }
                                ),
                                'success'
                              );
                              history.push(
                                resolvers.data.virtualizations.views.root({
                                  virtualization,
                                })
                              );
                            };
                            return (
                              <>
                                <Breadcrumb>
                                  <Link
                                    data-testid={toTestId(
                                      'VirtualizationCreatePage',
                                      'home-link'
                                    )}
                                    to={resolvers.dashboard.root()}
                                  >
                                    {t('shared:Home')}
                                  </Link>
                                  <Link
                                    data-testid={toTestId(
                                      'VirtualizationCreatePage',
                                      'virtualizations-link'
                                    )}
                                    to={resolvers.data.root()}
                                  >
                                    {t('shared:DataVirtualizations')}
                                  </Link>
                                  <span>
                                    {t(
                                      'data:virtualization.createDataVirtualizationTitle'
                                    )}
                                  </span>
                                </Breadcrumb>
                                <PageSection variant={'light'}>
                                  <h1 className="pf-c-title pf-m-xl">
                                    {t(
                                      'data:virtualization.createDataVirtualizationTitle'
                                    )}
                                  </h1>
                                </PageSection>
                                <PageSection>
                                  <AutoForm
                                    definition={formDefinition}
                                    initialValue={''}
                                    i18nRequiredProperty={t(
                                      'data:virtualization.requiredPropertyText'
                                    )}
                                    validate={validate}
                                    onSave={handleCreate}
                                  >
                                    {({ fields, handleSubmit }) => (
                                      <form onSubmit={handleSubmit}>
                                        {fields}
                                        <button
                                          type="submit"
                                          data-testid={toTestId(
                                            'VirtualizationCreatePage',
                                            'create-button'
                                          )}
                                          className="btn btn-primary"
                                        >
                                          {t('shared:Create')}
                                        </button>
                                      </form>
                                    )}
                                  </AutoForm>
                                </PageSection>
                              </>
                            );
                          }}
                        </WithVirtualizationHelpers>
                      )}
                    </AppContext.Consumer>
                  )}
                </WithRouteData>
              );
            }}
          </UIContext.Consumer>
        )}
      </Translation>
    );
  }
}
