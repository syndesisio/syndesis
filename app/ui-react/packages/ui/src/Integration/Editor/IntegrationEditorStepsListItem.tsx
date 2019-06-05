import { ListView, Overlay, Popover } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';

import './IntegrationEditorStepsListItem.css';

export interface IIntegrationEditorStepsListItemProps {
  action: string;
  children?: React.ReactNode;
  stepName: string;
  stepDescription: string;
  shape: string;
  showWarning: boolean;
  i18nWarningTitle: React.ReactNode;
  i18nWarningMessage: React.ReactNode;
  actions: any;
  icon: React.ReactNode;
}

export const IntegrationEditorStepsListItem: React.FunctionComponent<
  IIntegrationEditorStepsListItemProps
> = props => {
  const [showWarningPopover, setShowWarningPopover] = React.useState(false);
  const toggleWarningPopover = () => setShowWarningPopover(!showWarningPopover);
  const itemRef = React.useRef(null);

  return (
    <ListView.Item
      data-testid={`integration-editor-steps-list-item-${toValidHtmlId(
        props.stepName
      )}-list-item`}
      actions={props.actions}
      heading={props.stepName}
      children={props.children}
      className={'integration-editor-steps-list-item__list-item'}
      initExpanded={typeof props.children !== 'undefined'}
      description={props.stepDescription}
      additionalInfo={[
        <React.Fragment key={0}>
          <div>
            <p>
              <strong>Action:</strong>&nbsp;
              <span>{props.action}</span>
            </p>
            <p>
              <strong>Data Type:</strong>&nbsp;
              <span>
                {props.shape}
                {props.showWarning && (
                  <ButtonLink
                    data-testid={
                      'integration-editor-steps-list-item-warning-button'
                    }
                    as={'link'}
                    onClick={toggleWarningPopover}
                    ref={itemRef}
                  >
                    <i className={'pficon pficon-warning-triangle-o'} />
                  </ButtonLink>
                )}
              </span>
              <Overlay
                placement={'top'}
                show={showWarningPopover}
                target={itemRef.current}
              >
                <Popover
                  id={'iedsli-shape-warning'}
                  title={props.i18nWarningTitle}
                >
                  {props.i18nWarningMessage}
                </Popover>
              </Overlay>
            </p>
          </div>
        </React.Fragment>,
      ]}
      leftContent={props.icon}
      stacked={true}
      hideCloseIcon={true}
    />
  );
};
