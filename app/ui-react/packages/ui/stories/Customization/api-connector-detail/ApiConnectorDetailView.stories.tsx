import { action } from '@storybook/addon-actions';
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
      connectorIcon={icons.github}
    />
  );
});

stories.add('API Connector Detail Body', () => {
  return (
    <ApiConnectorDetailBody
      basePath={connector.configuredProperties.basePath}
      name={connector.name}
      description={connector.description}
      host={connector.configuredProperties.host}
      i18nCancelLabel={'Cancel'}
      i18nEditLabel={'Edit'}
      i18nLabelBaseUrl={'Base URL'}
      i18nLabelDescription={'Description'}
      i18nLabelHost={'Host'}
      i18nLabelName={'Name'}
      i18nNameHelper={'Please provide a name for the API Connector'}
      i18nRequiredText={'The fields marked with * are required.'}
      i18nSaveLabel={'Save'}
      i18nTitle={connector.name + ' Configuration'}
      icon={icons.beer}
      handleSubmit={action('submit')}
      i18nLabelAddress={'Address'}
      propertyKeys={[]}
    />
  );
});
