import {
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
   * clicking the Add Connection button
   * @param idx - the zero-based index where a new connection should be added
   */
  addStepHref: (idx: number) => H.LocationDescriptor;
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
    step: Step,
  ) => H.LocationDescriptorObject;
  deleteAction: (integration: Integration) => void;
  flowId: string;
  integration: Integration;
}

export interface IIntegrationEditorStepAdderState {
  position: number;
  showDeleteDialog: boolean;
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
export class IntegrationEditorStepAdder extends React.Component<IIntegrationEditorStepAdderProps,
  IIntegrationEditorStepAdderState> {
  constructor(props: any) {
    super(props);
    this.state = {
      position: 0,
      showDeleteDialog: false,
    };

    this.onDelete = this.onDelete.bind(this);
    this.closeDeleteDialog = this.closeDeleteDialog.bind(this);
    this.handleDeleteConfirm = this.handleDeleteConfirm.bind(this);
  }

  public handleDeleteConfirm() {
    if (this.state.showDeleteDialog) {
      this.closeDeleteDialog();
    }
  }

  public onDelete(idx: any): void {
    this.setState({ position: idx, showDeleteDialog: true });
  }

  public closeDeleteDialog(): void {
    this.setState({ position: 0, showDeleteDialog: false });
  }

  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
<<<<<<< HEAD
          <>
            {this.state.showDeleteDialog && (
              <ConfirmationDialog
                buttonStyle={ConfirmationButtonStyle.NORMAL}
                icon={ConfirmationIconType.DANGER}
                i18nCancelButtonText={t('shared:Cancel')}
                i18nConfirmButtonText={t('shared:Delete')}
                i18nConfirmationMessage={t(
                  'integrations:editor:confirmDeleteStepDialogBody',
=======
          <WithIntegrationHelpers>
            {({ removeStep }) => (
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
                      removeStepFromFlow(
                        this.props.integration!,
                        this.props.flowId!,
                        this.state.position!
                      );
                    }}
                  />
>>>>>>> Fix for removeStepFromFlow, get steps separately
                )}
                i18nTitle={t(
                  'integrations:editor:confirmDeleteStepDialogTitle',
                )}
                showDialog={this.state.showDeleteDialog}
                onCancel={this.closeDeleteDialog}
                onConfirm={() => {
                  this.handleDeleteConfirm();
                  const newInt = removeStepFromFlow(
                    this.props.integration!,
                    this.props.flowId!,
                    this.state.position!
                  );
                  this.props.deleteAction(newInt);
                }}
              />
            )}
            <PageSection>
              <IntegrationEditorStepsList>
                {toUIIntegrationStepCollection(
                  toUIStepCollection(this.props.steps),
                ).map((s, idx) => (
                  <React.Fragment key={idx}>
                    <IntegrationEditorStepsListItem
                      stepName={(s.action && s.action.name) || s.name!}
                      stepDescription={(s.action! && s.action!.description) || ''}
                      action={(s.action && s.action.name) || 'n/a'}
                      shape={s.shape || 'n/a'}
                      icon={getStepIcon(process.env.PUBLIC_URL, s)}
                      showWarning={
                        s.shouldAddDataMapper || s.previousStepShouldDefineDataShape
                      }
                      i18nWarningTitle={'Data Type Mismatch'}
                      i18nWarningMessage={
                        s.previousStepShouldDefineDataShape ? (
                          <>
                            <a href={'/todo'}>Define the data type</a> for the
                            previous step to resolve this warning.
                          </>
                        ) : (
                          <>
                            <Link to={this.props.addDataMapperStepHref(idx)}>
                              Add a data mapping step
                            </Link>{' '}
                            before this connection to resolve the difference.
                          </>
                        )
                      }
                      actions={
                        <>
                          <ButtonLink
                            href={this.props.configureStepHref(
                              idx,
                              this.props.steps[idx],
                            )}
                          >
                            {t('shared:Configure')}
                          </ButtonLink>
                          <ButtonLink href={'#'} onClick={() => this.onDelete(idx)} as={'danger'}>
                            <i className="fa fa-trash"/>
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
                    )}
                  </React.Fragment>
                ))}
              </IntegrationEditorStepsList>
            </PageSection>
          </>
        )}
      </Translation>
    );
  }
}
