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
    const componentWithAllProps = <ExtensionOverview {...props} />;
    const { queryAllByText, queryByText } = render(componentWithAllProps);

    // name label
    expect(queryAllByText(nameLabel)).toHaveLength(1);
    expect(queryByText(nameLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Name
</div>
`);

    // name value
    expect(queryAllByText(name)).toHaveLength(1);
    expect(queryByText(name)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyValue"
>
  Log
</div>
`);

    // description label
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);
    expect(queryByText(descriptionLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Description
</div>
`);

    // description value
    expect(queryAllByText(description)).toHaveLength(1);
    expect(queryByText(description)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyValue"
>
  An extension to Syndesis to do Logging
</div>
`);

    // type label
    expect(queryAllByText(typeLabel)).toHaveLength(1);
    expect(queryByText(typeLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Type
</div>
`);

    // type message
    expect(queryAllByText(typeMessage)).toHaveLength(1);
    expect(queryByText(typeMessage)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyValue"
>
  Step Extension
</div>
`);

    // last update label
    expect(queryAllByText(lastUpdateLabel)).toHaveLength(1);
    expect(queryByText(lastUpdateLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Last Update
</div>
`);

    // last update value
    expect(queryAllByText(lastUpdateDate)).toHaveLength(1);
    expect(queryByText(lastUpdateDate)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyValue"
>
  Dec 10, 2018, 10:32:28 AM
</div>
`);
  });

  it('Should render correctly when only required properties', () => {
    const componentWithAllProps = <ExtensionOverview {...requiredProps} />;
    const { queryAllByText, queryByText } = render(componentWithAllProps);

    // name label
    expect(queryAllByText(nameLabel)).toHaveLength(1);
    expect(queryByText(nameLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Name
</div>
`);

    // name value
    expect(queryAllByText(name)).toHaveLength(1);
    expect(queryByText(name)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyValue"
>
  Log
</div>
`);

    // description label
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);
    expect(queryByText(descriptionLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Description
</div>
`);

    // description value
    expect(queryByText(description)).toBeNull();

    // type label
    expect(queryAllByText(typeLabel)).toHaveLength(1);
    expect(queryByText(typeLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Type
</div>
`);

    // type message
    expect(queryAllByText(typeMessage)).toHaveLength(1);
    expect(queryByText(typeMessage)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyValue"
>
  Step Extension
</div>
`);

    // last update label
    expect(queryAllByText(lastUpdateLabel)).toHaveLength(1);
    expect(queryByText(lastUpdateLabel)).toMatchInlineSnapshot(`
<div
  class="extension-overview__propertyLabel col-xs-2"
>
  Last Update
</div>
`);

    // last update value
    expect(queryByText(lastUpdateDate)).toBeNull();
  });
});
