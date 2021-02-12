import { Action } from '@syndesis/models';
import { getActionsWithPattern } from '../connectionFunctions';

describe('connection functions', () => {
  it(`filters actions by pattern`, () => {
    const actions = [
      'Pipe',
      'From',
      'To',
      'From',
      'To',
      'From',
      'From',
      'To',
      'Pipe',
      'PollEnrich',
    ].map(
      p =>
        ({
          pattern: p,
        } as Action)
    );

    expect(getActionsWithPattern('From', actions)).toHaveLength(4);
    expect(getActionsWithPattern('To', actions)).toHaveLength(3);
    expect(getActionsWithPattern('Pipe', actions)).toHaveLength(2);
    expect(getActionsWithPattern('PollEnrich', actions)).toHaveLength(1);
  });
});
