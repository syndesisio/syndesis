import { Step } from '@syndesis/models';
import {
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import * as React from 'react';
import { IUIStep } from './interfaces';
import { getDataShapeText, toUIStepCollection } from './utils';

function makeActiveStep(
  position: number,
  expanded: boolean,
  activeStep?: IUIStep
) {
  return activeStep ? (
    <IntegrationFlowStepWithOverview
      icon={
        <img
          alt={activeStep.name}
          src={activeStep.icon}
          width={24}
          height={24}
        />
      }
      i18nTitle={`${position}. ${activeStep.name}`}
      i18nTooltip={`${position}. ${activeStep.title}`}
      active={true}
      showDetails={expanded}
      name={
        activeStep.action ? activeStep.action.name : 'Select/configure action'
      }
      action={activeStep.action && activeStep.action.name}
      dataType={getDataShapeText(
        activeStep.stepKind!,
        activeStep.outputDataShape
      )}
    />
  ) : (
    <IntegrationFlowStepGeneric
      icon={<i className={'fa fa-plus'} />}
      i18nTitle={`${position}. Finish`}
      i18nTooltip={'Finish'}
      active={true}
      showDetails={expanded}
      description={'Choose a connection'}
    />
  );
}

export interface IEditorSidebarProps {
  steps: Step[];
  activeStep?: IUIStep;
  activeIndex: number;
}
export const EditorSidebar: React.FunctionComponent<IEditorSidebarProps> = ({
  activeIndex,
  activeStep,
  steps,
}) => {
  const UISteps = toUIStepCollection(steps);
  return (
    <IntegrationVerticalFlow>
      {({ expanded }) => {
        if (UISteps.length === 0) {
          return (
            <>
              {makeActiveStep(1, expanded, activeStep)}
              <IntegrationFlowStepGeneric
                icon={<i className={'fa fa-plus'} />}
                i18nTitle={'2. Finish'}
                i18nTooltip={'Finish'}
                active={false}
                showDetails={expanded}
                description={'Choose a connection'}
              />
            </>
          );
        } else if (UISteps.length === 1) {
          const startStep = UISteps[0];
          return (
            <>
              <IntegrationFlowStepWithOverview
                icon={
                  <img
                    alt={'Step'}
                    src={startStep.icon}
                    width={24}
                    height={24}
                  />
                }
                i18nTitle={`1. ${startStep.name}`}
                i18nTooltip={`1. ${startStep.title}`}
                showDetails={expanded}
                name={startStep.name}
                action={startStep.action && startStep.action.name!}
                dataType={getDataShapeText(
                  startStep.stepKind!,
                  startStep.outputDataShape
                )}
              />
              {makeActiveStep(2, expanded, activeStep)}
            </>
          );
        } else {
          return (
            <>
              {UISteps.map((s, idx) => {
                const isActive = idx === activeIndex;
                const hasAddStep = activeIndex === idx + 1;
                const isAfterActiveAddStep = activeIndex - 1 < idx;
                const position = isAfterActiveAddStep ? idx + 2 : idx + 1;

                return (
                  <React.Fragment key={idx}>
                    <IntegrationFlowStepWithOverview
                      icon={
                        <img alt={s.name} src={s.icon} width={24} height={24} />
                      }
                      i18nTitle={`${position}. ${s.name}`}
                      i18nTooltip={`${position}. ${s.title}`}
                      active={isActive && !isAfterActiveAddStep}
                      showDetails={expanded}
                      name={s.name}
                      action={s.action && s.action.name!}
                      dataType={
                        idx === 0
                          ? getDataShapeText(s.stepKind!, s.outputDataShape)
                          : getDataShapeText(s.stepKind!, s.inputDataShape)
                      }
                    />
                    {hasAddStep
                      ? makeActiveStep(position, expanded, activeStep)
                      : null}
                  </React.Fragment>
                );
              })}
            </>
          );
        }
      }}
    </IntegrationVerticalFlow>
  );
};
