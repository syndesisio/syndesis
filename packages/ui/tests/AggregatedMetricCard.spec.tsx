import { shallow } from 'enzyme';
import expect from 'expect';
import * as React from 'react';
import { AggregatedMetricCard } from '../src';

export default describe('AggregatedMetricCard', function() {
  const story = <AggregatedMetricCard title={'A Title'} ok={10} error={5} />;

  it('Should have the A Title title', function() {
    const wrapper = shallow(story);
    const title = wrapper.find('[data-test-aggregate-title]');
    expect(title.text()).toEqual('A Title');
  });
  it('Should have 5 errors', function() {
    const wrapper = shallow(story);
    const errorCount = wrapper.find('[data-test-aggregate-error-count]');
    expect(errorCount.text()).toEqual('5');
  });
  it('Should have 10 ok', function() {
    const wrapper = shallow(story);
    const okCount = wrapper.find('[data-test-aggregate-ok-count]');
    expect(okCount.text()).toEqual('10');
  });
});
