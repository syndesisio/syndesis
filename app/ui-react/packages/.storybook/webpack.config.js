const path = require('path');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');
module.exports = ({ config, mode }) => {
  config.module.rules.push({
    test: /\.tsx?$/,
    exclude: /node_modules/,
    use: [
      {
        loader: 'ts-loader',
        options: {
          transpileOnly: true,
          experimentalWatchApi: true,
        }
      },
      'react-docgen-typescript-loader',
    ]
  });
  config.resolve.extensions = ['.ts', '.tsx', '.js', '.jsx'];
  config.resolve.plugins = [
    new TsconfigPathsPlugin({
      configFile: 'tsconfig.storybook.json'
    })
  ];
  config.node = {
    fs: 'empty',
  };
  return config;
};
