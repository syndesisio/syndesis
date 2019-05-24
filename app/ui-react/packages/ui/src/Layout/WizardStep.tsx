// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
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

export const WizardStep: React.FunctionComponent<IWizardStepProps> = ({
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
