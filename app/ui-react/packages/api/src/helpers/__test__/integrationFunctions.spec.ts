import {
  Connection,
  DataShape,
  Flow,
  Integration,
  Step,
} from '@syndesis/models';
import produce from 'immer';
import { DataShapeKinds } from '../../constants';
import {
  getFlow,
  hasDataShape,
  isIntegrationApiProvider,
  isPrimaryFlow,
  removeStepFromFlow,
} from '../integrationFunctions';

describe('integration functions', () => {
  test.each`
    kind                   | place       | expected
    ${undefined}           | ${'input'}  | ${'not present'}
    ${undefined}           | ${'output'} | ${'not present'}
    ${DataShapeKinds.NONE} | ${'input'}  | ${'present'}
    ${DataShapeKinds.NONE} | ${'output'} | ${'not present'}
    ${DataShapeKinds.ANY}  | ${'input'}  | ${'present'}
    ${DataShapeKinds.ANY}  | ${'output'} | ${'present'}
  `(
    '$kind $place data shape should be asserted as $expected',
    ({ kind, place, expected }) => {
      const step = {
        action: {
          descriptor: {},
        },
      } as Step;

      const dataShape = {
        kind: kind as DataShapeKinds,
      } as DataShape;

      const isInput = place === 'input';
      if (isInput) {
        step.action!.descriptor!.inputDataShape = dataShape;
      } else {
        step.action!.descriptor!.outputDataShape = dataShape;
      }

      expect(hasDataShape(step, isInput)).toBe(expected === 'present');
    }
  );

  it(`steps without actions don't have shapes`, () => {
    const step = {};
    expect(hasDataShape(step, true)).toBe(false);
  });

  it(`steps without action descriptors don't have shapes`, () => {
    const step = {
      action: {},
    } as Step;

    expect(hasDataShape(step, true)).toBe(false);
  });

  it(`should return a specified flow from an integration`, () => {
    const connections: Connection[] = [];

    const flows: Flow[] = [
      {
        connections,
        id: '123456',
        name: 'hello!',
      },
      {
        connections,
        id: '123457',
        name: 'goodbye!',
      },
    ];

    const integration: Integration = {
      flows,
      name: 'Tiny Integration',
    };

    expect(getFlow(integration, '123457')).toBe(flows[1]);
  });

  it(`should determine if the provided flow is primary`, () => {
    const primFlow: Flow = {
      name: 'Some super duper primary flow..',
      type: 'PRIMARY',
    };
    const nonPrimFlow: Flow = {
      name: 'An alternate flow',
      type: 'ALTERNATE',
    };

    expect(isPrimaryFlow(primFlow)).toBeTruthy();
    expect(isPrimaryFlow(nonPrimFlow)).toBeFalsy();

    nonPrimFlow.type = 'API_PROVIDER';

    expect(nonPrimFlow.type).toBe('API_PROVIDER');
    /**
     * isPrimaryFlow checks for BOTH Primary + API Provider types
     */
    expect(isPrimaryFlow(nonPrimFlow)).toBeTruthy();
  });

  it(`should determine if the provided integration is an API provider integration`, () => {
    /**
     * The way this is determined is by including `API_PROVIDER`
     * as a tag within the integration
     */
    const integration: Integration = {
      flows: [],
      name: 'Tiny Integration',
      tags: ['api-provider'],
    };

    expect(isIntegrationApiProvider(integration)).toBeTruthy();
  });

  it(`should remove banana step from the provided integration`, async () => {
    const customPosition = 1;
    const fetchStepDescriptors = jest.fn().mockImplementation(() => {
      return steps;
    });

    const removeStep = async (
      integration: Integration,
      flowId: string,
      position: number
    ) => {
      return produce(integration, () => {
        return removeStepFromFlow(
          integration,
          flowId,
          position,
          fetchStepDescriptors
        );
      });
    };

    /**
     * If step is first or last position,
     * the step should be deleted and user
     * redirected to the step select
     * page for that position.
     */

    const steps: Step[] = [
      {
        connection: { name: 'peach' },
        id: '1234567',
      },
      {
        connection: { name: 'banana' },
        id: '1234567',
      },
      {
        connection: { name: 'apple' },
        id: '1234567',
      },
      {
        connection: { name: 'mango' },
        id: '1234567',
      },
    ];

    const flows: Flow[] = [
      {
        id: '-MW_06XdNSe0O_IutbEY',
        name: 'My Fun Flow',
        steps,
      },
    ];

    const customIntegration: Integration = {
      flows,
      name: 'Tiny Integration',
    };

    const newInt = await removeStep(
      customIntegration,
      '-MW_06XdNSe0O_IutbEY',
      customPosition
    );

    expect(newInt!.flows![0].steps).toHaveLength(3);
  });
});
