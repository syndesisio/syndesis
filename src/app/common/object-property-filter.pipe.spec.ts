import { ObjectPropertyFilterPipe } from './object-property-filter.pipe';

describe('ObjectPropertyFilterPipe', () => {

  let pipe: ObjectPropertyFilterPipe;
  let testArray = [
    {
      name: 'foo',
      yes: false,
      number: 4,
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
    pipe = new ObjectPropertyFilterPipe();
  });

  it('will return a filtered list of objects', () => {
    let results = pipe.transform(testArray, { filter: 'ba', propertyName: 'name' });
    expect(results.length).toEqual(2);
    let result = results.shift();
    expect(result.name).toEqual('bar');    
    let result2 = results.shift();
    expect(result2.name).toEqual('bar2');    
  });

  it('will not return any objects if the filter doesnt match', () => {
    let results = pipe.transform(testArray, { filter: 'bla', propertyName: 'name' });
    expect(results.length).toEqual(0);
  });

  it('can filter on a numeric value', () => {
    let results = pipe.transform(testArray, { filter: 4, propertyName: 'number' });
    expect(results.length).toEqual(1);
    let result = results.shift();
    expect(result.name).toEqual('foo');
  });

  it('can filter on a boolean', () => {
    let results = pipe.transform(testArray, { filter: true, propertyName: 'yes' });
    expect(results.length).toEqual(1);
    let result = results.shift();
    expect(result.name).toEqual('bar');
  });

  it('can filter on an object', () => {
    let results = pipe.transform(testArray, { filter: {
      inner: {
        name: 'bar'
      }
    }, propertyName: 'stuff' });
    expect(results.length).toEqual(1);
    let result = results.shift();
    expect(result.name).toEqual('bar');    
  });

  it('can descend into an object', () => {
    let results = pipe.transform(testArray, { filter: 'ba', propertyName: 'stuff.inner.name' });
    expect(results.length).toEqual(2);
    let result = results.shift();
    expect(result.name).toEqual('bar');    
    let result2 = results.shift();
    expect(result2.name).toEqual('bar2');
  });

  it('lets me use a function', () => {
    let results = pipe.transform(testArray, { filter: (val:any) => val === 'bar', propertyName: 'name' });
    expect(results.length).toEqual(1);
  });

})