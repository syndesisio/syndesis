import { getSteps } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Step } from '@syndesis/models';
import {
  IntegrationEditorLayout,
} from '@syndesis/ui';
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
    s: IBaseRouteState,
  ) => H.LocationDescriptor;
  getEditAddStepHref: (
    position: number,
    p: IBaseRouteParams,
    s: IBaseRouteState,
  ) => H.LocationDescriptor;
  saveHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
}

export interface IAddStepPageState {
  showRemoveDialog: boolean;
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
export class AddStepPage extends React.Component<IAddStepPageProps,
  IAddStepPageState> {
  constructor(props: any) {
    super(props);
    this.state = {
      showRemoveDialog: false,
    };

    this.openRemoveDialog = this.openRemoveDialog.bind(this);
    this.closeRemoveDialog = this.closeRemoveDialog.bind(this);
    this.handleRemoveConfirm = this.handleRemoveConfirm.bind(this);
  }

  public handleSave(name: string) {
    if (this.state.showRemoveDialog) {
      this.closeRemoveDialog();
      //this.onRemoveItem(name);
    }
  }

  public handleRemoveConfirm() {
    //this.handleSave(this.state.what!);
  }

  public openRemoveDialog() {
    this.setState({ showRemoveDialog: true });
  }

  public closeRemoveDialog() {
    this.setState({ showRemoveDialog: false });
  }

  public render() {
    return (
      <Translation ns={['integrations']}>
        {t => (
          <WithRouteData<IBaseRouteParams, IBaseRouteState>>
            {({ flowId }, { integration }) => (
              <>
                <PageTitle title={t('integrations:editor:saveOrAddStep')}/>
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
                          { integration },
                        )
                      }
                      addStepHref={position =>
                        this.props.getEditAddStepHref(
                          position,
                          { flowId },
                          { integration },
                        )
                      }
                      configureStepHref={(position: number, step: Step) =>
                        getStepHref(
                          step,
                          { flowId, position: `${position}` },
                          { integration },
                          this.props,
                        )
                      }
                      cancelHref={this.props.cancelHref(
                        { flowId },
                        { integration },
                      )}
                      saveHref={this.props.saveHref({ flowId }, { integration })}
                      publishHref={this.props.saveHref({ flowId }, { integration })}
                      i18nConfirmRemoveButtonText={t('shared:Yes')}
                      openRemoveDialog={this.openRemoveDialog}
                    />
                  }
                  showDialog={this.state.showRemoveDialog}
                  onCancel={this.closeRemoveDialog}
                  onConfirm={this.handleRemoveConfirm}
                />
              </>
            )}
          </WithRouteData>
        )}
      </Translation>
    );
  }
}
