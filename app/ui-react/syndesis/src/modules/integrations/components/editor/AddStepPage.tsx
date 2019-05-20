import { getSteps } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, Step } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { IntegrationEditorStepAdder } from '../IntegrationEditorStepAdder';
import { IBaseRouteParams, IBaseRouteState } from './interfaces';
import { getStepHref, IGetStepHrefs } from './utils';

export interface IAddStepPageProps extends IGetStepHrefs {
  cancelHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
  getAddMapperStepHref: (
    position: number,
    p: IBaseRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getEditAddStepHref: (
    position: number,
    p: IBaseRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  saveHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
  selfHref: (
    p: IBaseRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptorObject;
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
export class AddStepPage extends React.Component<IAddStepPageProps> {
  public render() {
    return (
      <Translation ns={['integrations']}>
        {t => (
          <WithRouteData<IBaseRouteParams, IBaseRouteState>>
            {({ flowId }, { integration }, { history }) => {
              const deleteAction = (newIntegration: Integration) => {
                history.push(
                  this.props.selfHref(
                    { flowId },
                    { integration: newIntegration }
                  )
                );
              };

              return (
                <>
                  <PageTitle title={t('integrations:editor:saveOrAddStep')} />
                  <IntegrationEditorLayout
                    title={t('integrations:editor:addToIntegration')}
                    description={t('integrations:editor:addStepDescription')}
                    content={
                      <IntegrationEditorStepAdder
                        steps={getSteps(integration, flowId)}
                        addDataMapperStepHref={position =>
                          this.props.getAddMapperStepHref(
                            position,
                            { flowId },
                            { integration }
                          )
                        }
                        configureStepHref={(position: number, step: Step) =>
                          getStepHref(
                            step,
                            { flowId, position: `${position}` },
                            { integration },
                            this.props
                          )
                        }
                        editAddStepHref={position =>
                          this.props.getEditAddStepHref(
                            position,
                            { flowId },
                            { integration }
                          )
                        }
                        flowId={flowId}
                        integration={integration}
                        deleteAction={deleteAction}
                      />
                    }
                    cancelHref={this.props.cancelHref(
                      { flowId },
                      { integration }
                    )}
                    saveHref={this.props.saveHref({ flowId }, { integration })}
                    publishHref={this.props.saveHref(
                      { flowId },
                      { integration }
                    )}
                  />
                </>
              );
            }}
          </WithRouteData>
        )}
      </Translation>
    );
  }
}
