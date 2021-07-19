import { Button, Modal } from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';

export const _3SCALE = '_3SCALE';
export const ROUTE = 'ROUTE';

export interface IIntegrationExposeViaProps {
  exposure: string;
  exposureMeans: string[];
  isUnpublished: boolean;
  isPending: boolean;
  i18nDisableDiscovery: string;
  i18nDisableDiscoveryConfirm: string;
  i18nDiscoveryDescription: string;
  i18nEnableDiscovery: string;
  i18nEnableDiscoveryConfirm: string;
  i18nNo3ScaleConfigured: string;
  i18nRepublish: string;
  i18nYes: string;
  i18nNo: string;
  onChange: (exposure: string) => void;
}

export const IntegrationExposeVia: React.FunctionComponent<IIntegrationExposeViaProps> =
  ({
    exposure,
    exposureMeans,
    isUnpublished,
    isPending,
    i18nDisableDiscovery,
    i18nDisableDiscoveryConfirm,
    i18nDiscoveryDescription,
    i18nEnableDiscovery,
    i18nEnableDiscoveryConfirm,
    i18nNo3ScaleConfigured,
    i18nRepublish,
    i18nYes,
    i18nNo,
    onChange,
  }) => {
    const [showDialog, setShowDialog] = React.useState(false);
    const doShowDialog = () => {
      setShowDialog(true);
    };
    const doEnable3scale = () => {
      onChange(_3SCALE);
      doHideDialog();
    };
    const doDisable3scale = () => {
      onChange(ROUTE);
      doHideDialog();
    };
    const doHideDialog = () => {
      setShowDialog(false);
    };
    const disableDiscoveryDialog = (
      <Modal
        variant={'small'}
        title={i18nDisableDiscovery}
        isOpen={showDialog}
        onClose={doHideDialog}
        actions={[
          <Button key="confirm" variant="primary" onClick={doDisable3scale}>
            {i18nYes}
          </Button>,
          <Button key="cancel" variant="link" onClick={doHideDialog}>
            {i18nNo}
          </Button>,
        ]}
      >
        <p className={'lead'}>
          {i18nDisableDiscoveryConfirm}
          {isUnpublished ? null : <> {i18nRepublish}</>}?
        </p>
      </Modal>
    );

    if (exposureMeans.indexOf(_3SCALE) !== -1) {
      return (
        <>
          {exposure !== _3SCALE ? (
            <PageSection>
              <div className={'pf-c-content'}>
                <Modal
                  variant={'small'}
                  title={i18nEnableDiscovery}
                  isOpen={showDialog}
                  onClose={doHideDialog}
                  actions={[
                    <Button
                      key="confirm"
                      variant="primary"
                      onClick={doEnable3scale}
                    >
                      {i18nYes}
                    </Button>,
                    <Button key="cancel" variant="link" onClick={doHideDialog}>
                      {i18nNo}
                    </Button>,
                  ]}
                >
                  <p className={'lead'}>
                    {i18nEnableDiscoveryConfirm}
                    {isUnpublished ? null : <> {i18nRepublish}</>}?
                  </p>
                </Modal>
                <div className={'pf-l-split pf-m-gutter'}>
                  <div>
                    <ButtonLink
                      children={i18nEnableDiscovery}
                      onClick={doShowDialog}
                      disabled={isPending}
                    />
                  </div>
                  <div>{i18nDiscoveryDescription}</div>
                </div>
              </div>
            </PageSection>
          ) : (
            <>
              <PageSection>
                <div className={'pf-c-content'}>
                  {disableDiscoveryDialog}
                  <div className={'pf-l-split pf-m-gutter'}>
                    <div>
                      <ButtonLink
                        children={i18nDisableDiscovery}
                        onClick={doShowDialog}
                        disabled={isPending}
                      />
                    </div>
                    <div>{i18nDiscoveryDescription}</div>
                  </div>
                </div>
              </PageSection>
            </>
          )}
        </>
      );
    } else {
      return exposure === _3SCALE ? (
        <PageSection>
          <div className={'pf-c-content'}>
            {disableDiscoveryDialog}
            <div className={'pf-l-split pf-m-gutter'}>
              <div>
                <ButtonLink
                  children={i18nDisableDiscovery}
                  onClick={doShowDialog}
                  disabled={isPending}
                />
              </div>
              <div>{i18nNo3ScaleConfigured}</div>
            </div>
          </div>
        </PageSection>
      ) : null;
    }
  };
