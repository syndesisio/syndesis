#!/usr/bin/perl

use Getopt::Long;
use Term::ANSIColor qw(:constants);
use strict;

=head1 Transfering Docker images

This sript is from our pipeline/brew builds to our ignite cluster.

B<Before running this script please verify the hash C<RELEASE_MAP> for the
proper tag numbers


Run with

  migrate-images.pl [--source=brew|pipeline]

where C<--brew> speci specifies to pull from brew for productised images,
C<--pipeline> for pipeline builds. C<--brew> is the default.

Prerequisites:

=over 4

=item Docker daemon

=item VPN Access

and the Docker daemon must be able to reach VPN (not the case for
minishift for me)

=back

=head3 Version map

Please update the following C<$RELEASE_MAP> with the latest version of the images.
The entries shout be self-explanatory

=cut

my $RELEASE_MAP =
{
 "TP2" =>
   {
    "ignite" =>
      {
       "registry" => "registry.fuse-ignite.openshift.com/fuse-ignite",
       "images" =>
         {
          "fuse-ignite-rest" => "1.1",
          "fuse-ignite-ui" => "1.1",
          "fuse-ignite-verifier" => "1.1",
          "fuse-ignite-mapper" => "1.1"
         }
      },
    "pipeline" =>
      {
       "registry" => "docker-registry.engineering.redhat.com/jboss-fuse-7-tech-preview",
       "images" =>
         {
          "fuse-ignite-rest" => "1.1",
          "fuse-ignite-ui" => "1.1",
          "fuse-ignite-verifier" => "1.1",
          "fuse-ignite-mapper" => "1.31"
         }
      },
    "brew" =>
      {
       "registry" => "brew-pulp-docker01.web.prod.ext.phx2.redhat.com:8888/jboss-fuse-7-tech-preview",
       "images" =>
         {
          "fuse-ignite-rest" => "1.1-1",
          "fuse-ignite-ui" => "1.1-1",
          "fuse-ignite-verifier" => "1.1-1",
          "fuse-ignite-mapper" => "1.1-1"
         }
      }
   },
 "TP3" =>
   {
    "ignite" =>
      {
       "registry" => "registry.fuse-ignite.openshift.com/fuse-ignite",
       "images" =>
         {
          "fuse-ignite-rest" => "1.2",
          "fuse-ignite-ui" => "1.2",
          "fuse-ignite-verifier" => "1.2",
          "fuse-ignite-s2i" => "1.2",
          "fuse-ignite-mapper" => "1.2",
         }
      },
    "pipeline" =>
      {
       "registry" => "docker-registry.engineering.redhat.com/jboss-fuse-7-tech-preview",
       "images" =>
         {
          "fuse-ignite-rest" => "1.2.3",
          "fuse-ignite-ui" => "1.2.3",
          "fuse-ignite-verifier" => "1.2.3",
          "fuse-ignite-s2i" => "1.2.3",
          "fuse-ignite-mapper" => "1.32.2"
         }
      },
    "brew" =>
      {
       "registry" => "brew-pulp-docker01.web.prod.ext.phx2.redhat.com:8888/jboss-fuse-7-tech-preview",
       "images" =>
         {
          "fuse-ignite-rest" => "1.1-4",
          "fuse-ignite-ui" => "1.1-5",
          "fuse-ignite-verifier" => "1.1-3",
          "fuse-ignite-s2i" => "1.0-2",
          "fuse-ignite-mapper" => "1.1-4"
         }
      }
   },
 "TP4" =>
   {
    "ignite" =>
      {
       "registry" => "registry.fuse-ignite.openshift.com/fuse-ignite",
       "images" =>
         {
          "fuse-ignite-server" => "1.3",
          "fuse-ignite-ui" => "1.3",
          "fuse-ignite-meta" => "1.3",
          "fuse-ignite-s2i" => "1.3"
         }
      },
    "pipeline" =>
      {
       "registry" => "docker-registry.engineering.redhat.com/jboss-fuse-7-tech-preview",
       "images" =>
         {
          "fuse-ignite-server" => "1.3.4",
          "fuse-ignite-ui" => "1.3.4",
          "fuse-ignite-meta" => "1.3.4",
          "fuse-ignite-s2i" => "1.3.4"
         }
      },
    "brew" =>
      {
       "registry" => "brew-pulp-docker01.web.prod.ext.phx2.redhat.com:8888/jboss-fuse-7-tech-preview",
       "images" =>
         {
          "fuse-ignite-server" => "1.3-2",
          "fuse-ignite-ui" => "1.3-2",
          "fuse-ignite-meta" => "1.3-2",
          "fuse-ignite-s2i" => "1.3-2"
         }
      }
   },
};

