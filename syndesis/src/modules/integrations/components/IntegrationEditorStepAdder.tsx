import { Step } from '@syndesis/models';
import { IntegrationFlowAddStep } from '@syndesis/ui';
import * as H from 'history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

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
      <ListView style={{ background: 'transparent' }}>
        {this.props.steps.map((s, idx) => {
          return (
            <React.Fragment key={idx}>
              <ListView.Item
                heading={s.connection!.connector!.name}
                description={s.action!.name}
                hideCloseIcon={true}
                leftContent={
                  <img src={s.connection!.icon} width={24} height={24} />
                }
                stacked={true}
                actions={
                  <>
                    <Link
                      to={this.props.configureConnectionHref(idx, s)}
                      className={'btn btn-default'}
                    >
                      Configure
                    </Link>
                    <Link to={'#'} className={'btn btn-danger'}>
                      <i className="fa fa-trash" />
                    </Link>
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
      </ListView>
    );
  }
}
