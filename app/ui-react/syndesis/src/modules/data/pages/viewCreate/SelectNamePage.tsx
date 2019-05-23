import { WithViewEditorStates, WithVirtualizationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { RestDataService, SchemaNodeInfo, ViewInfo } from '@syndesis/models';
import { ViewConfigurationForm, ViewCreateLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../../app';
import i18n from '../../../../i18n';
import { PageTitle } from '../../../../shared';
import resolvers from '../../../resolvers';
import { ViewCreateSteps } from '../../shared';
import { generateViewEditorState } from '../../shared/VirtualizationUtils';

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
  schemaNodeInfo: SchemaNodeInfo;
}

export class SelectNamePage extends React.Component {
  public selectedViews: ViewInfo[] = []; // Maintains list of selected views

  public render() {
    return (
      <Translation ns={['data', 'shared']}>
        {t => (
          <UIContext.Consumer>
            {({ pushNotification }) => {
              return (
                <WithRouteData<ISelectNameRouteParams, ISelectNameRouteState>>
                  {(
                    { virtualizationId },
                    { virtualization, schemaNodeInfo },
                    { history }
                  ) => (
                    <WithVirtualizationHelpers>
                      {({ refreshVirtualizationViews }) => {
                        const onSave = async (
                          { name, description }: ISaveForm,
                          actions: any
                        ) => {
                          // ViewEditorState for the source
                          const viewEditorState = generateViewEditorState(
                            virtualization.serviceVdbName,
                            schemaNodeInfo,
                            name,
                            description
                          );
                          try {
                            await refreshVirtualizationViews(
                              virtualization.keng__id,
                              [viewEditorState]
                            );
                            pushNotification(
                              t('virtualization.createViewSuccess', {
                                name: viewEditorState.viewDefinition.viewName,
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
                              virtualization,
                            })
                          );
                        };
                        const definition: IFormDefinition = {
                          name: {
                            defaultValue: '',
                            displayName: i18n.t(
                              'data:virtualization.viewNameDisplay'
                            ),
                            required: true,
                            type: 'string',
                          },
                          /* tslint:disable-next-line:object-literal-sort-keys */
                          description: {
                            defaultValue: '',
                            displayName: i18n.t(
                              'data:virtualization.viewDescriptionDisplay'
                            ),
                            type: 'textarea',
                          },
                        };
                        return (
                          <WithViewEditorStates
                            idPattern={virtualization.serviceVdbName + '*'}
                          >
                            {({ data, hasData, error }) => {
                              const validate = (v: { name: string }) => {
                                const errors: any = {};
                                // TODO Improve name validation
                                if (v.name.includes('?')) {
                                  errors.name =
                                    'View name contains an illegal character';
                                }
                                return errors;
                              };
                              return (
                                <AutoForm<ISaveForm>
                                  i18nRequiredProperty={'* Required field'}
                                  definition={definition}
                                  initialValue={{
                                    description: '',
                                    name: '',
                                  }}
                                  validate={validate}
                                  onSave={onSave}
                                >
                                  {({
                                    fields,
                                    handleSubmit,
                                    isSubmitting,
                                    isValid,
                                    submitForm,
                                  }) => (
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
                                      cancelHref={resolvers.data.virtualizations.views.root(
                                        {
                                          virtualization,
                                        }
                                      )}
                                      backHref={resolvers.data.virtualizations.views.createView.selectSources(
                                        { virtualization }
                                      )}
                                      onNext={submitForm}
                                      isNextDisabled={!isValid}
                                      isNextLoading={isSubmitting}
                                      isLastStep={true}
                                    />
                                  )}
                                </AutoForm>
                              );
                            }}
                          </WithViewEditorStates>
                        );
                      }}
                    </WithVirtualizationHelpers>
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
