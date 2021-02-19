#
# Make Options
#
MK_OPTIONS := -s

.PHONY: $(SUBDIRS)

SYNCDIRS := ../app ../bases/crd ../bases/deployment ../bases/grant ../bases/jaeger-crd ../bases/role ../dev/app ../dev/operator

sync:
	for dir in $(SYNCDIRS); do \
    $(MAKE) $(MK_OPTIONS) -C $$dir init; \
  done

INSTALLDIR := $(RELEASE_NAME)-installer
RELEASEDIRS := ../app ../bases ../dev ../operator ../product ../setup ../setup-jaeger

check-vars:
ifndef RELEASE_VERSION
	$(error RELEASE_VERSION is not set)
endif
ifndef RELEASE_NAME
	$(error RELEASE_NAME is not set)
endif

create:
	#@ Make a new syndesis builddirectory
	mkdir -p $(INSTALLDIR)
	#@ Copy directories into build directory
	for dir in $(RELEASEDIRS); do \
    cp -rf $$dir $(INSTALLDIR)/; \
  done
	#@ Remove any generated yaml files (need the tmpl files)
	for f in `find $(INSTALLDIR) -name "*.gen.yaml"`; do \
		rm -f $$f; \
	done
	#@ Remove sync rules from Makefiles
	for f in `find $(INSTALLDIR) -name Makefile`; do \
		sed -i '/# start-sync/,/# end-sync/c sync:\n  # Not required in release' $$f; \
	done
	#@ Copy the root Makefile into build directory
	cp -f ../Makefile $(INSTALLDIR)/

release: check-vars sync create
	tar zcvf $(INSTALLDIR)-$(RELEASE_VERSION).tar.gz $(INSTALLDIR)

clean:
	rm -rf *-installer *.tar.gz
