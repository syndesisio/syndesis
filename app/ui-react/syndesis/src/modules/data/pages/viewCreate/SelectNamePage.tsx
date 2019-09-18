import {
  IDvNameValidationResult,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { RestDataService, SchemaNodeInfo } from '@syndesis/models';
import { ViewConfigurationForm, ViewCreateLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import i18n from '../../../../i18n';
import { PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
import { ViewCreateSteps } from '../../shared';
import { generateViewDefinition } from '../../shared/VirtualizationUtils';

export interface ISaveForm {
  name: string;
  description?: string;
}

/**
 * @param virtualizationId - the ID of the virtualization for the wizard.
 */
export interface ISelectNameRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 * @param schemaNodeInfo - the selected schema node
 */
export interface ISelectNameRouteState {
  virtualization: RestDataService;
  schemaNodeInfo: SchemaNodeInfo[];
}

export const SelectNamePage: React.FunctionComponent = () => {
  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<
    ISelectNameRouteParams,
    ISelectNameRouteState
  >();
  const { pushNotification } = useContext(UIContext);
  const {
    saveViewDefinition,
    validateViewName,
  } = useVirtualizationHelpers();

  const validateDescription = (desc: string): string => {
    if (desc.includes("'")) {
      return i18n.t('data:virtualization.viewDescriptionValidationError');
    }
    return '';
  };

  /**
   * Backend name validation only occurs when attempting to create
   * @param proposedName the name to validate
   */
  const doValidateName = async (proposedName: string): Promise<string> => {
    // make sure name has a value
    if (proposedName === '') {
      return t('shared:requiredFieldMessage') as string;
    }

    const response: IDvNameValidationResult = await validateViewName(
      state.virtualization.keng__id,
      proposedName
    );

    if (!response.isError) {
      return '';
    }
    return (
      t('virtualization.errorValidatingViewName') +
      (response.error ? ' : ' + response.error : '')
    );
  };

  const onSave = async (value: any) => {
    let validationMsg = validateDescription(value.description);
    if (validationMsg.length === 0) {
      validationMsg = await doValidateName(value.name);
    }
    if (validationMsg.length === 0) {
      // ViewDefinition for the source
      const viewDefinition = generateViewDefinition(
        state.schemaNodeInfo,
        params.virtualizationId,
        value.name,
        value.description
      );
      try {
        await saveViewDefinition(viewDefinition);
        pushNotification(
          t('virtualization.createViewSuccess', {
            name: viewDefinition.name,
          }),
          'success'
        );
      } catch (error) {
        const details = error.message ? error.message : '';
        pushNotification(
          t('virtualization.createViewFailed', {
            details,
          }),
          'error'
        );
      }
      history.push(
        resolvers.data.virtualizations.views.root({
          virtualization: state.virtualization,
        })
      );
    } else {
      pushNotification(validationMsg, 'error');
    }
  };

  const definition: IFormDefinition = {
    name: {
      defaultValue: '',
      displayName: i18n.t('data:virtualization.viewNameDisplay'),
      required: true,
      type: 'string',
    },
    /* tslint:disable-next-line:object-literal-sort-keys */
    description: {
      defaultValue: '',
      displayName: i18n.t('data:virtualization.viewDescriptionDisplay'),
      type: 'textarea',
    },
  };

  return (
    <AutoForm<ISaveForm>
      i18nRequiredProperty={t('shared:requiredFieldMessage')}
      definition={definition}
      initialValue={{
        description: '',
        name: '',
      }}
      onSave={(properties, actions) => {
        onSave(properties).finally(() => {
          actions.setSubmitting(false);
        });
      }}
    >
      {({ fields, handleSubmit, isSubmitting, isValid, submitForm }) => (
        <ViewCreateLayout
          header={<ViewCreateSteps step={2} />}
          content={
            <>
              <PageTitle
                title={i18n.t(
                  'data:virtualization.createViewWizardSelectNameTitle'
                )}
              />
              <ViewConfigurationForm
                i18nFormTitle={i18n.t(
                  'data:virtualization.createViewWizardSelectNameTitle'
                )}
                handleSubmit={handleSubmit}
              >
                {fields}
              </ViewConfigurationForm>
            </>
          }
          cancelHref={resolvers.data.virtualizations.views.root({
            virtualization: state.virtualization,
          })}
          backHref={resolvers.data.virtualizations.views.createView.selectSources(
            { virtualization: state.virtualization }
          )}
          onNext={submitForm}
          isNextDisabled={!isValid}
          isNextLoading={isSubmitting}
          isLastStep={true}
        />
      )}
    </AutoForm>
  );
};
