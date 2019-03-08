import { WithVirtualizationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Breadcrumb, Container } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import i18n from '../../../i18n';
import resolvers from '../../resolvers';

export default class VirtualizationCreatePage extends React.Component {
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
      <WithRouteData<null, null>>
        {(p, s, { history }) => (
          // TODO need to retrieve real user here
          <WithVirtualizationHelpers username="developer">
            {({ createVirtualization }) => {
              const handleCreate = async (value: any) => {
                await createVirtualization(
                  value.virtName,
                  value.virtDescription
                );
                // TODO: post toast notification
                history.push(resolvers.data.virtualizations.list());
              };
              return (
                <Translation ns={['data', 'shared']}>
                  {t => (
                    <>
                      <Breadcrumb>
                        <Link to={resolvers.dashboard.root()}>
                          {t('shared:Home')}
                        </Link>
                        <Link to={resolvers.data.root()}>
                          {t('shared:DataVirtualizations')}
                        </Link>
                        <span>
                          {t(
                            'data:virtualization.createDataVirtualizationTitle'
                          )}
                        </span>
                      </Breadcrumb>
                      <Container>
                        <h1>
                          {t(
                            'data:virtualization.createDataVirtualizationTitle'
                          )}
                        </h1>
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
                            <React.Fragment>
                              {fields}
                              <button
                                type="button"
                                className="btn btn-primary"
                                onClick={handleSubmit}
                              >
                                {t('shared:Create')}
                              </button>
                            </React.Fragment>
                          )}
                        </AutoForm>
                      </Container>
                    </>
                  )}
                </Translation>
              );
            }}
          </WithVirtualizationHelpers>
        )}
      </WithRouteData>
    );
  }
}
