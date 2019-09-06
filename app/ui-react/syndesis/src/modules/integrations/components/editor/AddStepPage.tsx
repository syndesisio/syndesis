import {
  getFirstPosition,
  getFlow,
  getLastPosition,
  getMetadataValue,
  getSteps,
  isDefaultFlow,
  isIntegrationApiProvider,
  isPrimaryFlow,
  useIntegrationHelpers,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Step } from '@syndesis/models';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IntegrationEditorLayout,
  PageLoader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { IntegrationEditorStepAdder } from '../IntegrationEditorStepAdder';
import {
  IBaseFlowRouteParams,
  IBaseRouteState,
  IPageWithEditorBreadcrumb,
} from './interfaces';
import { getStepHref, IGetStepHrefs } from './utils';

export interface IAddStepPageProps
  extends IGetStepHrefs,
    IPageWithEditorBreadcrumb {
  cancelHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getAddMapperStepHref: (
    position: number,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getGotoDescribeDataHref: (
    position: number,
    flowId: string,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getAddStepHref: (
    position: number,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getDeleteEdgeStepHref: (
    position: number,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptorObject;
  saveHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  selfHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptorObject;
  getFlowHref: (
    flowId: string,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
}

export interface IAddStepPageState {
  position?: number;
  showDeleteDialog: boolean;
  step?: Step;
}

/**
 * This page shows the steps of an existing integration.
 *
 * This component expects a [state]{@link IBaseRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo make this page shareable by making the [integration]{@link IBaseRouteState#integration}
 * optional and adding a WithIntegration component to retrieve the integration
 * from the backend
 */

export const AddStepPage: React.FunctionComponent<
  IAddStepPageProps
> = props => {
  const {
    cancelHref,
    getAddMapperStepHref,
    getAddStepHref,
    getDeleteEdgeStepHref,
    saveHref,
    selfHref,
    getBreadcrumb,
    getFlowHref,
    getGotoDescribeDataHref,
  } = props;
  const { params, state, history, location } = useRouteData<
    IBaseFlowRouteParams,
    IBaseRouteState
  >();
  const [position, setPosition] = React.useState(0);
  const [showDeleteDialog, setShowDeleteDialog] = React.useState(false);
  const [isDeleting, setIsDeleting] = React.useState(false);
  const { removeStep } = useIntegrationHelpers();

  const closeDeleteDialog = (): void => {
    setShowDeleteDialog(false);
  };

  const openDeleteDialog = (): void => {
    setShowDeleteDialog(true);
  };

  const handleDeleteConfirm = () => {
    if (showDeleteDialog) {
      closeDeleteDialog();
    }
  };

  const onDelete = (idx: number, s: Step): void => {
    setPosition(idx);
    openDeleteDialog();
  };

  const isApiProvider = isIntegrationApiProvider(state.integration);
  const currentFlow = getFlow(state.integration, params.flowId);
  const isPrimary = isPrimaryFlow(currentFlow!);
  const primaryFlow = isPrimary
    ? currentFlow
    : getFlow(
      state.integration,
      getMetadataValue<string>('primaryFlowId', currentFlow!.metadata)!
    );

  return (
    <Translation ns={['integrations', 'shared']}>
      {t => (
        <React.Fragment key={location.key}>
          {showDeleteDialog && (
            <ConfirmationDialog
              buttonStyle={ConfirmationButtonStyle.NORMAL}
              icon={ConfirmationIconType.DANGER}
              i18nCancelButtonText={t('shared:Cancel')}
              i18nConfirmButtonText={t('shared:Delete')}
              i18nConfirmationMessage={t(
                'integrations:editor:confirmDeleteStepDialogBody'
              )}
              i18nTitle={t('integrations:editor:confirmDeleteStepDialogTitle')}
              showDialog={showDeleteDialog}
              onCancel={closeDeleteDialog}
              onConfirm={async () => {
                handleDeleteConfirm();

                /**
                 * Check if step is first or last position,
                 * in which case you should delete the step and
                 * subsequently redirect the user to the step select
                 * page for that position.
                 */
                if (
                  position ===
                    getFirstPosition(state.integration, params.flowId) ||
                  position === getLastPosition(state.integration, params.flowId)
                ) {
                  history.push(getDeleteEdgeStepHref(position!, params, state));
                } else {
                  /**
                   * Remove the step from the integration flow
                   * and receive a copy of the new integration.
                   */
                  setIsDeleting(true);
                  const newInt = await removeStep(
                    state.integration,
                    params.flowId,
                    position!
                  );
                  setIsDeleting(false);
                  /**
                   * If is a middle step, simply remove the step
                   * and update the UI.
                   */
                  history.push(
                    selfHref(params, {
                      ...state,
                      integration: newInt,
                    })
                  );
                }
              }}
            />
          )}
          <PageTitle title={t('integrations:editor:saveOrAddStep')} />
          <IntegrationEditorLayout
            title={isPrimary
              ? t('integrations:editor:addToIntegration')
              : currentFlow!.description || ''
            }
            description={t('integrations:editor:addStepDescription')}
            toolbar={getBreadcrumb(
              t('integrations:editor:addToIntegration'),
              params,
              state
            )}
            content={
              <WithLoader
                loading={isDeleting}
                loaderChildren={<PageLoader />}
                error={false}
                errorChildren={<span />}
              >
                {() => (
                  <IntegrationEditorStepAdder
                    steps={getSteps(state.integration, params.flowId)}
                    addDataMapperStepHref={p =>
                      getAddMapperStepHref(p, params, state)
                    }
                    addStepHref={p => getAddStepHref(p, params, state)}
                    configureStepHref={(p: number, s: Step) =>
                      getStepHref(
                        s,
                        { ...params, position: `${p}` },
                        state,
                        props
                      )
                    }
                    getFlowHref={flowId => getFlowHref(flowId, params, state)}
                    gotoDescribeDataHref={p =>
                      getGotoDescribeDataHref(p, params.flowId, params, state)
                    }
                    flowId={params.flowId}
                    integration={state.integration}
                    onDelete={onDelete}
                  />
                )}
              </WithLoader>
            }
            cancelHref={cancelHref(params, state)}
            saveHref={saveHref(params, state)}
            publishHref={saveHref(params, state)}
            isApiProvider={isApiProvider}
            isAlternateFlow={!isPrimary}
            isDefaultFlow={isDefaultFlow(currentFlow!)}
            primaryFlowHref={getFlowHref(primaryFlow!.id!, params, state)}
          />
        </React.Fragment>
      )}
    </Translation>
  );
};
