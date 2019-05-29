//
//go:generate go run assets/assets_generate.go
package generator

import (
    "io/ioutil"
    "sort"
    "strings"
    "github.com/hoisie/mustache"
)

type supportImages struct {
    Postgresql       string
    OAuthProxy       string
    OAuthProxyImage  string
    Prometheus       string
    PostgresExporter string
}

type syndesisImages struct {
    Rest     string
    Ui       string
    Verifier string
    S2i      string
    Upgrade  string
}

type images struct {
    Support                     supportImages
    Syndesis                    syndesisImages
    ImageStreamNamespace        string
    SyndesisImagesPrefix        string
    OAuthProxyImagePrefix       string
    PrometheusImagePrefix       string
    PostgresExporterImagePrefix string
}

type tags struct {
    Syndesis         string
    Postgresql       string
    OAuthProxy       string
    Prometheus       string
    Upgrade          string
    PostgresExporter string
}

type Context struct {
    Name             string
    AllowLocalHost   bool
    WithDockerImages bool
    Productized      bool
    EarlyAccess      bool
    Oso              bool
    Ocp              bool
    Tag              string
    Registry         string
    Images           images
    Tags             tags
    Debug            bool
    PrometheusRules  string
}

// TODO: Could be added from a local configuration file

func CreateSyndesisContext() Context {
    return Context{
        Images: images{
            SyndesisImagesPrefix:        "syndesis",
            OAuthProxyImagePrefix:       "quay.io/openshift",
            PrometheusImagePrefix:       "prom",
            PostgresExporterImagePrefix: "wrouesnel",
            Support: supportImages{
                Postgresql:       "postgresql",
                OAuthProxy:       "oauth-proxy",
                OAuthProxyImage:  "origin-oauth-proxy",
                Prometheus:       "prometheus",
                PostgresExporter: "postgres_exporter",
            },
            Syndesis: syndesisImages{
                Rest:     "syndesis-server",
                Ui:       "syndesis-ui",
                Verifier: "syndesis-meta",
                S2i:      "syndesis-s2i",
                Upgrade:  "syndesis-upgrade",
            },
        },
        Tags: tags{
            Postgresql:       "9.5",
            OAuthProxy:       "v4.0.0",
            Prometheus:       "v2.1.0",
            PostgresExporter: "v0.4.7",
        },
    }
}

// TODO: Update with product image references here
func CreateProductContext() Context {
    return Context{
        Images: images{
            ImageStreamNamespace:        "fuse-ignite",
            SyndesisImagesPrefix:        "fuse7",
            OAuthProxyImagePrefix:       "openshift",
            PrometheusImagePrefix:       "prom",
            PostgresExporterImagePrefix: "wrouesnel",
            Support: supportImages{
                Postgresql:       "postgresql",
                OAuthProxy:       "oauth-proxy",
                Prometheus:       "prometheus",
                PostgresExporter: "postgres_exporter",
            },
            Syndesis: syndesisImages{
                Rest:     "fuse-ignite-server",
                Ui:       "fuse-ignite-ui",
                Verifier: "fuse-ignite-meta",
                S2i:      "fuse-ignite-s2i",
                Upgrade:  "fuse-ignite-upgrade",
            },
        },
        Tags: tags{
            Postgresql:       "9.5",
            OAuthProxy:       "v1.1.0",
            Prometheus:       "v2.1.0",
            PostgresExporter: "v0.4.7",
        },
        Registry: "registry.fuse-ignite.openshift.com",
    }
}

func (this *Context) GenerateResources() (string, error) {

    f, err := assets.Open("./")
    if err != nil {
        return "", err
    }
    defer f.Close()

    files, err := f.Readdir(-1)
    if err != nil {
        return "" ,err
    }
    sort.Slice(files, func(i, j int) bool {
        return files[i].Name() < files[j].Name()
    })

    response := []string{}
    for _, f := range files {
        if strings.HasSuffix(f.Name(), ".yml.mustache") {
            tf, err := assets.Open("./"+f.Name());
            if err != nil {
                return "", err
            }
            defer tf.Close()
            template, err := ioutil.ReadAll(tf);
            if err != nil {
                return "", err
            }
            response = append(response, mustache.Render(string(template), this))
        }
    }

    return strings.Join(response, "\n"), nil
}

