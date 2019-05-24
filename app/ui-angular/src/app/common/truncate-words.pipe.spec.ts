import { TruncateWordsPipe } from '@syndesis/ui/common/truncate-words.pipe';

describe('TruncateWordsPipe', () => {
  let pipe: TruncateWordsPipe;

  beforeEach(() => {
    pipe = new TruncateWordsPipe();
  });

  it('transforms "123 456 789" to "123 456…"', () => {
    expect(pipe.transform('123 456 789', 2)).toEqual('123 456…');
  });

  it('transforms "1234 5678 9" to "123xxx"', () => {
    expect(pipe.transform('123 45678 9', 1, 'xxx')).toEqual('123xxx');
  });

  it('transforms "1234 56 7 8" to "1234 56 7…"', () => {
    expect(pipe.transform('1234 56 7 8', 3)).toEqual('1234 56 7…');
  });

  it('leaves "123" unchanged', () => {
    expect(pipe.transform('123 4 56', 3)).toEqual('123 4 56');
  });

  it('leaves "12" unchanged', () => {
    expect(pipe.transform('12', 3)).toEqual('12');
  });

  it('leaves empty string unchanged', () => {
    expect(pipe.transform('', 3)).toEqual('');
  });

  // Left side truncating
  it('[left] transforms "123 456 789" to "…456 789"', () => {
    expect(pipe.transform('123 456 789', -2)).toEqual('…456 789');
  });

  it('[left] transforms "1234 5678 9" to "xxx9"', () => {
    expect(pipe.transform('123 45678 9', -1, 'xxx')).toEqual('xxx9');
  });

  it('[left] leaves empty string unchanged', () => {
    expect(pipe.transform('', 3)).toEqual('');
  });
});
