import { ListView, Overlay, Popover } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';

export interface IIntegrationEditorStepsListItemProps {
  stepName: string;
  stepDescription: string;
  action: string;
  shape: string;
  showWarning: boolean;
  i18nWarningTitle: React.ReactNode;
  i18nWarningMessage: React.ReactNode;
  actions: any;
  icon: string;
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
      leftContent={
        <img alt={props.stepName} src={props.icon} width={24} height={24} />
      }
      stacked={true}
      hideCloseIcon={true}
    />
  );
};
