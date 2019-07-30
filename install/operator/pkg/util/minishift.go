package util

import (
	"fmt"
	"github.com/chirino/hawtgo/sh"
	"k8s.io/client-go/rest"
	"path/filepath"
	"regexp"
)

type MinishiftCluster struct {
	config *rest.Config
	oc     string
}

func GetMinishiftCluster(c *rest.Config) *MinishiftCluster {
	ip, _, err := sh.New().Line(`minishift ip"`).Output()
	if err != nil {
		return nil
	}

	host := fmt.Sprintf("https://%s:8443", ip)
	if c.Host != host {
		return nil
	}

	ocenv, _, err := sh.New().Line(`minishift oc-env"`).Output()
	if err != nil {
		return nil
	}
	re := regexp.MustCompile(`.*export PATH="(.*?):\$PATH.*`)
	submatch := re.FindAllStringSubmatch(ocenv, -1)
	if !(len(submatch) == 1 && len(submatch[0]) == 2) {
		return nil
	}
	oc := filepath.Join(submatch[0][1], "oc")

	return &MinishiftCluster{
		config: c,
		oc:     oc,
	}
}

func (this *MinishiftCluster) MinishiftAdminLogin() error {
	_, _, err := sh.New().LineArgs(this.oc, `login`, `-u`, `system:admin`).Output()
	if err != nil {
		return err
	}
	return nil
}

func (this *MinishiftCluster) MinishiftUserLogin(username string) error {
	_, _, err := sh.New().LineArgs(this.oc, `login`, `-u`, username, `-p`, username).Output()
	if err != nil {
		return err
	}
	return nil
}

func (this *MinishiftCluster) MinishiftWhoAmI() (string, error) {
	username, _, err := sh.New().LineArgs(this.oc, `whoami`).Output()
	if err != nil {
		return "", err
	}
	return username, nil
}

func RunAsMinishiftAdminIfPossible(c *rest.Config, action func() error) error {
	cluster := GetMinishiftCluster(c)
	if cluster != nil {
		originalUser, err := cluster.MinishiftWhoAmI()
		if err == nil {
			if originalUser != "admin" {
				err = cluster.MinishiftAdminLogin() // switch to admin..
				if err == nil {
					defer cluster.MinishiftUserLogin(originalUser) // make sure we switch back
					return action()
				}
			}
		}
	}
	return action()
}
