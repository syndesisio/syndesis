import {
  ALL_STEPS,
  getEmptyIntegration,
  getLastPosition,
  getSteps,
  useConnections,
  useExtensions,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { StepKind } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { IEditorSidebarProps } from './EditorSidebar';
import { EditorStepsWithToolbar } from './EditorStepsWithToolbar';
import {
  IPageWithEditorBreadcrumb,
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  IUIStep,
} from './interfaces';
import {
  getStepHref,
  IGetStepHrefs,
  mergeConnectionsSources,
  toUIStepCollection,
  visibleStepsByPosition,
} from './utils';

export interface ISelectConnectionPageProps
  extends IGetStepHrefs,
    IPageWithEditorBreadcrumb {
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  isAdding: boolean;
}

/**
 * This page shows the list of connections containing actions with a **to**
 * pattern.
 *
 * This component expects some [params]{@link ISelectConnectionRouteParams} and
 * [state]{@link ISelectConnectionRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export const SelectConnectionPage: React.FunctionComponent<
  ISelectConnectionPageProps
> = props => {
  const { getBreadcrumb, sidebar, cancelHref } = props;
  const { t } = useTranslation(['integrations', 'shared']);
  const { params, state } = useRouteData<
    ISelectConnectionRouteParams,
    ISelectConnectionRouteState
  >();
  const { flowId, position } = params;
  const { integration = getEmptyIntegration() } = state;
  const positionAsNumber = parseInt(position, 10) || 0;
  const integrationSteps = getSteps(integration, flowId);
  const lastPosition = getLastPosition(integration, flowId);

  const {
    resource: connectionsData,
    hasData: hasConnectionsData,
    error: connectionsError,
  } = useConnections();
  const {
    resource: extensionsData,
    hasData: hasExtensionsData,
    error: extensionsError,
  } = useExtensions();

  const stepKinds = mergeConnectionsSources(
    connectionsData.dangerouslyUnfilteredConnections,
    extensionsData.items,
    ALL_STEPS
  );
  const visibleSteps = visibleStepsByPosition(
    stepKinds as StepKind[],
    positionAsNumber,
    integrationSteps
  ) as IUIStep[];

  const title = t('integrations:editor:selectStep:title');
  const description =
    positionAsNumber === 0
      ? t('integrations:editor:selectStep:startDescription')
      : (positionAsNumber === lastPosition && !props.isAdding) ||
        (integrationSteps.length === 1 && positionAsNumber === 1)
      ? t('integrations:editor:selectStep:finishDescription')
      : t('integrations:editor:selectStep:middleDescription');

  return (
    <>
      <PageTitle title={title} />
      <IntegrationEditorLayout
        title={title}
        description={description}
        toolbar={getBreadcrumb(title, params, state)}
        sidebar={sidebar({
          activeIndex: positionAsNumber,
          steps: toUIStepCollection(integrationSteps),
        })}
        content={
          <EditorStepsWithToolbar
            loading={!hasConnectionsData || !hasExtensionsData}
            error={connectionsError || extensionsError}
            getEditorStepHref={step => getStepHref(step, params, state, props)}
            steps={visibleSteps}
            createConnectionButtonStyle={'default'}
          />
        }
        cancelHref={cancelHref(params, state)}
      />
    </>
  );
};
