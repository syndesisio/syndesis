import * as React from 'react';

interface IWizardStepProps {
  isActive: boolean;
  isDisabled: boolean;
  isAlt?: boolean;
  onClick?: () => any;
  step: number;
  title: string;
  subSteps?: string[];
  activeSubstep?: number;
}
const WizardStep: React.FunctionComponent<IWizardStepProps> = ({
  isActive,
  isDisabled,
  isAlt = false,
  onClick,
  step,
  title,
  subSteps = [],
  activeSubstep = 0,
}) => (
  <li
    className={`${isAlt ? 'wizard-pf-step-alt' : 'wizard-pf-step'} ${
      isActive ? 'active' : ''
    } ${isDisabled ? 'disabled' : ''}`}
    onClick={onClick}
  >
    <a>
      <span className={`wizard-pf-step${isAlt ? '-alt' : ''}-number`}>
        {step}
      </span>
      <span className={`wizard-pf-step${isAlt ? '-alt' : ''}-title`}>
        {title}
      </span>
      {!isAlt &&
        subSteps.map((s, idx) => (
          <span
            className={`wizard-pf-step-title-substep ${
              idx === activeSubstep ? 'active' : ''
            }`}
            key={idx}
          >
            {s}
          </span>
        ))}
    </a>
    {isAlt && isActive && (
      <ul>
        {subSteps.map((s, idx) => (
          <li
            className={`wizard-pf-step-alt-substep ${
              idx === activeSubstep ? 'active' : 'disabled'
            }`}
            key={idx}
          >
            <a>{s}</a>
          </li>
        ))}
      </ul>
    )}
  </li>
);

export interface IIntegrationEditorBreadcrumbsProps {
  /**
   * The one-based active step number.
   */
  step: number;
  /**
   * Optional. The one-based active sub-step number.
   */
  subStep?: number;
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
 * @see [subStep]{@link IIntegrationEditorBreadcrumbsProps#subStep}
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
      <div className={'wizard-pf-steps'}>
        <ul
          className={`wizard-pf-steps-indicator wizard-pf-steps-alt-indicator ${
            this.state.active ? 'active' : ''
          }`}
        >
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
        </ul>
        <ul
          className={`wizard-pf-steps-alt ${this.state.active ? '' : 'hidden'}`}
        >
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
        </ul>
      </div>
    );
  }
}
