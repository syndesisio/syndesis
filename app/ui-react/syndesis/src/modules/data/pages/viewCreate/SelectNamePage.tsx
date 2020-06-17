import {
  IDvNameValidationResult,
  useVirtualizationHelpers,
} from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { SchemaNodeInfo, Virtualization } from '@syndesis/models';
import {
  IViewConfigurationFormValidationResult,
  ViewConfigurationForm,
  ViewCreateLayout,
  ViewWizardHeader,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
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
  virtualization: Virtualization;
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
    getView,
    saveViewDefinition,
    validateViewName,
  } = useVirtualizationHelpers();
  const [validationResults, setValidationResults] = React.useState<
    IViewConfigurationFormValidationResult[]
  >([]);

  const validateDescription = (desc: string): string => {
    if (desc.includes("'")) {
      return t('viewDescriptionValidationError');
    }
    return '';
  };

  /**
   * Backend name validation only occurs when attempting to create
   * @param proposedName the name to validate
   */
  const doValidateName = async (
    proposedName: string
  ): Promise<IViewConfigurationFormValidationResult> => {
    // make sure name has a value
    if (proposedName === '') {
      return {
        message: t('shared:requiredFieldMessage') as string,
        type: 'danger',
      };
    }

    const response: IDvNameValidationResult = await validateViewName(
      state.virtualization.name,
      proposedName
    );

    if (response.nameExists) {
      return {
        message: t('errorViewNameExists', {
          name: proposedName,
        }),
        type: 'danger',
      };
    }
    if (response.hasError) {
      return {
        message: response.message
          ? response.message
          : t('errorViewNameValidation'),
        type: 'danger',
      };
    }
    return {
      message: '',
      type: 'success',
    };
  };

  const onSave = async (value: any) => {
    const validateDescrMsg = validateDescription(value.description);
    let validation = {
      message: validateDescrMsg,
      type: 'danger',
    } as IViewConfigurationFormValidationResult;
    if (validateDescrMsg.length === 0) {
      validation = await doValidateName(value.name);
    }
    if (validation.type === 'success') {
      // ViewDefinition for the source
      const viewDefinition = generateViewDefinition(
        state.schemaNodeInfo,
        params.virtualizationId,
        value.name,
        value.description
      );
      const saveResult = await saveViewDefinition(viewDefinition);
      if (!saveResult.hasError) {
        const newView = await getView(
          state.virtualization.name,
          viewDefinition.name
        );
        history.push(
          resolvers.data.virtualizations.views.edit.sql({
            virtualization: state.virtualization,
            // tslint:disable-next-line: object-literal-sort-keys
            viewDefinitionId: newView.id!, // id should be defined
          })
        );
      } else {
        const details = saveResult.message ? saveResult.message : '';
        pushNotification(
          t('createViewFailed', {
            details,
          }),
          'error'
        );
        history.push(
          resolvers.data.virtualizations.views.root({
            virtualization: state.virtualization,
          })
        );
      }
    } else {
      setValidationResults([validation]);
    }
  };

  const definition: IFormDefinition = {
    name: {
      defaultValue: '',
      displayName: t('viewNameDisplay'),
      required: true,
      type: 'string',
    },
    /* tslint:disable-next-line:object-literal-sort-keys */
    description: {
      defaultValue: '',
      displayName: t('viewDescriptionDisplay'),
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
      <>
        <PageTitle title={t('createViewPageTitle')} />
        <ViewCreateLayout
          header={
            <ViewWizardHeader
               step={2}
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
              i18nStep1Text={t('shared:ChooseTable')}
              i18nStep2Text={t('shared:NameYourView')}
              i18nBack={t('shared:Back')}
              i18nDone={t('shared:Done')}
              i18nNext={t('shared:Next')}
              i18nCancel={t('shared:Cancel')}
            />
          }
          content={
            <ViewConfigurationForm
              validationResults={validationResults}
              handleSubmit={handleSubmit}
            >
              {fields}
            </ViewConfigurationForm>
          }
        />
      </>
      )}
    </AutoForm>
  );
};