# Extra images to push
my $EXTRA_IMAGES =
  [
     {
      source => "docker.io/openshift/oauth-proxy:v1.0.0",
      target =>  "oauth-proxy:v1.0.0"
     },
     {
      source => "docker.io/openshift/oauth-proxy:v1.1.0",
      target =>  "oauth-proxy:v1.1.0"
     },
     {
      source => "registry.access.redhat.com/jboss-fuse-6/fis-java-openshift:2.0-9",
      target => "fuse-ignite-java-openshift:1.0"
    },
    {
      source => "docker.io/prom/prometheus:v2.1.0",
      target => "prometheus:v2.1.0"
    }
  ];

# Target system
my $target_key = "ignite";
my $source_key = "brew";
my $release = "TP4";
GetOptions("source=s",\$source_key,
           "release=s",\$release);

die "Invalide source. Must be 'brew' or 'pipeline'" until $source_key eq "brew" || $source_key eq "pipeline";

my $source = $RELEASE_MAP->{$release}->{$source_key} || die "Invalid release '$release'";
my $target = $RELEASE_MAP->{$release}->{$target_key};

print RED,<<EOT,RESET;
===========================================
Moving images from "$source_key" to "$target_key"
===========================================
EOT

for my $image (sort keys %{$source->{images}}) {
    print YELLOW,"* ",GREEN,"Transfering ${image}:",$source->{images}->{$image},"\n",RESET;

    my $pulled_image = &docker_pull(&format_image($image,$source));
    my $tagged_image = &docker_tag($pulled_image, &format_image($image,$target));
    &docker_push($tagged_image);

    # Check for an additional patchlevel tag
    my $source_tag = $source->{images}->{$image};
    if ($source_tag =~ /^\d+\.\d+[.\-](\d+)$/) {
        my $patch_level = $1;
        my $tagged_image = &docker_tag($pulled_image, &format_image($image, $target), $patch_level);
        &docker_push($tagged_image);
    }
}


print RED,<<EOT,RESET;
=====================
Pushing extra images
=====================
EOT

for my $extra (@$EXTRA_IMAGES) {
    my $pulled_image = &docker_pull($extra->{source});
    my $tagged_image = &docker_tag($pulled_image, $target->{registry} . "/" . $extra->{target});
    &docker_push($tagged_image);
}

# ==============================================================================================
sub format_image {
    my $image = shift;
    my $map = shift;
    return sprintf("%s/%s:%s",$map->{registry},$image,$map->{images}->{$image});
}

sub docker_pull {
    my $src_image = shift;
    &exec_cmd("docker","pull",$src_image);
    return $src_image;
}

sub docker_tag {
    my $source_image = shift;
    my $target_image = shift;
    my $patch_level = shift;
    $target_image .= "." . $patch_level if defined($patch_level);
    &exec_cmd("docker","tag",$source_image,$target_image);
    return $target_image;
}

sub docker_push {
    my $target_image = shift;
    &exec_cmd("docker","push","$target_image");
}

sub exec_cmd {
    my @args = @_;
    print join " ",BLUE,@args[0..1],CYAN,"\n    ",@args[2],MAGENTA,"\n    ",@args[3..$#args],RESET,"\n";
    print BRIGHT_BLACK;
    system(@args) == 0 or die "command failed: $?";
    print RESET;
}

# Autoflush
BEGIN {
    $| = 1;
}
