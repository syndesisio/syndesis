import { WizardStep, WizardSteps } from '@syndesis/ui';
import * as React from 'react';

export interface IIntegrationEditorBreadcrumbsProps {
  /**
   * The one-based active step number.
   */
  step: number;
}

export interface IIntegrationEditorBreadcrumbsState {
  /**
   * Indicates if the user clicked on a step. Used to show
   * sub-steps when browsing from a mobile device.
   */
  active: boolean;
}

/**
 * A component to display the PatternFly Wizard Steps specific to the integration
 * editor flow.
 * @see [step]{@link IIntegrationEditorBreadcrumbsProps#step}
 */
export class IntegrationEditorBreadcrumbs extends React.Component<
  IIntegrationEditorBreadcrumbsProps,
  IIntegrationEditorBreadcrumbsState
> {
  public state = {
    active: false,
  };

  constructor(props: IIntegrationEditorBreadcrumbsProps) {
    super(props);
    this.toggleActive = this.toggleActive.bind(this);
  }

  public toggleActive() {
    this.setState({
      active: !this.state.active,
    });
  }

  public render() {
    return (
      <WizardSteps
        active={this.state.active}
        mainSteps={
          <>
            <WizardStep
              isActive={this.props.step === 1}
              isDisabled={this.props.step < 1}
              onClick={this.toggleActive}
              step={1}
              title={'Add to integration'}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              onClick={this.toggleActive}
              step={2}
              title={'Save the integration'}
            />
          </>
        }
        altSteps={
          <>
            <WizardStep
              isActive={this.props.step === 1}
              isDisabled={this.props.step < 1}
              isAlt={true}
              onClick={this.toggleActive}
              step={1}
              title={'Add to integration'}
            />
            <WizardStep
              isActive={this.props.step === 2}
              isDisabled={this.props.step < 2}
              isAlt={true}
              onClick={this.toggleActive}
              step={4}
              title={'Save the integration'}
            />
          </>
        }
      />
    );
  }
}
