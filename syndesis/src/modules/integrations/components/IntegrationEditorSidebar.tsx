import { Step } from '@syndesis/models';
import {
  IntegrationFlowAddStep,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import * as React from 'react';

export interface IIntegrationEditorSidebarProps {
  disabled?: boolean;
  steps: Step[];
  activeIndex?: number;
  addAtIndex?: number;
  addType?: 'connection' | 'step';
  forceTooltips?: boolean;
  addStepHref?(idx: number): string;
  addConnectionHref?(idx: number): string;
}

export class IntegrationEditorSidebar extends React.Component<
  IIntegrationEditorSidebarProps
> {
  public static defaultProps = {
    disabled: false,
    forceTooltips: false,
  };

  public render() {
    return (
      <IntegrationVerticalFlow disabled={this.props.disabled}>
        {({ expanded }) =>
          this.props.steps.map((s, idx) => {
            const hasAddStep = idx < this.props.steps.length - 1;
            const addStep = (
              <IntegrationFlowAddStep
                forceTooltip={this.props.forceTooltips}
                showDetails={expanded}
                addStepHref={this.props.addStepHref!(idx + 1)}
                i18nAddStep={'Add a step'}
                addConnectionHref={this.props.addConnectionHref!(idx + 1)}
                i18nAddConnection={'Add a connection'}
              />
            );
            const activeAddStep = (
              <IntegrationFlowStepGeneric
                icon={'+'}
                i18nTitle={'1. Start'}
                i18nTooltip={'Start'}
                active={true}
                showDetails={expanded}
                description={'Choose a connection'}
              />
            );
            return (
              <React.Fragment key={idx}>
                <IntegrationFlowStepWithOverview
                  icon={<img src={s.connection!.icon} width={24} height={24} />}
                  i18nTitle={`${idx + 1}. ${s.action!.name}`}
                  i18nTooltip={`${idx + 1}. ${s.action!.name}`}
                  active={false}
                  showDetails={expanded}
                  name={s.connection!.connector!.name}
                  action={s.action!.name}
                  dataType={'TODO'}
                />
                {hasAddStep &&
                  (this.props.addAtIndex === idx ? activeAddStep : addStep)}
              </React.Fragment>
            );
          })
        }
      </IntegrationVerticalFlow>
    );
  }
}
