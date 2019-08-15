/*
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grant

import (
    "fmt"
    "github.com/operator-framework/operator-sdk/pkg/log/zap"
    "github.com/pkg/errors"
    "github.com/spf13/cobra"
    "github.com/syndesisio/syndesis/install/operator/pkg"
    "github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
    "github.com/syndesisio/syndesis/install/operator/pkg/generator"
    "github.com/syndesisio/syndesis/install/operator/pkg/util"
    "os/exec"
)

type Grant struct {
    *internal.Options
    Role    string
    cluster bool
    user    string
}

func New(parent *internal.Options) *cobra.Command {
    o := Grant{Options: parent}
    cmd := cobra.Command{
        Use:   "grant",
        Short: "grants permissions needed to run the operator(requires namespace admin privileges or cluster admin privileges)",
        Run: func(_ *cobra.Command, _ []string) {
            util.ExitOnError(o.grant())
        },
    }

    cmd.PersistentFlags().BoolVarP(&o.cluster, "cluster", "", false, "add the permission for all projects in the cluster(requires cluster admin privileges)")
    cmd.PersistentFlags().StringVarP(&o.user, "user", "u", pkg.DefaultOperatorImage, "add permissions for the given user")
    cmd.PersistentFlags().AddFlagSet(zap.FlagSet())
    cmd.MarkFlagRequired("user")

    return &cmd
}

func (o *Grant) grant() error {
    if util.CommandExists("oc") {
        o.Role = fmt.Sprintf("syndesis-operator-%s", o.user)

        role, err := generator.Render("./install/role.yml.tmpl", o)
        if err != nil {
            return err
        }

        client, err := o.NewClient()
        for _, res := range role {
            res.SetNamespace(o.Namespace)

            _, _, err := util.CreateOrUpdate(o.Context, client, &res)
            if err != nil {
                return errors.Wrap(err, util.Dump(res))
            }
        }

        cmd := exec.Command("oc", "policy", "add-role-to-user", o.Role, o.user)
        if o.cluster != false {
            cmd = exec.Command("oc", "adm", "policy", "add-cluster-role-to-user", o.Role, o.user)
        }

        err = cmd.Run()
        if err != nil {
            return err
        }
        fmt.Println("role", o.Role, "granted to", o.user)
    } else {
        return fmt.Errorf("extra permissions for user %s not granted, oc command is missing", o.user)
    }
    return nil
}
