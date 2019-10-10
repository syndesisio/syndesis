import {
  IDvNameValidationResult,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition, IFormValue } from '@syndesis/auto-form';
import { validateRequiredProperties } from '@syndesis/utils';

import {
  Breadcrumb,
  IVirtualizationCreateValidationResult,
  PageSection,
  VirtualizationCreateForm,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { AppContext, UIContext } from '../../../app';
import resolvers from '../../resolvers';

export const VirtualizationCreatePage: React.FunctionComponent = () => {
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { history } = useRouteData();
  const appContext = React.useContext(AppContext);
  const {
    createVirtualization,
    validateVirtualizationName,
  } = useVirtualizationHelpers();
  const [validationResults, setValidationResults] = React.useState<
    IVirtualizationCreateValidationResult[]
  >([]);

  const formDefinition = {
    virtDescription: {
      componentProperty: true,
      deprecated: false,
      displayName: t('virtualizationDescriptionDisplay'),
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
      displayName: t('virtualizationNameDisplay'),
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
        message: t('shared:requiredFieldMessage') as string,
        type: 'danger',
      };
    }

    const response: IDvNameValidationResult = await validateVirtualizationName(
      proposedName
    );

    if (response.nameExists) {
      return {
        message: t('errorVirtualizationNameExists', {
          name: proposedName,
        }),
        type: 'danger',
      };
    }
    if (response.hasError) {
      return {
        message: response.message
          ? response.message
          : t('errorVirtualizationNameValidation'),
        type: 'danger',
      };
    }
    return {
      message: '',
      type: 'success',
    };
  };

  const handleCreate = async (value: any) => {
    const validation = await doValidateName(value.virtName);
    if (validation.type === 'success') {
      const virtualization = await createVirtualization(
        appContext.user.username || 'developer',
        value.virtName,
        value.virtDescription
      );
      pushNotification(
        t('createVirtualizationSuccess', {
          name: value.virtName,
        }),
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

  const validator = (values: IFormValue) =>
    validateRequiredProperties(
      formDefinition,
      (name: string) => `${name} is required`,
      values
    );

  return (
    <>
      <Breadcrumb>
        <Link
          data-testid={'virtualization-create-page-home-link'}
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={'virtualization-create-page-virtualizations-link'}
          to={resolvers.data.root()}
        >
          {t('shared:DataVirtualizations')}
        </Link>
        <span>{t('createDataVirtualizationTitle')}</span>
      </Breadcrumb>
      <PageSection variant={'light'}>
        <h1 className="pf-c-title pf-m-xl">
          {t('createDataVirtualizationTitle')}
        </h1>
      </PageSection>
      <PageSection>
        <AutoForm
          definition={formDefinition}
          i18nRequiredProperty={t('shared:requiredFieldMessage')}
          initialValue={{
            virtDescription: '',
            virtName: '',
          }}
          validate={validator}
          validateInitial={validator}
          onSave={(properties, actions) => {
            handleCreate(properties).finally(() => {
              actions.setSubmitting(false);
            });
          }}
        >
          {({ fields, handleSubmit, isSubmitting, isValid, isValidating }) => (
            <VirtualizationCreateForm
              handleSubmit={handleSubmit}
              i18nCancelLabel={t('shared:Cancel')}
              i18nCreateLabel={t('shared:Create')}
              isDisableCreate={!isValid}
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
};
