import { getStepIcon } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Integration, Step } from '@syndesis/models';
import {
  ButtonLink,
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
    step: Step
  ) => H.LocationDescriptorObject;
  flowId: string;
  integration: Integration;
  onDelete: (idx: number, step: Step) => void;
}

/**
 * A component to render the steps of an integration with the required action
 * buttons to add a new step, edit an existing one, etc.
 *
 * @see [steps]{@link IIntegrationEditorStepAdderProps#steps}
 * @see [addStepHref]{@link IIntegrationEditorStepAdderProps#addStepHref}
 * @see [configureStepHref]{@link IIntegrationEditorStepAdderProps#configureStepHref}
 *
 * @todo add the delete step button
 */
export class IntegrationEditorStepAdder extends React.Component<
  IIntegrationEditorStepAdderProps
> {
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <PageSection>
            <IntegrationEditorStepsList>
              {toUIIntegrationStepCollection(
                toUIStepCollection(this.props.steps)
              ).map((s, idx) => {
                let restrictedDelete = false;

                if (
                  (s.configuredProperties &&
                    s.configuredProperties!.stepKind === 'choice') ||
                  (s.connection &&
                    s.connection!.connectorId! === 'api-provider')
                ) {
                  restrictedDelete = true;
                }

                return (
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
                                'integration-editor-step-adder-define-data-type-link'
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
                                'integration-editor-step-adder-add-step-before-connection-link'
                              }
                              to={this.props.addDataMapperStepHref(idx)}
                            >
                              Add a data mapping step
                            </Link>{' '}
                            before this connection to resolve the difference.
                          </>
                        )
                      }
                      actions={
                        <>
                          <ButtonLink
                            data-testid={
                              'integration-editor-step-adder-configure-button'
                            }
                            href={this.props.configureStepHref(
                              idx,
                              this.props.steps[idx]
                            )}
                          >
                            {t('shared:Configure')}
                          </ButtonLink>
                          {!restrictedDelete && (
                            <ButtonLink
                              data-testid={
                                'integration-editor-step-adder-delete-button'
                              }
                              onClick={() => this.props.onDelete(idx, s)}
                              as={'danger'}
                            >
                              <i className="fa fa-trash" />
                            </ButtonLink>
                          )}
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
                );
              })}
            </IntegrationEditorStepsList>
          </PageSection>
        )}
      </Translation>
    );
  }
}
