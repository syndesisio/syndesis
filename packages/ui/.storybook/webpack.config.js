module.exports = (storybookBaseConfig, configType, defaultConfig) => {
  defaultConfig.module.rules.push(
    {
      test: /\.tsx?$/,
      exclude: /node_modules/,
      use: [
        "awesome-typescript-loader?configFileName=tsconfig.storybook.json",
        "react-docgen-typescript-loader"
      ]
    }
  );
  defaultConfig.resolve.extensions = [".ts", ".tsx", ".js", ".jsx"];
  return defaultConfig;
};