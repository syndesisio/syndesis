import { DataShape, Step } from '@syndesis/models';
import { DataShapeKinds } from '../../constants';
import { hasDataShape } from '../integrationFunctions';

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
});
