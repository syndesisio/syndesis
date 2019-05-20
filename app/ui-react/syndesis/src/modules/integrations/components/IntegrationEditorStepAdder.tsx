import {
  getFirstPosition,
  getLastPosition,
  getStepIcon,
  removeStepFromFlow,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, Step } from '@syndesis/models';
import {
  ButtonLink,
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IntegrationEditorStepsList,
  IntegrationEditorStepsListItem,
  IntegrationFlowAddStep,
  PageSection,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import {
  toUIIntegrationStepCollection,
  toUIStepCollection,
} from './editor/utils';

export interface IIntegrationEditorStepAdderProps {
  /**
   * the list of steps to render.
   */
  steps: Step[];
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the 'Add a data mapping step' link
   * @param idx - the zero-based index where a new connection should be added
   */
  addDataMapperStepHref: (idx: number) => H.LocationDescriptor;
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Add Connection button, or when deleting the first or last
   * step
   * @param idx - the zero-based index where a new connection should be added
   */
  editAddStepHref: (idx: number) => H.LocationDescriptor;
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Edit Step button
   * @param stepIdx - the zero-based index of the integration step that should
   * be edited
   * @param step - the integration step object that should be edited
   */
  // tslint:disable-next-line:react-unused-props-and-state
  configureStepHref: (
    stepIdx: number,
    step: Step
  ) => H.LocationDescriptorObject;
  deleteAction: (integration: Integration) => void;
  flowId: string;
  integration: Integration;
}

export interface IIntegrationEditorStepAdderState {
  position: number;
  showDeleteDialog: boolean;
  step?: Step;
  stepIdx?: number;
}

/**
 * A component to render the steps of an integration with the required action
 * buttons to add a new step, edit an existing one, etc.
 *
 * @see [steps]{@link IIntegrationEditorStepAdderProps#steps}
 * @see [addStepHref]{@link IIntegrationEditorStepAdderProps#addStepHref}
 * @see [addStepHref]{@link IIntegrationEditorStepAdderProps#addStepHref}
 * @see [configureStepHref]{@link IIntegrationEditorStepAdderProps#configureStepHref}
 *
 * @todo add the delete step button
 */
export class IntegrationEditorStepAdder extends React.Component<
  IIntegrationEditorStepAdderProps,
  IIntegrationEditorStepAdderState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      position: 0,
      showDeleteDialog: false,
      step: {},
      stepIdx: 0,
    };

    this.onDelete = this.onDelete.bind(this);
    this.closeDeleteDialog = this.closeDeleteDialog.bind(this);
    this.handleDeleteConfirm = this.handleDeleteConfirm.bind(this);
  }

  public handleDeleteConfirm() {
    if (this.state.showDeleteDialog) {
      this.closeDeleteDialog();
    }

    const newInt = removeStepFromFlow(
      this.props.integration!,
      this.props.flowId!,
      this.state.position!
    );

    this.props.deleteAction(newInt);
  }

  public onDelete(idx: any, step: Step): void {
    console.log('step: ' + JSON.stringify(step));
    // console.log('idx: ' + idx);
    // console.log('firstPosition: ' + getFirstPosition(this.props.integration, this.props.flowId));
    // console.log('lastPosition: ' + getLastPosition(this.props.integration, this.props.flowId));

    if (idx === getFirstPosition(this.props.integration, this.props.flowId)) {
      console.log('Is first position');
    }

    if (idx === getLastPosition(this.props.integration, this.props.flowId)) {
      console.log('Is last position');
    }

    // Check if it's an API provider step that can't be deleted
    if (step.configuredProperties!.stepKind === 'mapper') {
      console.log('Data mapper step');
    }

    this.setState({
      position: idx,
      showDeleteDialog: true,
      step: step,
      stepIdx: idx,
    });
  }

  public closeDeleteDialog(): void {
    this.setState({
      position: 0,
      showDeleteDialog: false,
      step: {},
      stepIdx: 0,
    });
  }

  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <>
            {this.state.showDeleteDialog && (
              <ConfirmationDialog
                buttonStyle={ConfirmationButtonStyle.NORMAL}
                icon={ConfirmationIconType.DANGER}
                i18nCancelButtonText={t('shared:Cancel')}
                i18nConfirmButtonText={t('shared:Delete')}
                i18nConfirmationMessage={t(
                  'integrations:editor:confirmDeleteStepDialogBody'
                )}
                i18nTitle={t(
                  'integrations:editor:confirmDeleteStepDialogTitle'
                )}
                showDialog={this.state.showDeleteDialog}
                onCancel={this.closeDeleteDialog}
                onConfirm={() => {
                  this.handleDeleteConfirm();
                }}
              />
            )}
            <PageSection>
              <IntegrationEditorStepsList>
                {toUIIntegrationStepCollection(
                  toUIStepCollection(this.props.steps)
                ).map((s, idx) => (
                  <React.Fragment key={idx}>
                    <IntegrationEditorStepsListItem
                      stepName={(s.action && s.action.name) || s.name!}
                      stepDescription={
                        (s.action! && s.action!.description) || ''
                      }
                      action={(s.action && s.action.name) || 'n/a'}
                      shape={s.shape || 'n/a'}
                      icon={getStepIcon(process.env.PUBLIC_URL, s)}
                      showWarning={
                        s.shouldAddDataMapper ||
                        s.previousStepShouldDefineDataShape
                      }
                      i18nWarningTitle={'Data Type Mismatch'}
                      i18nWarningMessage={
                        s.previousStepShouldDefineDataShape ? (
                          <>
                            <a
                              data-testid={
                                'integration-editor-step-adder-define-data-type'
                              }
                              href={'/todo'}
                            >
                              Define the data type
                            </a>{' '}
                            for the previous step to resolve this warning.
                          </>
                        ) : (
                          <>
                            <Link
                              data-testid={
                                'integration-editor-step-adder-add-step-before-connection'
                              }
                              to={this.props.addDataMapperStepHref(idx)}
                            >
                        {t('integrations:editor:addStepDataMapping')}
                            </Link>{' '}
                            {t('integrations:editor:addStepDataMappingTrail')}
                          </>
                        )
                      }
                      actions={
                        <>
                          <ButtonLink
                            data-testid={
                              'integration-editor-step-adder-configure'
                            }
                            href={this.props.configureStepHref(
                              idx,
                              this.props.steps[idx]
                            )}
                          >
                            {t('shared:Configure')}
                          </ButtonLink>
                          <ButtonLink
                            data-testid={'integration-editor-step-adder-delete'}
                            onClick={() => this.onDelete(idx)}
                            as={'danger'}
                          >
                            <i className="fa fa-trash" />
                          </ButtonLink>
                        </>
                      }
                    />
                    {idx < this.props.steps.length - 1 && (
                      <IntegrationFlowAddStep
                        active={false}
                        showDetails={false}
                        addStepHref={this.props.addStepHref(idx + 1)}
                        i18nAddStep={t('integrations:editor:addStep')}
                      />
                      {idx < this.props.steps.length - 1 && (
                        <IntegrationFlowAddStep
                          active={false}
                          showDetails={false}
                          addStepHref={this.props.editAddStepHref(idx + 1)}
                          i18nAddStep={t('integrations:editor:addStep')}
                        />
                      )}
                    </React.Fragment>
                  );
                })}
              </IntegrationEditorStepsList>
            </PageSection>
          </>
        )}
      </Translation>
    );
  }
}
