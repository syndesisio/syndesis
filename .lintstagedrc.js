module.exports = {
  '*.go': files => [
    `go fmt ${files.join(' ')}`,
    `git add ${files.join(' ')}`
  ],
  '*.{java,properties,xml}': files => [
    `./tools/misc/format-license.sh ${files.join(' ')}`,
    `git add ${files.join(' ')}`
  ],
  'install/operator/pkg/generator/assets/**': () => [
    'go generate ./install/operator/pkg/...'
  ]
}
