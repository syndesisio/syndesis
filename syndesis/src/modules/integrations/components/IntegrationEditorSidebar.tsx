import { Step } from '@syndesis/models';
import {
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import * as React from 'react';

export interface IIntegrationEditorSidebarProps {
  /**
   * the list of steps to render.
   */
  steps: Step[];
  /**
   * the zero-based index of a configured step that should be highlighted as
   * active.
   * This should not be set in conjunction with [addAtIndex]{@link IIntegrationEditorSidebarProps#addAtIndex}
   */
  activeIndex?: number;
  /**
   * the zero-based index where a new step is being added.
   * This should not be set in conjunction with [activeIndex]{@link IIntegrationEditorSidebarProps#activeIndex}
   */
  addAtIndex?: number;
  /**
   * indicates if the step that is being added is a 'connection' or 'step'.
   * @todo is it needed?
   */
  addType?: 'connection' | 'step';
  /**
   * the icon to show in the circle of the step that is being added.
   */
  addIcon?: any;
  /**
   * The title of the information table shown for the step that is being added,
   * in the extended view.
   */
  addI18nTitle?: string;
  /**
   * The text to show on the tooltip that opens when hovering with the mouse on
   * the icon.
   */
  addI18nTooltip?: string;
  /**
   * The description of the information table shown for the step that is being
   * added, in the extended view.
   */
  addI18nDescription?: string;
}

/**
 * This component shows the steps of an integration in a vertical fashion. It's
 * meant to be used as the sidebar of the `IntegrationEditorLayout` component.
 * Steps are rendered as circles, showing the step's connection icon.
 *
 * It offers two visualization, a compact one where just the icons are shown,
 * and an expanded one where additional information about the step are shown in
 * a table next to the step's circle.
 *
 * It can also show a step that is being added to the integration by providing
 * its position and some information about the configuration step.
 *
 * @see [steps]{@link IIntegrationEditorSidebarProps#steps}
 * @see [activeIndex]{@link IIntegrationEditorSidebarProps#activeIndex}
 * @see [addAtIndex]{@link IIntegrationEditorSidebarProps#addAtIndex}
 * @see [addType]{@link IIntegrationEditorSidebarProps#addType}
 * @see [addIcon]{@link IIntegrationEditorSidebarProps#addIcon}
 * @see [addI18nTitle]{@link IIntegrationEditorSidebarProps#addI18nTitle}
 * @see [addI18nTooltip]{@link IIntegrationEditorSidebarProps#addI18nTooltip}
 * @see [addI18nDescription]{@link IIntegrationEditorSidebarProps#addI18nDescription}
 */
export class IntegrationEditorSidebar extends React.Component<
  IIntegrationEditorSidebarProps
> {
  public render() {
    return (
      <IntegrationVerticalFlow>
        {({ expanded }) =>
          this.props.steps.map((s, idx) => {
            const isActive = idx === this.props.activeIndex;
            const hasAddStep =
              this.props.addAtIndex && idx < this.props.steps.length - 1;
            const isAfterActiveAddStep = this.props.addAtIndex! - 1 < idx;
            const position = isAfterActiveAddStep ? idx + 2 : idx + 1;

            const activeAddStep = (
              <IntegrationFlowStepGeneric
                icon={this.props.addIcon || <i className={'fa fa-plus'} />}
                i18nTitle={this.props.addI18nTitle!}
                i18nTooltip={this.props.addI18nTooltip!}
                active={true}
                showDetails={expanded}
                description={this.props.addI18nDescription!}
              />
            );
            return (
              <React.Fragment key={idx}>
                {s.stepKind === 'endpoint' && (
                  <IntegrationFlowStepWithOverview
                    icon={
                      <img src={s.connection!.icon} width={24} height={24} />
                    }
                    i18nTitle={`${position}. ${s.action!.name}`}
                    i18nTooltip={`${position}. ${s.action!.name}`}
                    active={isActive}
                    showDetails={expanded}
                    name={s.connection!.connector!.name}
                    action={s.action!.name}
                    dataType={'TODO'}
                  />
                )}
                {hasAddStep ? activeAddStep : null}
              </React.Fragment>
            );
          })
        }
      </IntegrationVerticalFlow>
    );
  }
}
