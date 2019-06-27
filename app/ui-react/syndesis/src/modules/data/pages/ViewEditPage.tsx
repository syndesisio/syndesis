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
  viewValid: boolean;
}

export class ViewEditPage extends React.Component<{}, IViewEditPageState> {

  public constructor(props: {}) {
    super(props);
    this.state = {
      isWorking: false,
      validationResults: [],
      viewValid: true,
    };
    this.handleValidationStarted = this.handleValidationStarted.bind(this);
    this.handleValidationComplete = this.handleValidationComplete.bind(this);
  }

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
                      {({ refreshVirtualizationViews, validateViewDefinition }) => {
                        // Save the View with new DDL and description
                        const handleSaveView = async (ddlValue: string) => {
                          // View Definition
                          const viewDefn: ViewDefinition = {
                            compositions: viewDefinition.compositions,
                            ddl: ddlValue,
                            isComplete: viewDefinition.isComplete,
                            isUserDefined: true,
                            keng__description: viewDefinition.keng__description,
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
                            <ViewEditContent
                              viewDdl={initialView}
                              i18nCancelLabel={t('shared:Cancel')}
                              i18nSaveLabel={t('shared:Save')}
                              i18nTitle={t('virtualization.viewEditorTitle')}
                              i18nDescription={t('virtualization.viewEditorDescription')}
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
