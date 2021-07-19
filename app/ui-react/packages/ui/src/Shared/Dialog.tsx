import { Modal } from '@patternfly/react-core';
import * as React from 'react';

export interface IDialogProps {
  body: any;
  footer: any;
  onHide: () => void;
  title: string;
}

export const Dialog: React.FunctionComponent<IDialogProps> = ({
  body,
  footer,
  onHide,
  title,
}) => (
  <Modal
    width={'50%'}
    title={title}
    footer={footer}
    isOpen={true}
    onClose={onHide}
  >
    {body}
  </Modal>
);
