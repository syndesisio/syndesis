import { SlugifyPipe } from '@syndesis/ui/common/slugify.pipe';

describe('SlugifyPipe', () => {
  it('create an instance', () => {
    const pipe = new SlugifyPipe();
    expect(pipe).toBeTruthy();
  });

  it('should return a slugified lowercased string', () => {
    const pipe = new SlugifyPipe();
    expect(pipe.transform(`I'm a non-Slugified String`)).toEqual(
      'i-m-a-non-slugified-string'
    );
  });
});
