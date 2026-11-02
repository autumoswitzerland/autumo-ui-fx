#!/bin/sh

###############################################################################
#
#  autumo UI FX.
#  Version: 1.0
#
#  Notes:
#   - creates the product package (make create)
#
#------------------------------------------------------------------------------
#
#  2024-2026 autumo GmbH
#  Date: 31.10.2026
#
###############################################################################



# VARS
VERSION=1.2.0

# Always operate from this script's directory, regardless of the caller's CWD
cd "$(dirname "$0")"


# ------------------------------------------------
# -------- Usage
# ------------------------------------------------
if [ "$1" = "help" -o "$#" -lt 1 ]
then
	echo " "
	echo "make clear"
	echo "make create"
	echo " "
	echo " "
	exit 1
fi


mkdir -p product


# ------------------------------------------------
# -------- DELETE PRODUCT
# ------------------------------------------------
if [ "$1" = "clear" ]
then
	cd product

	# remove working directory
	if [ -d "autumo-ui-fx-$VERSION" ]
	then
		rm -Rf autumo-ui-fx-$VERSION
	fi
	
	# remove package
	if [ -f "autumo-ui-fx-$VERSION.zip" ]
	then
    	rm autumo-ui-fx-$VERSION.zip
	fi	
	
	cd ..

	exit 0
fi


# ------------------------------------------------
# -------- CREATE PRODUCT
# ------------------------------------------------
if [ "$1" = "create" -a "$#" -gt 0 ]
then


# -----------------------------
# ---- Cleanup & Prepare
# -----------------------------

	echo "-> Cleanup & prepare..."
	
	# delete old product package
	if [ -d "product/autumo-ui-fx-$VERSION" ]
	then
		rm -Rf product/autumo-ui-fx-$VERSION
	fi	

	# go to product
	cd product
	
	# make working directory
	mkdir autumo-ui-fx-$VERSION



# -----------------------------
# ---- Go to package
# -----------------------------

	# go to package folder
	cd autumo-ui-fx-$VERSION



# -----------------------------
# ---- Copying
# -----------------------------
		

	echo "-> Copying..."

	# Documentation: the README (Markdown) and the generated PDF
	cp ../../README.md .
	mkdir -p etc/doc
	cp ../../etc/doc/autumo-ui-fx-logo.png etc/doc/

	cp -R ../../build/docs/javadoc etc/
	
	mkdir -p lib
	cp ../../build/libs/autumo-ui-fx-$VERSION.jar lib/
	cp ../../build/libs/autumo-ui-fx-$VERSION-sources.jar lib/
	
	cp ../../LICENSE.md .
	cp ../../THIRDPARTYLICENSES.md .
	

# -----------------------------
# ---- Create Product
# -----------------------------

	echo "-> Create PRODUCT..."

	# LEAVE PACKAGE FOLDER
	cd ..

	# create archive
	zip -r "autumo-ui-fx-${VERSION}.zip" autumo-ui-fx-${VERSION} \
		-x "*/.DS_Store" \
		-x "*/__MACOSX"

	# delete build directory
	rm -Rf autumo-ui-fx-${VERSION}
	

	
# -----------------------------
# ---- END
# -----------------------------
	
	# leave product folder
	cd ..

	
else
	echo "Nope! -> make create|clear"
	echo " "
fi



