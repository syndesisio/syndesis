import { WithVirtualizationHelpers } from '@syndesis/api';
import {
  RestDataService,
  ViewDefinition,
  ViewEditorState,
} from '@syndesis/models';
import {
  Breadcrumb,
  IViewEditValidationResult,
  ViewEditContent,
  ViewEditHeader,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import resolvers from '../../resolvers';

/**
 * @param virtualization - the Virtualization
 * @param editorState - the ViewDefinition
 */
export interface IViewEditRouteState {
  virtualization: RestDataService;
  viewDefinition: ViewDefinition;
}

export interface IViewEditPageState {
  isWorking: boolean;
  validationResults: IViewEditValidationResult[];
  viewDescription: string | '[unchanged]';
  viewValid: boolean;
}

export class ViewEditPage extends React.Component<{}, IViewEditPageState> {

  public constructor(props: {}) {
    super(props);
    this.state = {
      isWorking: false,
      validationResults: [],
      viewDescription: '[unchanged]',
      viewValid: true,
    };
    this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    this.handleNameChange = this.handleNameChange.bind(this);
    this.handleValidationStarted = this.handleValidationStarted.bind(this);
    this.handleValidationComplete = this.handleValidationComplete.bind(this);
  }

  public handleDescriptionChange = async (descr: string): Promise<boolean> => {
    this.setState({
      ...this.state,
      viewDescription: descr,
    });
    return true;
  };

  public handleNameChange = async (name: string): Promise<boolean> => {
    return true;
  };

  public handleValidationStarted = async (): Promise<void> => {
    this.setState({
      ...this.state,
      isWorking: true,
    });
  };

  public handleValidationComplete = async (
    validation: IViewEditValidationResult
  ): Promise<void> => {
    this.setState({
      ...this.state,
      isWorking: false,
      validationResults: [validation],
      viewValid: validation.type === 'success',
    });
  };

  public render() {
    return (
      <WithRouteData<null, IViewEditRouteState>>
        {(_, { virtualization, viewDefinition }, { history }) => (
          <Translation ns={['data', 'shared']}>
            {t => (
              <UIContext.Consumer>
                {({ pushNotification }) => {
                  return (
                    <WithVirtualizationHelpers>
                      {({ deleteViewEditorState, refreshVirtualizationViews, validateViewDefinition }) => {
                        // Save the View with new DDL and description
                        const handleSaveView = async (ddlValue: string) => {
                          const vwDesc = this.state.viewDescription === '[unchanged]' ? viewDefinition.keng__description : this.state.viewDescription;
                          // View Definition
                          const viewDefn: ViewDefinition = {
                            compositions: viewDefinition.compositions,
                            ddl: ddlValue,
                            isComplete: viewDefinition.isComplete,
                            isUserDefined: true,
                            keng__description: vwDesc,
                            projectedColumns: viewDefinition.projectedColumns,
                            sourcePaths: viewDefinition.sourcePaths,
                            viewName: viewDefinition.viewName,
                          };

                          const viewEditorState: ViewEditorState = {
                            id:
                              virtualization.serviceVdbName +
                              '.' +
                              viewDefinition.viewName,
                            viewDefinition: viewDefn,
                          };
                          try {
                            await refreshVirtualizationViews(
                              virtualization.keng__id,
                              [viewEditorState]
                            );
                            pushNotification(
                              t(
                                'virtualization.saveViewSuccess',
                                { name: viewDefinition.viewName }
                              ),
                              'success'
                            );
                            // redirect to views page on success
                            history.push(
                              resolvers.data.virtualizations.views.root({
                                virtualization,
                              })
                            );
                          } catch (error) {
                            const details = error.message
                              ? error.message
                              : '';
                            pushNotification(
                              t('virtualization.saveViewFailed', {
                                details,
                                name: viewDefinition.viewName,
                              }),
                              'error'
                            );
                          }
                        };
                        // Validate the View using the new DDL
                        const handleValidateView = async (ddlValue: string) => {
                          this.handleValidationStarted();

                          // View Definition
                          const viewDefn: ViewDefinition = {
                            compositions: viewDefinition.compositions,
                            ddl: ddlValue,
                            isComplete: viewDefinition.isComplete,
                            isUserDefined: viewDefinition.isUserDefined,
                            keng__description: viewDefinition.keng__description,
                            projectedColumns: viewDefinition.projectedColumns,
                            sourcePaths: viewDefinition.sourcePaths,
                            viewName: viewDefinition.viewName,
                          };

                          const validationResponse = await validateViewDefinition(
                            viewDefn
                          );
                          if (validationResponse.status === 'SUCCESS') {
                            const validationResult = {
                              message: 'Validation successful',
                              type: 'success',
                            } as IViewEditValidationResult;
                            this.handleValidationComplete(validationResult);
                          } else {
                            const validationResult = {
                              message: validationResponse.message,
                              type: 'error',
                            } as IViewEditValidationResult;
                            this.handleValidationComplete(validationResult);
                          }
                        };
                        const handleCancel = () => {
                          history.push(
                            resolvers.data.virtualizations.views.root({
                              virtualization,
                            })
                          );
                        };
                        const initialView = viewDefinition.ddl ? viewDefinition.ddl : '';
                        return (
                          <>
                            <Breadcrumb>
                              <Link to={resolvers.dashboard.root()}>
                                {t('shared:Home')}
                              </Link>
                              <Link to={resolvers.data.root()}>
                                {t('shared:DataVirtualizations')}
                              </Link>
                              <Link
                                to={resolvers.data.virtualizations.views.root(
                                  {
                                    virtualization,
                                  }
                                )}
                              >
                                {virtualization.keng__id}
                              </Link>
                              <span>{viewDefinition.viewName}</span>
                            </Breadcrumb>
                            <ViewEditHeader
                              allowEditing={true}
                              viewDescription={
                                viewDefinition.keng__description
                              }
                              viewName={viewDefinition.viewName}
                              i18nDescriptionLabel={t(
                                'data:virtualization.viewDescriptionDisplay'
                              )}
                              i18nDescriptionPlaceholder={t(
                                'data:virtualization.viewDescriptionPlaceholder'
                              )}
                              i18nNamePlaceholder={t(
                                'data:virtualization.viewNamePlaceholder'
                              )}
                              isWorking={false}
                              onChangeDescription={
                                this.handleDescriptionChange
                              }
                              onChangeName={this.handleNameChange}
                            />
                            <ViewEditContent
                              viewDdl={initialView}
                              i18nCancelLabel={t('shared:Cancel')}
                              i18nSaveLabel={t('shared:Save')}
                              i18nValidateLabel={t('shared:Validate')}
                              isValid={this.state.viewValid}
                              isWorking={this.state.isWorking}
                              onCancel={handleCancel}
                              onValidate={handleValidateView}
                              onSave={handleSaveView}
                              validationResults={
                                this.state.validationResults
                              }
                            />
                          </>
                        );
                      }}
                    </WithVirtualizationHelpers>
                  );
                }}
              </UIContext.Consumer>
            )}
          </Translation>
        )}
      </WithRouteData>
    );
  }
}
