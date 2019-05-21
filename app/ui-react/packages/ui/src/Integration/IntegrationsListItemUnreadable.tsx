import { ListView } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../Layout';
import { toTestId } from '../utils';

export interface IIntegrationsListItemUnreadableProps {
  integrationName: string;
  i18nDescription: string;
  rawObject: string;
}

export const IntegrationsListItemUnreadable: React.FC<
  IIntegrationsListItemUnreadableProps
> = ({ integrationName, i18nDescription, rawObject }) => {
  const onClick = () => window.alert(rawObject);
  return (
    <ListView.Item
      heading={integrationName}
      actions={
        <ButtonLink
          data-testid={`${toTestId(
            IntegrationsListItemUnreadable.name,
            'json-button'
          )}`}
          onClick={onClick}
        >
          Integration JSON
        </ButtonLink>
      }
      description={i18nDescription}
      stacked={true}
    />
  );
};
