import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorDetailHeader } from '../../../src/Customization/apiClientConnectors/detail';
import icons from '../../Shared/icons';
import connector from '../openapi-connector';

const stories = storiesOf('Customization/ApiClientConnector/Detail', module);

stories.add('API Connector Detail Header', () => {
  const getUsedByMessage = (c: any): string => {
    const numUsedBy = c.uses as number;
    if (numUsedBy === 1) {
      // return i18n.t('apiConnector:usedByOne');
      return 'Used by 1 integration';
    }
    return 'Used by integrations ' + numUsedBy + 'times';
    // return i18n.t('apiConnector:usedByMulti', { count: numUsedBy });
  };

  return (
    <ApiConnectorDetailHeader
      i18nDescription={'Description'}
      i18nUsageLabel={'Usage'}
      i18nUsageMessage={getUsedByMessage(connector)}
      connectorName={connector.name}
      connectorDescription={connector.description}
      connectorIcon={icons.beer}
    />
  );
});
