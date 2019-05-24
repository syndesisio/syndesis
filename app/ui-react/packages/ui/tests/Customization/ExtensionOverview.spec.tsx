import * as React from 'react';
import { render } from 'react-testing-library';
import {
  ExtensionOverview,
  IExtensionOverviewProps,
} from '../../src/Customization';

export default describe('ExtensionOverview', () => {
  const description = 'An extension to Syndesis to do Logging';
  const descriptionLabel = 'Description';
  const lastUpdateDate = 'Dec 10, 2018, 10:32:28 AM';
  const lastUpdateLabel = 'Last Update';
  const name = 'Log';
  const nameLabel = 'Name';
  const typeLabel = 'Type';
  const typeMessage = 'Step Extension';

  const props = {
    extensionDescription: description,
    extensionName: name,
    i18nDescription: descriptionLabel,
    i18nLastUpdate: lastUpdateLabel,
    i18nLastUpdateDate: lastUpdateDate,
    i18nName: nameLabel,
    i18nType: typeLabel,
    i18nTypeMessage: typeMessage,
  } as IExtensionOverviewProps;

  const { extensionDescription, i18nLastUpdateDate, ...requiredProps } = props;

  it('Should render all properties correctly', () => {
    const comp = <ExtensionOverview {...props} />;
    const { queryAllByText } = render(comp);

    // name label
    expect(queryAllByText(nameLabel)).toHaveLength(1);

    // name value
    expect(queryAllByText(name)).toHaveLength(1);

    // description label
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);

    // description value
    expect(queryAllByText(description)).toHaveLength(1);

    // type label
    expect(queryAllByText(typeLabel)).toHaveLength(1);

    // type message
    expect(queryAllByText(typeMessage)).toHaveLength(1);

    // last update label
    expect(queryAllByText(lastUpdateLabel)).toHaveLength(1);

    // last update value
    expect(queryAllByText(lastUpdateDate)).toHaveLength(1);
  });

  it('Should render correctly when only required properties', () => {
    const comp = <ExtensionOverview {...requiredProps} />;
    const { queryAllByText, queryByText } = render(comp);

    // name label
    expect(queryAllByText(nameLabel)).toHaveLength(1);

    // name value
    expect(queryAllByText(name)).toHaveLength(1);

    // description label
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);

    // description value
    expect(queryByText(description)).toBeNull();

    // type label
    expect(queryAllByText(typeLabel)).toHaveLength(1);

    // type message
    expect(queryAllByText(typeMessage)).toHaveLength(1);

    // last update label
    expect(queryAllByText(lastUpdateLabel)).toHaveLength(1);

    // last update value
    expect(queryByText(lastUpdateDate)).toBeNull();
  });
});
