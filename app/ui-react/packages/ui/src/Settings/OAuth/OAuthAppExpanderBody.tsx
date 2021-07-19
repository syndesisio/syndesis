import { Alert, Button, Form, Stack, StackItem } from '@patternfly/react-core';
import * as React from 'react';

export interface IOAuthAppExpanderBodyProps {
  showSuccess: boolean;
  disableSave: boolean;
  disableRemove: boolean;
  onSave: () => void;
  onRemove: () => void;
  children: React.ReactNode;
  i18nRemoveButtonText: string;
  i18nSaveButtonText: string;
  i18nAlertTitle: string;
  i18nAlertDetail: string;
}

export const OAuthAppExpanderBody: React.FC<IOAuthAppExpanderBodyProps> = ({
  children,
  disableRemove,
  disableSave,
  i18nAlertDetail,
  i18nAlertTitle,
  i18nRemoveButtonText,
  i18nSaveButtonText,
  onRemove,
  onSave,
  showSuccess,
}) => {
  return (
    <Stack hasGutter={true}>
      {showSuccess && (
        <StackItem>
          <Alert variant={'success'} title={i18nAlertTitle}>
            {i18nAlertDetail}
          </Alert>
        </StackItem>
      )}
      <StackItem>
        <Form isHorizontal={true}>{children}</Form>
      </StackItem>
      <StackItem>
        <div>
          <Button
            data-testid={'o-auth-app-expander-body-save-button'}
            variant={'primary'}
            onClick={onSave}
            disabled={disableSave}
          >
            {i18nSaveButtonText}
          </Button>{' '}
          <Button
            data-testid={'o-auth-app-expander-body-remove-button'}
            variant={'link'}
            onClick={onRemove}
            disabled={disableRemove}
          >
            {i18nRemoveButtonText}
          </Button>
        </div>
      </StackItem>
    </Stack>
  );
};
