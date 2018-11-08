import { storiesOf } from "@storybook/react";
import { shallow } from "enzyme";
import expect from "expect";
import * as React from "react";
import { describe, it, specs } from "storybook-addon-specifications";
import { StoryHelper } from "../.storybook/StoryHelper";
import { AggregatedMetricCard } from "../src/AggregatedMetricCard";

const stories = storiesOf("Components", module);

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add(
    "AggregatedMetricCard",
    () => {
      const story =
        <AggregatedMetricCard
          title={"A Title"}
          ok={10}
          error={5}
        />;

      specs(() => describe("It renders", function() {
        it("Should have the A Title title", function() {
          const wrapper = shallow(story);
          const title = wrapper.find("[data-test-aggregate-title]");
          expect(title.text()).toEqual("A Title");
        });
        it("Should have 5 errors", function() {
          const wrapper = shallow(story);
          const errorCount = wrapper.find("[data-test-aggregate-error-count]");
          expect(errorCount.text()).toEqual("5");
        });
        it("Should have 10 ok", function() {
          const wrapper = shallow(story);
          const okCount = wrapper.find("[data-test-aggregate-ok-count]");
          expect(okCount.text()).toEqual("10");
        });
      }));

      return story;
    }
  );