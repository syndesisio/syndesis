import { Button, Modal } from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, Loader } from '../../../../Layout';

export interface IEditPoliciesModalProps {
  i18nCancel: string;
  i18nSave: string;
  i18nTitle: string;
  isOpen: boolean;
  isUpdating: boolean;
  onClose: () => void;
  onSetPolicies: () => void;
}

export const EditPoliciesModal: React.FunctionComponent<IEditPoliciesModalProps> = ({
  i18nCancel,
  i18nSave,
  i18nTitle,
  isOpen,
  isUpdating,
  onClose,
  onSetPolicies,
}) => (
  <Modal
    width={'50%'}
    title={i18nTitle}
    isOpen={isOpen}
    onClose={onClose}
    actions={[
      <ButtonLink
        key="confirm"
        as={'primary'}
        onClick={onSetPolicies}
        isDisabled={isUpdating}
      >
        {isUpdating ? <Loader size={'xs'} inline={true} /> : null}
        {i18nSave}
      </ButtonLink>,
      <Button key="cancel" variant="link" onClick={onClose}>
        {i18nCancel}
      </Button>,
    ]}
    isFooterLeftAligned={true}
  >
    {' '}
    <div>
      <h3>'COMING SOON!'</h3>
    </div>
  </Modal>
);
