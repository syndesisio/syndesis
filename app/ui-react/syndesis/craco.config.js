const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = function({ env, paths }) {
  return {
    // disable the custom loading of sourcemaps, it's too slow
    /*
    webpack: {
      configure: (webpackConfig, { env, paths }) => {
        webpackConfig.module.rules[2].oneOf = webpackConfig.module.rules[2].oneOf.map(
          r => {
            if (r.loader && r.loader.indexOf('babel-loader') >= 0) {
              r.options.sourceMaps = true;
            }
            return r;
          }
        );
        return webpackConfig;
      },
    },
    */
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
              parallel: 2,
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
