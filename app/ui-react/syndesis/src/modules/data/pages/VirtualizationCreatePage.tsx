import {
  IDvNameValidationResult,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';

import { Breadcrumb, ButtonLink, PageSection } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { AppContext, UIContext } from '../../../app';
import i18n from '../../../i18n';
import resolvers from '../../resolvers';

export const VirtualizationCreatePage: React.FunctionComponent = () => {

  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { history } = useRouteData();
  const appContext = React.useContext(AppContext);
  const { createVirtualization, validateVirtualizationName } = useVirtualizationHelpers();

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

  /**
   * Backend name validation only occurs when attempting to create
   * @param proposedName the name to validate
   */
  const doValidateName = async (
    proposedName: string
  ): Promise<true | string> => {
    // make sure name has a value
    if (proposedName === '') {
      return t(
        'shared:requiredFieldMessage'
      ) as string;
    }

    const response: IDvNameValidationResult = await validateVirtualizationName(
      proposedName
    );

    if (!response.isError) {
      return true;
    }
    return (
      t(
        'virtualization.errorValidatingVirtualizationName'
      ) +
      (response.error ? ' : ' + response.error : '')
    );
  };

  const handleCreate = async (value: any) => {
    const validation = await doValidateName(
      value.virtName
    );
    if (validation === true) {
      const virtualization = await createVirtualization(
        appContext.user.username || 'developer',
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
    } else {
      pushNotification(validation, 'error');
    }
  };

  return (
    <>
      <Breadcrumb
        actions={
          <ButtonLink
            data-testid={
              'virtualization-create-page-cancel-button'
            }
            href={resolvers.data.root()}
            className={'wizard-pf-cancel'}
          >
            Cancel
          </ButtonLink>
        }
      >
        <Link
          data-testid={
            'virtualization-create-page-home-link'
          }
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={
            'virtualization-create-page-virtualizations-link'
          }
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
          onSave={(properties, actions) => {
            handleCreate(properties).finally(() => {
              actions.setSubmitting(false);
            });
          }}
        >
          {({ fields, handleSubmit, isValid }) => (
            <form onSubmit={handleSubmit}>
              {fields}
              <button
                type="submit"
                data-testid={
                  'virtualization-create-page-create-button'
                }
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
}
