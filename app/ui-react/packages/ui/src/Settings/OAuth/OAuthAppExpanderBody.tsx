import { Alert, Button, Form, Grid, GridItem } from '@patternfly/react-core';
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
        <Grid sm={11}>
          <GridItem>
            <Alert variant={'success'}
                   title={i18nAlertTitle}>
              {i18nAlertDetail}
            </Alert>
          </GridItem>
        </Grid>
      )}
      <Grid sm={12} md={8}>
        <GridItem>
          <Form isHorizontal={true}>{children}</Form>
        </GridItem>
      </Grid>
      <Grid sm={12} md={8}>
        <GridItem>
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
              onClick={onRemove}
              disabled={disableRemove}
            >
              {i18nRemoveButtonText}
            </Button>
          </div>
        </GridItem>
      </Grid>
    </>
  );
};
