import { ObjectPropertySortPipe } from './object-property-sort.pipe';

describe('ObjectPropertySortPipe', () => {
  let pipe:ObjectPropertySortPipe;
  let testArray: any[] = [
    {
      name: 'foo',
      yes: false,
      number: 8,
      stuff: {
        inner: {
          name: 'foo'
        }
      }
    },
    {
      name: 'bar',
      yes: true,
      number: 5,
      stuff: {
        inner: {
          name: 'bar'
        }
      }
    },
    {
      name: 'bar2',
      yes: false,
      number: 6,
      stuff: {
        inner: {
          name: 'bar2'
        }
      }
    },
    {
      name: 'yum',
      yes: false,
      number: 7,
      stuff: {
        inner: {
          name: 'yum'
        }
      }
    }
  ];
  
  beforeEach(() => {
    pipe = new ObjectPropertySortPipe();
  });

  it('will sort an array of objects', () => {
    let results = pipe.transform(testArray, { sortField: 'name', descending: false });

    expect(results.length).toEqual(4);
    let result1: any = results.shift();
    let result2: any = results.shift();
    let result3: any = results.shift();
    let result4: any = results.shift();
    expect(result1.name).toEqual('bar');
    expect(result2.name).toEqual('bar2');
    expect(result3.name).toEqual('foo');
    expect(result4.name).toEqual('yum');
  });

  it('will sort an array of objects in reverse', () => {
    let results = pipe.transform(testArray, { sortField: 'name', descending: true });

    expect(results.length).toEqual(4);
    let result1: any = results.shift();
    let result2: any = results.shift();
    let result3: any = results.shift();
    let result4: any = results.shift();
    expect(result4.name).toEqual('bar');
    expect(result3.name).toEqual('bar2');
    expect(result2.name).toEqual('foo');
    expect(result1.name).toEqual('yum');
  });

  it('will sort an array of objects using numbers', () => {
    let results = pipe.transform(testArray, { sortField: 'number', descending: false });

    expect(results.length).toEqual(4);
    let result1: any = results.shift();
    let result2: any = results.shift();
    let result3: any = results.shift();
    let result4: any = results.shift();
    expect(result1.name).toEqual('bar');
    expect(result2.name).toEqual('bar2');
    expect(result4.name).toEqual('foo');
    expect(result3.name).toEqual('yum');
  });
  
  
});