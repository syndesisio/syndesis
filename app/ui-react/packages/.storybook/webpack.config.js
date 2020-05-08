const path = require('path');
const TsconfigPathsPlugin = require('tsconfig-paths-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
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
  config.resolve.alias = {'vscode' : require.resolve('monaco-languageclient/lib/vscode-compatibility')}
  config.resolve.extensions = ['.ts', '.tsx', '.js', '.jsx'];
  config.resolve.plugins = [
    new TsconfigPathsPlugin({
      configFile: 'tsconfig.storybook.json'
    })
  ];
  config.node = {
    fs: 'empty',
    net: 'mock',
  };
  config.optimization.minimizer = [ new TerserPlugin({
    parallel: 1
  })];
  return config;
};
