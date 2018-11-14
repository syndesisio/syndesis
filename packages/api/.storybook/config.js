import { addDecorator, configure } from "@storybook/react";
import { setOptions } from "@storybook/addon-options";
import { withInfo } from "@storybook/addon-info";
import { configure as configureEnzyme } from "enzyme";
import Adapter from "enzyme-adapter-react-16";

import * as React from "react";

const req = require.context("../src", true, /\.stories\.tsx$/);

function loadStories() {
  req.keys().forEach(filename => req(filename));
}

addDecorator(withInfo({
  inline: true,
  header: false,
  maxPropsIntoLine: 1
}));
configureEnzyme({ adapter: new Adapter() });
configure(loadStories, module);
