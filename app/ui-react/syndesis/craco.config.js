const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = function({ env, paths }) {
  return {
    overrideCracoConfig: ({
      cracoConfig,
      pluginOptions,
      context: { env, paths },
    }) => {
      // Always return the config object.
      return cracoConfig;
    },
    webpack: {
      configure: (webpackConfig, { env, paths }) => ({
        ...webpackConfig,

        optimization: {
          ...webpackConfig.optimization,
          minimizer: [
            new TerserPlugin({
              parallel: 1,
            }),
          ],
        },
      }),
      alias: {
        vscode: require.resolve(
          'monaco-languageclient/lib/vscode-compatibility'
        ),
      },
      plugins: [
        new MonacoWebpackPlugin({
          // available options are documented at https://github.com/Microsoft/monaco-editor-webpack-plugin#options
          languages: ['sql', 'pgsql'],
        }),
      ],
    },
  };
};
