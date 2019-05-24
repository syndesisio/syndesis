import { IConnectionWithIconFile, Integration, Step } from '@syndesis/models';
import {
  getEmptyIntegration,
  getIntegrationStepIcon,
} from '../../src/helpers/integrationFunctions';

export default describe('integrationFunctions', () => {
  // Work around missing jsdom implementation of this function for now
  if (typeof window.URL.createObjectURL === 'undefined') {
    Object.defineProperty(window.URL, 'createObjectURL', {
      value: (file: File) => {
        return file.name;
      },
    });
  }
  it('Should return a valid icon URL for a connection with a data URL', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              connection: {
                icon: 'data:blah',
              },
            },
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('data:blah');
  });
  it('Should return a valid icon URL for a legacy connection', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              connection: {
                icon: 'blah',
              },
            },
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('./../../icons/blah.connection.png');
  });
  it('Should return a valid icon URL for a connection that references a db entity', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              connection: {
                icon: 'db:blah',
                id: 'foo',
              },
            },
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('/connectors/foo/icon?db:blah');
  });
  it('Should return a valid icon URL for a connection that references a db entity', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              connection: {
                icon: 'db:blah',
                id: 'foo',
              },
            },
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('/connectors/foo/icon?db:blah');
  });
  it('Should return a valid icon URL for a connection that references a db entity', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              connection: {
                icon: 'extension:blah',
                id: 'foo',
              },
            },
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('/connectors/foo/icon?extension:blah');
  });
  it('Should return a valid icon URL for a connection with an icon file', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              connection: {
                icon: '',
                iconFile: new File([], 'foo'),
              } as IConnectionWithIconFile,
            },
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('foo');
  });
  it('Should return a valid icon URL for an extension step', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              extension: {
                icon: 'blah',
              },
            },
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('blah');
  });
  it('Should return a valid icon URL for a step', () => {
    const integration = {
      ...getEmptyIntegration(),
      flows: [
        {
          id: 'id',
          steps: [
            {
              stepKind: 'log',
            } as Step,
          ],
        },
      ],
    } as Integration;
    const iconPath = getIntegrationStepIcon('', integration, 'id', 0);
    expect(iconPath).toBe('/icons/steps/log.svg');
  });
});
