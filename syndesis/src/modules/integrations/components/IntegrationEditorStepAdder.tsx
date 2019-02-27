import { Step } from '@syndesis/models';
import {
  ButtonLink,
  IntegrationEditorStepsList,
  IntegrationEditorStepsListItem,
  IntegrationFlowAddStep,
} from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';

export interface IIntegrationEditorStepAdderProps {
  /**
   * the list of steps to render.
   */
  steps: Step[];
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Add Connection button
   * @param idx - the zero-based index where a new connection should be added
   */
  addConnectionHref(idx: number): H.LocationDescriptor;
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Add Step button
   * @param idx - the zero-based index where a new step should be added
   */
  addStepHref(idx: number): H.LocationDescriptor;
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Edit Connection button
   * @param stepIdx - the zero-based index of the integration step that should
   * be edited
   * @param step - the integration step object that should be edited
   */
  configureConnectionHref(stepIdx: number, step: Step): H.LocationDescriptor;
  /**
   * a callback to get the `LocationDescriptor` that should be reached when
   * clicking the Edit Step button
   * @param stepIdx - the zero-based index of the integration step that should
   * be edited
   * @param step - the integration step object that should be edited
   */
  // tslint:disable-next-line:react-unused-props-and-state
  configureStepHref(stepIdx: number, step: Step): H.LocationDescriptor;
}

/**
 * A component to render the steps of an integration with the required action
 * buttons to add a new step, edit an existing one, etc.
 *
 * @see [steps]{@link IIntegrationEditorStepAdderProps#steps}
 * @see [addConnectionHref]{@link IIntegrationEditorStepAdderProps#addConnectionHref}
 * @see [addStepHref]{@link IIntegrationEditorStepAdderProps#addStepHref}
 * @see [configureConnectionHref]{@link IIntegrationEditorStepAdderProps#configureConnectionHref}
 * @see [configureStepHref]{@link IIntegrationEditorStepAdderProps#configureStepHref}
 *
 * @todo add the delete step button
 */
export class IntegrationEditorStepAdder extends React.Component<
  IIntegrationEditorStepAdderProps
> {
  public render() {
    return (
      <IntegrationEditorStepsList>
        {this.props.steps.map((s, idx) => {
          return (
            <React.Fragment key={idx}>
              <IntegrationEditorStepsListItem
                stepName={s.connection!.connector!.name}
                stepDescription={s.action!.name}
                icon={<img src={s.connection!.icon} width={24} height={24} />}
                actions={
                  <>
                    <ButtonLink
                      href={this.props.configureConnectionHref(idx, s)}
                    >
                      Configure
                    </ButtonLink>
                    <ButtonLink href={'#'} as={'danger'}>
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
                  i18nAddStep={'Add a step'}
                  addConnectionHref={this.props.addConnectionHref(idx + 1)}
                  i18nAddConnection={'Add a connection'}
                />
              )}
            </React.Fragment>
          );
        })}
      </IntegrationEditorStepsList>
    );
  }
}
