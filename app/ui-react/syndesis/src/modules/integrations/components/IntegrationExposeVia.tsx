import { IntegrationOverview } from "@syndesis/models";
import { ButtonLink, PageSection } from '@syndesis/ui';
import { MessageDialog } from 'patternfly-react';
import * as React from 'react';
import { useState } from "react";
import { useTranslation } from "react-i18next"

export interface IIntegrationExposeViaProps {
  integration: IntegrationOverview;
  onChange: (exposure: string) => void;
};

export const IntegrationExposeVia: React.FunctionComponent<
  IIntegrationExposeViaProps
> = ({ integration, onChange }) => {
  const [showDialog, setShowDialog] = useState(false);

  const { t } = useTranslation(['integrations', 'shared']);

  const exposureMeans = integration.exposureMeans ? integration.exposureMeans : [];
  const unpublished = integration.currentState === 'Unpublished';
  const pending = integration.currentState === 'Pending';

  const doShowDialog = () => {
    setShowDialog(true);
  };

  const doEnable3scale = () => {
    onChange('_3SCALE');
    doHideDialog();
  };

  const doDisable3scale = () => {
    onChange('ROUTE');
    doHideDialog();
  };

  const doHideDialog = () => {
    setShowDialog(false);
  };

  const disableDiscoveryDialog = (<MessageDialog
    show={showDialog}
    title={t('integrations:exposeVia:disableDiscovery')} primaryContent={
    <p className="lead">{t('integrations:exposeVia:disableDiscoveryConfirm')}{unpublished ? (null) : (<> {t('integrations:exposeVia:republish')}</>)}?</p>
  }
    primaryActionButtonContent={t('shared:Yes')}
    primaryAction={doDisable3scale}
    secondaryActionButtonContent={t('shared:No')}
    secondaryAction={doHideDialog}
    onHide={doHideDialog}
    onCancel={doHideDialog}
  />);

  if (exposureMeans.indexOf('_3SCALE') !== -1) {
    return (
      <>
        {integration.exposure && integration.exposure !== '_3SCALE' ? (
          <PageSection>
            <div className="pf-c-content">
              <MessageDialog
                show={showDialog}
                title={t('integrations:exposeVia:enableDiscovery')} primaryContent={
                  <p className="lead">{t('integrations:exposeVia:enableDiscoveryConfirm')}{unpublished ? (null) : (<> {t('integrations:exposeVia:republish')}</>)}?</p>
                }
                primaryActionButtonContent={t('shared:Yes')}
                primaryAction={doEnable3scale}
                secondaryActionButtonContent={t('shared:No')}
                secondaryAction={doHideDialog}
                onHide={doHideDialog}
                onCancel={doHideDialog}
              />
              <div className="pf-l-split pf-m-gutter">
                <div><ButtonLink children={t('integrations:exposeVia:enableDiscovery')} onClick={doShowDialog} disabled={pending} /></div>
                <div>{t('integrations:exposeVia:discoveryDescription')}</div>
              </div>
            </div>
          </PageSection>
        ) : (
            <>
              <PageSection>
                <div className="pf-c-content">
                  {disableDiscoveryDialog}
                  <div className="pf-l-split pf-m-gutter">
                    <div><ButtonLink children={t('integrations:exposeVia:disableDiscovery')} onClick={doShowDialog} disabled={pending} /></div>
                    <div>{t('integrations:exposeVia:discoveryDescription')}</div>
                  </div>
                </div>
              </PageSection>
            </>
          )}
      </>
    );
  } else {
    return (integration.exposure && integration.exposure === '_3SCALE') ? (
      <PageSection>
        <div className="pf-c-content">
          {disableDiscoveryDialog}
          <div className="pf-l-split pf-m-gutter">
            <div><ButtonLink children={t('integrations:exposeVia:disableDiscovery')} onClick={doShowDialog} disabled={pending} /></div>
            <div>{t('integrations:exposeVia:no3scaleConfigured')}</div>
          </div>
        </div>
      </PageSection>
    ) : null;
  }
};
