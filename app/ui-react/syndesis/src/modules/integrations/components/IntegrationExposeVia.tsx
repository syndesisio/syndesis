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

  const doManageIn3scale = () => {
    window.location.href = integration.managementUrl!;
  };

  const doHideDialog = () => {
    setShowDialog(false);
  };

  const exposureMeans = integration.exposureMeans ? integration.exposureMeans : [];
  const unpublished = integration.currentState === 'Unpublished';
  const pending = integration.currentState === 'Pending';
  if (exposureMeans.indexOf('_3SCALE') !== -1) {
    return (
      <>
        {integration.exposure && integration.exposure !== '_3SCALE' ? (
          <PageSection>
            <div className="pf-c-content">
              <MessageDialog
                show={showDialog}
                title={t('integrations:exposeVia:3scale')} primaryContent={
                  <p className="lead">{t('integrations:exposeVia:3scaleConfirm')}{unpublished ? (null) : (<> {t('integrations:exposeVia:republish')}</>)}?</p>
                }
                primaryActionButtonContent={t('shared:Yes')}
                primaryAction={doEnable3scale}
                secondaryActionButtonContent={t('shared:No')}
                secondaryAction={doHideDialog}
                onHide={doHideDialog}
                onCancel={doHideDialog}
              />
              <div className="pf-l-split pf-m-gutter">
                <div><ButtonLink children={t('integrations:exposeVia:3scale')} onClick={doShowDialog} disabled={pending} /></div>
                <div>{t('integrations:exposeVia:3scaleDescription')}</div>
              </div>
            </div>
          </PageSection>
        ) : (
            <>
              <PageSection>
                <div className="pf-c-content">
                  <div className="pf-l-split pf-m-gutter">
                    <div><ButtonLink children={t('integrations:exposeVia:manageIn3scale')} onClick={doManageIn3scale} disabled={pending || unpublished} /></div>
                    {(pending || unpublished) ? (
                      <div>{t('integrations:exposeVia:manageIn3scalePendingDescription')}</div>
                    ) : (
                        <div>{t('integrations:exposeVia:manageIn3scaleDescription')}</div>
                      )
                    }
                  </div>
                </div>
              </PageSection>
              <PageSection>
                <div className="pf-c-content">
                  <MessageDialog
                    show={showDialog}
                    title={t('integrations:exposeVia:not3scale')} primaryContent={
                      <p className="lead">{t('integrations:exposeVia:not3scaleConfirm')}{unpublished ? (null) : (<> {t('integrations:exposeVia:republish')}</>)}?</p>
                    }
                    primaryActionButtonContent={t('shared:Yes')}
                    primaryAction={doDisable3scale}
                    secondaryActionButtonContent={t('shared:No')}
                    secondaryAction={doHideDialog}
                    onHide={doHideDialog}
                    onCancel={doHideDialog}
                  />
                  <div className="pf-l-split pf-m-gutter">
                    <div><ButtonLink children={t('integrations:exposeVia:not3scale')} onClick={doShowDialog} disabled={pending} /></div>
                    <div>{t('integrations:exposeVia:not3scaleDescription')}</div>
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
          <MessageDialog
            show={showDialog}
            title={t('integrations:exposeVia:not3scale')} primaryContent={
              <p className="lead">{t('integrations:exposeVia:not3scaleConfirm')}{unpublished ? (null) : (<> {t('integrations:exposeVia:republish')}</>)}?</p>
            }
            primaryActionButtonContent={t('shared:Yes')}
            primaryAction={doDisable3scale}
            secondaryActionButtonContent={t('shared:No')}
            secondaryAction={doHideDialog}
            onHide={doHideDialog}
            onCancel={doHideDialog}
          />
          <div className="pf-l-split pf-m-gutter">
            <div><ButtonLink children={t('integrations:exposeVia:not3scale')} onClick={doShowDialog} disabled={pending} /></div>
            <div>{t('integrations:exposeVia:no3scaleConfigured')}</div>
          </div>
        </div>
      </PageSection>
    ) : null;
  }
};
