import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ApiConnectorDetailBody,
  ApiConnectorDetailHeader,
} from '../../../src/Customization/apiClientConnectors/detail';
import icons from '../../Shared/icons';
import connector from '../openapi-connector';

const stories = storiesOf('Customization/ApiClientConnector/Detail', module);

stories.add('API Connector Detail Header', () => {
  const getUsedByMessage = (c: any): string => {
    const numUsedBy = c.uses as number;
    if (numUsedBy === 1) {
      return 'Used by 1 integration';
    }
    return 'Used by integrations ' + numUsedBy + 'times';
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

stories.add('API Connector Detail Body', () => {
  return (
    <ApiConnectorDetailBody
      name={connector.name}
      description={connector.description}
      icon={icons.beer}
      i18nCancelLabel={'Cancel'}
      i18nEditLabel={'Edit'}
      i18nSaveLabel={'Save'}
      i18nTitle={connector.name + ' Configuration'}
      i18nValidateLabel={'Validate'}
    />
  );
});
