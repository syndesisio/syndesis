import {
  IDvNameValidationResult,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';

import { Breadcrumb, IVirtualizationCreateValidationResult, PageSection, VirtualizationCreateForm } from '@syndesis/ui';
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
  const [validationResults, setValidationResults] = React.useState<
  IVirtualizationCreateValidationResult[]
  >([]);

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

  const doCancel = async () => {
    history.push(resolvers.data.virtualizations.list());
  };

  /**
   * Backend name validation only occurs when attempting to create
   * @param proposedName the name to validate
   */
  const doValidateName = async (
    proposedName: string
  ): Promise<IVirtualizationCreateValidationResult> => {
    // make sure name has a value
    if (proposedName === '') {
      return {
        message: t(
        'shared:requiredFieldMessage'
      ) as string,
      type: 'danger'
      };
    }
  
    const response: IDvNameValidationResult = await validateVirtualizationName(
      proposedName
    );

    if (!response.isError) {
      return {
        message: '',
        type: 'success'
      }
    }
    return {
      message: response.error ? response.error : '',
      type: 'danger'
    }
  };

  const handleCreate = async (value: any) => {
    const validation = await doValidateName(
      value.virtName
    );
    if (validation.type === 'success') {
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
      setValidationResults([validation]);
    }
  };

  return (
    <>
      <Breadcrumb>
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
          {({ fields, handleSubmit, isSubmitting, isValidating }) => (
            <VirtualizationCreateForm
              handleSubmit={handleSubmit}
              i18nCancelLabel={t('shared:Cancel')}
              i18nCreateLabel={t('shared:Create')}
              isWorking={isSubmitting || isValidating}
              validationResults={validationResults}
              onCancel={doCancel}
            >
              {fields}
            </VirtualizationCreateForm>
          )}
        </AutoForm>
      </PageSection>
    </>
  );
}
