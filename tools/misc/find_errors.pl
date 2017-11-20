#!/usr/bin/perl

use Getopt::Long;
use Term::ANSIColor qw(:constants);
use strict;

my $logs;
GetOptions("logs",\$logs);

for my $project (sort &get_projects("proj")) {
    my %error_pods = ();
    for my $pod_line (split /\n/,`oc get pods --no-headers -n $project`) {
        my @cols = split /\s+/,$pod_line;
        $error_pods{$cols[0]} = $cols[2] if ($cols[2] =~ /Error|CrashLoopback/i);
    }
    if (%error_pods) {
        print BLUE,$project,RESET,":\n";
        for my $pod (sort keys %error_pods) {
            printf "    %-25s %s\n",$pod,$error_pods{$pod};
            print BRIGHT_BLACK,`oc logs $pod -n $project | tail -30`,RESET,"\n";
        }
    }
}

sub get_projects {
    my $pattern = shift;
    my @projects = split /\n/,`oc get projects --no-headers -o custom-columns=NAME:metadata.name`;
    return $pattern ? grep { /$pattern/ } @projects : @projects;
}
