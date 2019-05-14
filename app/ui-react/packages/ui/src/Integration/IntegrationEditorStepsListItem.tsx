import {
  ListView,
  Overlay,
  OverlayTrigger,
  Tooltip,
  Popover,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../Layout';

export interface IIntegrationEditorStepsListItemProps {
  stepName: string;
  stepDescription: string;
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
      actions={props.actions}
      heading={props.stepName}
      description={props.stepDescription}
      additionalInfo={[
        <React.Fragment key={0}>
          {props.showWarning && (
            <ButtonLink
              as={'link'}
              onClick={toggleWarningPopover}
              ref={itemRef}
              className={'pull-left'}
            >
              <i className={'pficon pficon-warning-triangle-o'} />
            </ButtonLink>
          )}
          <div
            className={
              'list-view-pf-additional-info-item-stacked list-view-pf-additional-info-item'
            }
          >
            <OverlayTrigger
              placement={'top'}
              overlay={<Tooltip id={'iedsli-ds'}>Data shape</Tooltip>}
            >
              <strong>{props.shape}</strong>
            </OverlayTrigger>

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
