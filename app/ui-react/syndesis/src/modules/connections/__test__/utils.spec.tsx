import { substituteDefaultWithConfiguredProperties } from '../utils';

describe('utilities', () => {
  test('should not substitute if no configured properties given', async () => {
    const property = {};
    expect(
      substituteDefaultWithConfiguredProperties('property', property, undefined)
    ).toBe(property);
  });

  test('should not substitute when no configured property value is given', async () => {
    const property = {
      defaultValue: 'default value',
    };
    const configured = {
      different: 'configured',
    };
    expect(
      substituteDefaultWithConfiguredProperties(
        'property',
        property,
        configured
      )
    ).toBe(property);
  });

  test('should substitute when configured property value is given', async () => {
    const property = {
      defaultValue: 'default value',
    };
    const configured = {
      property: 'configured',
    };
    const expected = {
      defaultValue: 'configured',
    };
    expect(
      substituteDefaultWithConfiguredProperties(
        'property',
        property,
        configured
      )
    ).toEqual(expected);
  });

  test('should not substitute if configured property value is equal to default value', async () => {
    const property = {
      defaultValue: 'value',
    };
    const configured = {
      property: 'value',
    };
    expect(
      substituteDefaultWithConfiguredProperties(
        'property',
        property,
        configured
      )
    ).toBe(property);
  });
});
