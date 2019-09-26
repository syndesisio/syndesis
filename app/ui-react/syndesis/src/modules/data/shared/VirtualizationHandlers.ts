import { useVirtualizationHelpers } from '@syndesis/api';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';

export const VirtualizationHandlers = () => {
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const {
    deleteVirtualization,
    publishVirtualization,
    unpublishVirtualization,
  } = useVirtualizationHelpers();

  const handleDeleteVirtualization = async (
    pVirtualizationId: string
  ): Promise<boolean> => {
    let success = false;
    try {
      await deleteVirtualization(pVirtualizationId);
      pushNotification(
        t('virtualization.deleteVirtualizationSuccess', {
          name: pVirtualizationId,
        }),
        'success'
      );
      success = true;
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('virtualization.deleteVirtualizationFailed', {
          details,
          name: pVirtualizationId,
        }),
        'error'
      );
    }
    return Promise.resolve(success);
  };

  const handlePublishVirtualization = async (
    virtualizationId: string,
    hasViews: boolean
  ): Promise<boolean> => {
    let success = false;
    if (hasViews) {
      try {
        const status = await publishVirtualization(virtualizationId);

        if (status.Information.error) {
          pushNotification(
            t('virtualization.publishVirtualizationFailed', {
              details: status.Information.error,
              name: virtualizationId,
            }),
            'error'
          );
        } else {
          pushNotification(
            t('virtualization.publishVirtualizationSuccess', {
              name: virtualizationId,
            }),
            'success'
          );
          success = true;
        }
      } catch (error) {
        const details = error.error ? error.error : '';
        pushNotification(
          t('virtualization.publishVirtualizationFailed', {
            details,
            name: virtualizationId,
          }),
          'error'
        );
      }
    } else {
      pushNotification(
        t('virtualization.publishVirtualizationNoViews', {
          name: virtualizationId,
        }),
        'error'
      );
    }
    return Promise.resolve(success);
  };

  const handleUnpublishVirtualization = async (
    virtualizationName: string
  ): Promise<boolean> => {
    let success = false;
    try {
      const buildStatus = await unpublishVirtualization(virtualizationName);

      if (buildStatus.build_status === 'NOTFOUND') {
        pushNotification(
          t('virtualization.unpublishedVirtualization', {
            name: virtualizationName,
          }),
          'info'
        );
      } else if (buildStatus.build_status !== 'DELETE_SUBMITTED') {
        pushNotification(
          t('virtualization.unpublishVirtualizationFailed', {
            details: buildStatus.build_status_message,
            name: virtualizationName,
          }),
          'error'
        );
      } else {
        pushNotification(
          t('virtualization.unpublishVirtualizationSuccess', {
            name: virtualizationName,
          }),
          'success'
        );
        success = true;
      }
    } catch (error) {
      const details = error.message ? error.message : '';
      pushNotification(
        t('virtualization.unpublishVirtualizationFailed', {
          details,
          name: virtualizationName,
        }),
        'error'
      );
    }
    return success;
  };

  return {
    handleDeleteVirtualization,
    handlePublishVirtualization,
    handleUnpublishVirtualization,
  };
};
