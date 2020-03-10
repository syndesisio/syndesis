/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { getSteps } from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  ButtonLink,
  IntegrationEditorActionsListItem,
  IntegrationEditorChooseAction,
  IntegrationEditorLayout,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { PageTitle } from '../../../../../shared';
import { IEditorSidebarProps } from '../EditorSidebar';
import {
  IChoiceStepRouteParams,
  IChoiceStepRouteState,
  IPageWithEditorBreadcrumb,
  ISelectConfigModeRouteParams,
  ISelectConfigModeRouteState,
} from '../interfaces';
import { toUIStep, toUIStepCollection } from '../utils';

export interface ISelectChoiceConfigModePageProps
  extends IPageWithEditorBreadcrumb {
  cancelHref: (
    p: ISelectConfigModeRouteParams,
    s: ISelectConfigModeRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: IEditorSidebarProps) => React.ReactNode;
  selectHref: (
    p: IChoiceStepRouteParams,
    s: IChoiceStepRouteState
  ) => H.LocationDescriptor;
}

/**
 * This page shows the list of actions of a connection containing either a
 * **to** or **from pattern, depending on the specified [position]{@link ISelectConfigModeRouteParams#position}.
 *
 * This component expects some [params]{@link ISelectConfigModeRouteParams} and
 * [state]{@link ISelectConfigModeRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export const SelectChoiceConfigModePage: React.FunctionComponent<ISelectChoiceConfigModePageProps> = ({
  getBreadcrumb,
  cancelHref,
  sidebar,
  selectHref,
}) => {
  const { t } = useTranslation(['integrations', 'shared']);
  return (
    <WithRouteData<ISelectConfigModeRouteParams, ISelectConfigModeRouteState>>
      {(params, state) => {
        const positionAsNumber = parseInt(params.position, 10);
        const options = [
          {
            description: t(
              'integrations:editor:selectChoiceMode:basicDescription'
            ),
            mode: 'basic',
            name: t('integrations:editor:selectChoiceMode:basicName'),
          },
          {
            description: t(
              'integrations:editor:selectChoiceMode:advancedDescription'
            ),
            mode: 'advanced',
            name: t('integrations:editor:selectChoiceMode:advancedName'),
          },
        ];
        return (
          <>
            <PageTitle
              title={t(
                'integrations:editor:selectChoiceMode:ConfigureConditionalFlows'
              )}
            />
            <IntegrationEditorLayout
              title={t(
                'integrations:editor:selectChoiceMode:ConfigureConditionalFlows'
              )}
              description={t(
                'integrations:editor:selectChoiceMode:ConfigureConditionalFlows'
              )}
              toolbar={getBreadcrumb(
                t(
                  'integrations:editor:selectChoiceMode:ConfigureConditionalFlows'
                ),
                params,
                state
              )}
              sidebar={sidebar({
                activeIndex: positionAsNumber,
                activeStep: toUIStep(state.step),
                steps: toUIStepCollection(
                  getSteps(state.integration, params.flowId)
                ),
              })}
              content={
                <IntegrationEditorChooseAction>
                  {options.map((option, idx) => (
                    <IntegrationEditorActionsListItem
                      key={idx}
                      name={option.name}
                      description={
                        option.description || t('shared:NoDescriptionAvailable')
                      }
                      actions={
                        <ButtonLink
                          data-testid={'select-action-page-select-button'}
                          href={selectHref(
                            {
                              ...params,
                              configMode: option.mode,
                            } as IChoiceStepRouteParams,
                            {
                              ...state,
                              step: {
                                ...state.step,
                                configuredProperties: undefined,
                              },
                            } as IChoiceStepRouteState
                          )}
                        >
                          {t('shared:Select')}
                        </ButtonLink>
                      }
                    />
                  ))}
                </IntegrationEditorChooseAction>
              }
              cancelHref={cancelHref(params, state)}
            />
          </>
        );
      }}
    </WithRouteData>
  );
};
