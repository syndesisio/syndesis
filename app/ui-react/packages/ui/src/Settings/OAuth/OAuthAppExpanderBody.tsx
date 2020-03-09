import { Form } from '@patternfly/react-core';
import { Alert, Button, Col, Row } from 'patternfly-react';
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

export const OAuthAppExpanderBody: React.FC<
  IOAuthAppExpanderBodyProps
> = (
  {
    children,
    disableRemove,
    disableSave,
    i18nAlertDetail,
    i18nAlertTitle,
    i18nRemoveButtonText,
    i18nSaveButtonText,
    onRemove,
    onSave,
    showSuccess
  }) => {
  return (
    <>
      {showSuccess && (
        <Row>
          <Col xs={11}>
            <Alert type={'success'}>
              <strong>{i18nAlertTitle}</strong>&nbsp;
              {i18nAlertDetail}
            </Alert>
          </Col>
        </Row>
      )}
      <Row>
        <Col xs={12} md={8}>
          <Form isHorizontal={true}>{children}</Form>
        </Col>
      </Row>
      <Row>
        <Col xs={12} md={8}>
          <>
            <Button
              data-testid={'o-auth-app-expander-body-save-button'}
              bsStyle="primary"
              onClick={onSave}
              disabled={disableSave}
            >
              {i18nSaveButtonText}
            </Button>{' '}
            <Button
              data-testid={'o-auth-app-expander-body-remove-button'}
              onClick={onRemove}
              disabled={disableRemove}
            >
              {i18nRemoveButtonText}
            </Button>
          </>
        </Col>
      </Row>
    </>
  );
};
