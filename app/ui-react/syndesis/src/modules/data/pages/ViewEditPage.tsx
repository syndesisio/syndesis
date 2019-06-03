import { WithVdbModel, WithVirtualizationHelpers } from '@syndesis/api';
import {
  RestDataService,
  ViewDefinition,
  ViewEditorState,
} from '@syndesis/models';
import {
  Breadcrumb,
  IViewEditValidationResult,
  PageLoader,
  ViewEditContent,
  ViewEditHeader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import { getViewDdl } from '../shared/VirtualizationUtils';

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
  viewDescription: string;
  viewValid: boolean;
}

export class ViewEditPage extends React.Component<{}, IViewEditPageState> {
  public constructor(props: {}) {
    super(props);
    this.state = {
      isWorking: false,
      validationResults: [],
      viewDescription: '',
      viewValid: true,
    };
    this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    this.handleNameChange = this.handleNameChange.bind(this);
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
              <WithVirtualizationHelpers>
                {({ refreshVirtualizationViews, validateViewDefinition }) => {
                  return (
                    <WithVdbModel
                      vdbId={virtualization.serviceVdbName}
                      modelId={'views'}
                    >
                      {({ data, hasData, error }) => {
                        const startingViewDdl = getViewDdl(
                          data,
                          viewDefinition.viewName
                        );
                        // Save the View with new DDL and description
                        const handleSaveView = async (ddlValue: string) => {
                          // View Definition
                          const viewDefn: ViewDefinition = {
                            compositions: viewDefinition.compositions,
                            ddl: ddlValue,
                            isComplete: viewDefinition.isComplete,
                            keng__description: this.state.viewDescription,
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
                          await refreshVirtualizationViews(
                            virtualization.keng__id,
                            [viewEditorState]
                          );
                          // TODO: post toast notification
                          history.push(
                            resolvers.data.virtualizations.views.root({
                              virtualization,
                            })
                          );
                        };
                        // Validate the View using the new DDL
                        const handleValidateView = async (ddlValue: string) => {
                          this.handleValidationStarted();

                          // View Definition
                          const viewDefn: ViewDefinition = {
                            compositions: viewDefinition.compositions,
                            ddl: ddlValue,
                            isComplete: viewDefinition.isComplete,
                            keng__description: this.state.viewDescription,
                            projectedColumns: viewDefinition.projectedColumns,
                            sourcePaths: viewDefinition.sourcePaths,
                            viewName: viewDefinition.viewName,
                          };

                          const validationResponse = await validateViewDefinition(
                            viewDefn
                          );
                          // TODO: Update validation message when service is complete
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
                          // TODO: post toast notification
                        };
                        const handleCancel = () => {
                          history.push(
                            resolvers.data.virtualizations.views.root({
                              virtualization,
                            })
                          );
                        };
                        return (
                          <WithLoader
                            error={error}
                            loading={!hasData}
                            loaderChildren={<PageLoader />}
                            errorChildren={<ApiError />}
                          >
                            {() => {
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
                                    viewDdl={startingViewDdl}
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
                          </WithLoader>
                        );
                      }}
                    </WithVdbModel>
                  );
                }}
              </WithVirtualizationHelpers>
            )}
          </Translation>
        )}
      </WithRouteData>
    );
  }
}
