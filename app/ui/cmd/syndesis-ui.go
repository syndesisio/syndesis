/*
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package main

import (
	"fmt"
	"net/http"
	"os"
	"regexp"
	"time"

	simhttp "github.com/jboss-fuse/simble/v1/cmd/http"
	"github.com/jboss-fuse/simble/v1/pkg/simble"
	simecho "github.com/jboss-fuse/simble/v1/pkg/simble/echo"
	"github.com/labstack/echo"
)

func main() {
	// static.DefaultAssetFS = &assetfs.AssetFS{Asset: Asset, AssetDir: AssetDir, AssetInfo: AssetInfo, Prefix: ""}
	// Lets update our root command be named what the
	// executable is actually called
	simhttp.Command.Use = os.Args[0]
	if err := simhttp.Command.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func init() {
	simble.AddPlugin(simecho.InitEchoMiddlewarePhase, func(server *simble.Simble) error {
		ctx := server.Context(&simecho.EchoContext{}).(*simecho.EchoContext)

		// Only allow caching of Hashed assets.
		ctx.Echo.Use(func(next echo.HandlerFunc) echo.HandlerFunc {
			HASHED_ASSET_REGEX := regexp.MustCompile(`^.+\.[a-f0-9]{20}\..+$`)

			return func(c echo.Context) error {
				res := c.Response()
				if HASHED_ASSET_REGEX.MatchString(c.Request().URL.Path) {
					// Only allow caching of hashed assets, since on a new version, they will
					// will be renamed if they are changed.
					res.Header().Set("Cache-Control", "max-age=31556926") // 1 Year
				} else {
					res.Header().Set("Cache-Control", "no-cache, no-store, must-revalidate")
					res.Header().Set("Pragma", "no-cache")
					res.Header().Set("Expires", "0")
				}
				return next(c)
			}
		})

		// Install a /logout handler
		ctx.Echo.Use(func(next echo.HandlerFunc) echo.HandlerFunc {
			return func(c echo.Context) error {
				url := c.Request().URL
				if url.Path == "/logout" {
					if c.Request().Header.Get("SYNDESIS-XSRF-TOKEN") == "awesome" {
						cookie := new(http.Cookie)
						cookie.HttpOnly = true
						cookie.Secure = true
						cookie.Domain = c.Request().Host
						cookie.Path = "/"
						cookie.Name = "_oauth_proxy"
						cookie.Value = ""
						cookie.Expires = time.Unix(0, 0)
						c.SetCookie(cookie)
					}
					// Serve up the content from logout.html
					url.Path = "/logout.html"
				}
				return next(c)
			}
		})

		return nil
	})

}
