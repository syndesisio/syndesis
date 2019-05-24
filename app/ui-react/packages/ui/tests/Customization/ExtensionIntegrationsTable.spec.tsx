import * as React from 'react';
import { fireEvent, render } from 'react-testing-library';
import {
  ExtensionIntegrationsTable,
  IExtensionIntegration,
} from '../../src/Customization';

export default describe('ExtensionIntegrationsTable', () => {
  const nameLabel = 'Name';
  const descriptionLabel = 'Description';
  const integrations = [
    { id: '1', name: 'int-1', description: 'integration one description' },
    { id: '2', name: 'int-2', description: 'integration two description' },
    { id: '3', name: 'int-3', description: 'integration three description' },
  ] as IExtensionIntegration[];
  const mockOnSelectIntegration = jest.fn();
  const usageMessage = 'Used by 3 integrations';

  it('Should render correctly', () => {
    const comp = (
      <ExtensionIntegrationsTable
        i18nDescription={descriptionLabel}
        i18nName={nameLabel}
        i18nUsageMessage={usageMessage}
        onSelectIntegration={mockOnSelectIntegration}
        data={integrations}
      />
    );

    const { getByText, queryByText, queryAllByText } = render(comp);

    // description table header
    expect(queryAllByText(descriptionLabel)).toHaveLength(1);

    // name table header
    expect(queryAllByText(nameLabel)).toHaveLength(1);

    // usage message
    expect(queryAllByText(nameLabel)).toHaveLength(1);

    // integrations
    integrations.map(a => {
      const link = getByText(a.name);
      expect(queryByText(a.description)).toBeDefined();

      // callback
      fireEvent.click(link);
      expect(mockOnSelectIntegration).toBeCalledWith(a.id);
    });
  });
});
