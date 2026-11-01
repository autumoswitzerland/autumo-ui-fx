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

	
export JAVA_TOOL_OPTIONS="-Duser.language=en -Duser.country=US"
gradle javadoc
cp -R etc/javadoc/fonts build/docs/javadoc/resource-files
export JAVA_TOOL_OPTIONS="-Duser.language=de -Duser.country=CH"
