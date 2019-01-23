import * as React from 'react';

export interface IIntegrationCreatorBreadcrumbsProps {
  step: number;
  subStep?: number;
}

export interface IIntegrationCreatorBreadcrumbsState {
  active: boolean;
}

interface IWizardStepProps {
  isActive: boolean;
  isAlt?: boolean;
  onClick?: () => any;
  step: number;
  title: string;
  subSteps?: string[];
  activeSubstep?: number;
}
const WizardStep: React.FunctionComponent<IWizardStepProps> = ({
  isActive,
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
    }`}
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

export class IntegrationCreatorBreadcrumbs extends React.Component<
  IIntegrationCreatorBreadcrumbsProps,
  IIntegrationCreatorBreadcrumbsState
> {
  public state = {
    active: false,
  };

  constructor(props: IIntegrationCreatorBreadcrumbsProps) {
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
            onClick={this.toggleActive}
            step={1}
            title={'Start connection'}
            subSteps={[
              'Choose connection',
              'Choose action',
              'Configure action',
            ]}
            activeSubstep={this.props.subStep}
          />
          <WizardStep
            isActive={this.props.step === 2}
            onClick={this.toggleActive}
            step={2}
            title={'Finish connection'}
            subSteps={[
              'Choose connection',
              'Choose action',
              'Configure action',
            ]}
            activeSubstep={this.props.subStep}
          />
          <WizardStep
            isActive={this.props.step === 3}
            onClick={this.toggleActive}
            step={3}
            title={'Add to integration'}
          />
          <WizardStep
            isActive={this.props.step === 4}
            onClick={this.toggleActive}
            step={4}
            title={'Save and publish'}
          />
        </ul>
        <ul
          className={`wizard-pf-steps-alt ${this.state.active ? '' : 'hidden'}`}
        >
          <WizardStep
            isActive={this.props.step === 1}
            isAlt={true}
            onClick={this.toggleActive}
            step={1}
            title={'Start connection'}
            subSteps={[
              '1A. Choose connection',
              '1B. Choose action',
              '1C. Configure action',
            ]}
            activeSubstep={this.props.subStep}
          />
          <WizardStep
            isActive={this.props.step === 2}
            isAlt={true}
            onClick={this.toggleActive}
            step={2}
            title={'Finish connection'}
            subSteps={[
              '2A. Choose connection',
              '2B. Choose action',
              '2C. Configure action',
            ]}
            activeSubstep={this.props.subStep}
          />
          <WizardStep
            isActive={this.props.step === 3}
            isAlt={true}
            onClick={this.toggleActive}
            step={3}
            title={'Add to integration'}
          />
          <WizardStep
            isActive={this.props.step === 4}
            isAlt={true}
            onClick={this.toggleActive}
            step={4}
            title={'Save and publish'}
          />
        </ul>
      </div>
    );
  }
}
