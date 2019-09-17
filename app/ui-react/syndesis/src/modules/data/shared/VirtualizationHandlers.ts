import { useVirtualizationHelpers } from '@syndesis/api';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';

export const VirtualizationHandlers = () => {

  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { deleteVirtualization, publishVirtualization, unpublishVirtualization } = useVirtualizationHelpers();

  const handleDeleteVirtualization = async (
    pVirtualizationId: string
  ): Promise<boolean> => {
    let success = false;
    try {
      await deleteVirtualization(
        pVirtualizationId
      );
      pushNotification(
        t(
          'virtualization.deleteVirtualizationSuccess',
          { name: pVirtualizationId }
        ),
        'success'
      );
      success = true;
    } catch (error) {
      const details = error.message
        ? error.message
        : '';
      pushNotification(
        t(
          'virtualization.deleteVirtualizationFailed',
          {
            details,
            name: pVirtualizationId,
          }
        ),
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
        await publishVirtualization(
          virtualizationId
        );
  
        pushNotification(
          t(
            'virtualization.publishVirtualizationSuccess',
            { name: virtualizationId }
          ),
          'success'
        );
        success = true;
      } catch (error) {
        const details = error.error
          ? error.error
          : '';
        pushNotification(
          t(
            'virtualization.publishVirtualizationFailed',
            { name: virtualizationId, details }
          ),
          'error'
        );
      }
    } else {
      pushNotification(
        t(
          'virtualization.publishVirtualizationNoViews',
          { name: virtualizationId }
        ),
        'error'
      );
    }
    return Promise.resolve(success);
  }

  const handleUnpublishVirtualization = async (
    virtualizationName: string
  ): Promise<boolean> => {
    let success = false;
    try {
      await unpublishVirtualization(virtualizationName);

      pushNotification(
        t(
          'virtualization.unpublishVirtualizationSuccess',
          { name: virtualizationName }
        ),
        'success'
      );
      success = true;
    } catch (error) {
      const details = error.message
        ? error.message
        : '';
      pushNotification(
        t('virtualization.unpublishFailed', {
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
    handleUnpublishVirtualization
  };

}