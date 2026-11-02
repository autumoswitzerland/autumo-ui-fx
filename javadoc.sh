#!/bin/sh

###############################################################################
#
#  autumo UI FX - Javadoc.
#  Version: 1.0
#
#------------------------------------------------------------------------------
#
#  2026 autumo GmbH
#  Date: 31.10.2026
#
###############################################################################


cd "$(dirname "$0")" || exit 1

# Generate Javadoc
gradle javadoc || exit 1

# Copy additional resources
cp -R etc/doc/javadoc/fonts build/docs/javadoc/resource-files/
cp etc/doc/javadoc/autumo.css build/docs/javadoc/

echo "Javadoc generated in build/docs/javadoc"

