import { useVirtualizationHelpers } from '@syndesis/api';
import { useContext } from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';

export const VirtualizationHandlers = () => {

  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['data', 'shared']);
  const { deleteVirtualization, publishVirtualization, unpublishServiceVdb } = useVirtualizationHelpers();

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

  const handleUnpublishServiceVdb = async (
    serviceVdbName: string
  ): Promise<boolean> => {
    let success = false;
    try {
      await unpublishServiceVdb(serviceVdbName);

      pushNotification(
        t(
          'virtualization.unpublishVirtualizationSuccess',
          { name: serviceVdbName }
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
          name: serviceVdbName,
        }),
        'error'
      );
    }
    return success;
  };

  return {
    handleDeleteVirtualization,
    handlePublishVirtualization,
    handleUnpublishServiceVdb
  };

}