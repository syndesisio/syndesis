import { Button, Modal, Tab, Tabs } from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, Loader } from '../../../../Layout';

export interface IEditPoliciesModalProps {
  i18nCancel: string;
  i18nColumnMasking: string;
  i18nColumnPermissions: string;
  i18nRowBasedFiltering: string;
  i18nSave: string;
  i18nTitle: string;
  isOpen: boolean;
  isUpdating: boolean;
  onClose: () => void;
  onSetPolicies: () => void;
}

export const EditPoliciesModal: React.FunctionComponent<IEditPoliciesModalProps> = ({
  i18nCancel,
  i18nColumnMasking,
  i18nColumnPermissions,
  i18nRowBasedFiltering,
  i18nSave,
  i18nTitle,
  isOpen,
  isUpdating,
  onClose,
  onSetPolicies,
}) => {

  const [activeTabKey, setActiveTabKey] = React.useState<React.ReactText>();

  const handleTabClick = (event: any, tabIndex: React.ReactText) => {
    setActiveTabKey(tabIndex);
  };

  return (
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
      <div>
        <Tabs
          activeKey={activeTabKey}
          onSelect={handleTabClick}
        >
          <Tab
            eventKey={0}
            title={i18nRowBasedFiltering}
          >
            Row-based filtering - coming soon!
          </Tab>
          <Tab 
            eventKey={1} 
            title={i18nColumnMasking}
          >
            Column masking - coming soon!
          </Tab>
          <Tab
            eventKey={2}
            title={i18nColumnPermissions}
          >
            Column permissions - coming soon!
          </Tab>
        </Tabs>
      </div>
    </Modal>
  );
};